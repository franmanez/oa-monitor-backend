package es.oamonitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import es.oamonitor.harvest.OpenAlexClient;
import es.oamonitor.model.HarvestJob;
import es.oamonitor.model.Institution;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.*;
import es.oamonitor.repository.HarvestJobRepository;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OpenAlexHarvestService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAlexHarvestService.class);
    
    private final InstitutionRepository institutionRepository;
    private final PublicationRepository publicationRepository;
    private final HarvestJobRepository harvestJobRepository;
    private final InstitutionService institutionService;
    private final OpenAlexClient openAlexClient;

    public OpenAlexHarvestService(InstitutionRepository institutionRepository,
                                  PublicationRepository publicationRepository,
                                  HarvestJobRepository harvestJobRepository,
                                  InstitutionService institutionService,
                                  OpenAlexClient openAlexClient) {
        this.institutionRepository = institutionRepository;
        this.publicationRepository = publicationRepository;
        this.harvestJobRepository = harvestJobRepository;
        this.institutionService = institutionService;
        this.openAlexClient = openAlexClient;
    }

    @Transactional
    public HarvestJob harvestInstitution(UUID institutionId, int year) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found: " + institutionId));

        HarvestJob job = new HarvestJob();
        job.setInstitution(institution);
        job.setHarvestType(HarvestType.OPENALEX);
        job.setStatus(HarvestStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        job = harvestJobRepository.save(job);

        int fetched = 0;
        int savedCount = 0;
        int skippedNoDoi = 0;
        int pageNum = 0;
        Set<String> seenDois = new HashSet<>();
        try {
            String cursor = "*";
            while (cursor != null) {
                pageNum++;
                logger.info("Fetching page {} with cursor: {}", pageNum, cursor.substring(0, Math.min(50, cursor.length())));
                
                JsonNode response = openAlexClient.fetchWorks(institution.getRorId(), year, cursor);
                JsonNode results = response.get("results");
                int pageSize = 0;
                if (results != null && results.isArray()) {
                    for (JsonNode work : results) {
                        WorkResult result;
                        try {
                            result = processWork(work, institution, seenDois);
                        } catch (Exception e) {
                            logger.error("Error processing work: {}", e.getMessage());
                            fetched++;
                            pageSize++;
                            continue;
                        }
                        fetched++;
                        if (result == WorkResult.SKIPPED_NO_DOI || result == WorkResult.SKIPPED_DUPLICATE) {
                            skippedNoDoi++;
                        } else {
                            savedCount++;
                        }
                        pageSize++;
                    }
                }
                logger.info("Page {} processed {} works, {} saved to DB", pageNum, pageSize, savedCount);

                JsonNode meta = response.get("meta");
                JsonNode nextCursor = meta != null ? meta.get("next_cursor") : null;
                cursor = (nextCursor != null && !nextCursor.isNull()) ? nextCursor.asText() : null;
                
                if (cursor != null) {
                    logger.info("Next cursor found, continuing pagination...");
                } else {
                    logger.info("No more pages, pagination complete");
                }
            }
            
            logger.info("Harvest complete: {} fetched from API, {} skipped (no DOI or duplicate), {} saved to DB",
                    fetched, skippedNoDoi, savedCount);

            institutionService.updateInstitutionAfterHarvest(institution);
            job.setStatus(HarvestStatus.COMPLETED);
            job.setRecordsFetched(savedCount);
            job.setRecordsUpdated(savedCount);
            logger.info("Job completed: {} records fetched, {} saved", fetched, savedCount);
        } catch (Exception e) {
            logger.error("Harvest failed: {}", e.getMessage(), e);
            job.setStatus(HarvestStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setFinishedAt(LocalDateTime.now());
            job = harvestJobRepository.save(job);
        }

        return job;
    }

    private enum WorkResult { CREATED, UPDATED, SKIPPED_NO_DOI, SKIPPED_DUPLICATE }

    private WorkResult processWork(JsonNode work, Institution institution, Set<String> seenDois) {
        String doi = extractDoi(work);
        if (doi == null) {
            logger.debug("Skipping work without DOI: {}", extractTitle(work));
            return WorkResult.SKIPPED_NO_DOI;
        }

        if (!seenDois.add(doi)) {
            logger.debug("Skipping duplicate DOI within harvest: {}", doi);
            return WorkResult.SKIPPED_DUPLICATE;
        }

        Optional<Publication> existing = publicationRepository.findByInstitutionIdAndDoi(institution.getId(), doi);

        Publication pub = existing.orElseGet(Publication::new);
        pub.setInstitution(institution);
        pub.setDoi(doi);
        pub.setTitle(extractTitle(work));
        pub.setPublicationYear(work.has("publication_year") ? work.get("publication_year").asInt() : null);

        String issn = extractIssn(work);
        pub.setJournalIssn(issn);
        pub.setJournalName(extractJournalName(work));

        OaStatus oaStatus = extractOaStatus(work);
        pub.setOaStatusOpenalex(oaStatus);
        if (pub.getOaStatusResolved() == null) {
            pub.setOaStatusResolved(oaStatus);
        }

        String oaUrl = extractOaUrl(work);
        if (oaUrl != null && !oaUrl.contains("doi.org")) {
            pub.setRepositoryUrl(oaUrl);
        }

        if (existing.isEmpty()) {
            pub.setSource(PublicationSource.OPENALEX);
        } else if (pub.getSource() == PublicationSource.REPO) {
            pub.setSource(PublicationSource.OPENALEX_REPO);
        }

        publicationRepository.save(pub);
        logger.debug("Saved publication with DOI: {}", doi);
        return existing.isEmpty() ? WorkResult.CREATED : WorkResult.UPDATED;
    }

    private String extractDoi(JsonNode work) {
        JsonNode doiNode = work.get("doi");
        if (doiNode == null || doiNode.isNull()) return null;
        String doi = doiNode.asText();
        if (doi.startsWith("https://doi.org/")) {
            doi = doi.substring("https://doi.org/".length());
        }
        return doi.toLowerCase();
    }

    private String extractTitle(JsonNode work) {
        JsonNode titleNode = work.get("title");
        return titleNode != null && !titleNode.isNull() ? titleNode.asText() : null;
    }

    private String extractIssn(JsonNode work) {
        JsonNode primaryLocation = work.get("primary_location");
        if (primaryLocation == null || primaryLocation.isNull()) return null;
        JsonNode source = primaryLocation.get("source");
        if (source == null || source.isNull()) return null;
        JsonNode issnNode = source.get("issn_l");
        if (issnNode != null && !issnNode.isNull()) return issnNode.asText();

        JsonNode issnArr = source.get("issn");
        if (issnArr != null && issnArr.isArray() && !issnArr.isEmpty()) {
            return issnArr.get(0).asText();
        }
        return null;
    }

    private String extractJournalName(JsonNode work) {
        JsonNode primaryLocation = work.get("primary_location");
        if (primaryLocation == null || primaryLocation.isNull()) return null;
        JsonNode source = primaryLocation.get("source");
        if (source == null || source.isNull()) return null;
        JsonNode displayName = source.get("display_name");
        return displayName != null && !displayName.isNull() ? displayName.asText() : null;
    }

    private OaStatus extractOaStatus(JsonNode work) {
        JsonNode openAccess = work.get("open_access");
        if (openAccess == null || openAccess.isNull()) return OaStatus.CLOSED;
        JsonNode oaStatusNode = openAccess.get("oa_status");
        if (oaStatusNode == null || oaStatusNode.isNull()) return OaStatus.CLOSED;

        return switch (oaStatusNode.asText().toLowerCase()) {
            case "diamond" -> OaStatus.DIAMOND;
            case "gold" -> OaStatus.GOLD;
            case "hybrid" -> OaStatus.HYBRID;
            case "bronze" -> OaStatus.BRONZE;
            case "green" -> OaStatus.GREEN;
            default -> OaStatus.CLOSED;
        };
    }

    private String extractOaUrl(JsonNode work) {
        JsonNode openAccess = work.get("open_access");
        if (openAccess == null || openAccess.isNull()) return null;
        JsonNode oaUrlNode = openAccess.get("oa_url");
        return oaUrlNode != null && !oaUrlNode.isNull() ? oaUrlNode.asText() : null;
    }
}

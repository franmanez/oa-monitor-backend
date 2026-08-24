package es.oamonitor.service;

import es.oamonitor.harvest.OaiPmhClient;
import es.oamonitor.harvest.OaiPmhClient.OaiPage;
import es.oamonitor.harvest.OaiPmhClient.OaiRecord;
import es.oamonitor.model.HarvestJob;
import es.oamonitor.model.Institution;
import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.HarvestStatus;
import es.oamonitor.model.enums.HarvestType;
import es.oamonitor.model.enums.OaStatus;
import es.oamonitor.model.enums.PublicationSource;
import es.oamonitor.repository.HarvestJobRepository;
import es.oamonitor.repository.InstitutionRepository;
import es.oamonitor.repository.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.kb.oai.pmh.ResumptionToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OaiPmhHarvestService {

    private static final Logger log = LoggerFactory.getLogger(OaiPmhHarvestService.class);
    private static final List<String> ARTICLE_TYPES = List.of(
        "article",
        "Article",
        "artículo",
        "article de revista",
        "journal article",
        "info:eu-repo/semantics/article",
        "info:eu-repo/semantics/Article",
        "info:eu-repo/semantics/contributionToPeriodical"
    );

    private final InstitutionRepository institutionRepository;
    private final PublicationRepository publicationRepository;
    private final HarvestJobRepository harvestJobRepository;
    private final OaiPmhClient oaiPmhClient;

    public OaiPmhHarvestService(InstitutionRepository institutionRepository,
                                 PublicationRepository publicationRepository,
                                 HarvestJobRepository harvestJobRepository,
                                 OaiPmhClient oaiPmhClient) {
        this.institutionRepository = institutionRepository;
        this.publicationRepository = publicationRepository;
        this.harvestJobRepository = harvestJobRepository;
        this.oaiPmhClient = oaiPmhClient;
    }

    @Transactional
    public HarvestJob harvestInstitution(UUID institutionId, int year) {
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found: " + institutionId));

        if (institution.getOaiEndpoint() == null || institution.getOaiEndpoint().isEmpty()) {
            throw new IllegalArgumentException("Institution has no OAI endpoint configured");
        }

        HarvestJob job = new HarvestJob();
        job.setInstitution(institution);
        job.setHarvestType(HarvestType.OAI_PMH);
        job.setStatus(HarvestStatus.RUNNING);
        job.setStartedAt(LocalDateTime.now());
        job.setRecordsFetched(0);
        job.setRecordsUpdated(0);
        job = harvestJobRepository.save(job);

        try {
            int[] counts = doHarvest(institution, year);

            job.setRecordsFetched(counts[0]);
            job.setRecordsUpdated(counts[1]);
            job.setStatus(HarvestStatus.COMPLETED);
        } catch (Exception e) {
            log.error("OAI-PMH harvest failed for institution {}: {}", institutionId, e.getMessage(), e);
            job.setStatus(HarvestStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setFinishedAt(LocalDateTime.now());
            harvestJobRepository.save(job);
        }

        return job;
    }

    private int[] doHarvest(Institution institution, int year) {
        UUID instId = institution.getId();
        String endpoint = institution.getOaiEndpoint();
        String metadataPrefix = institution.getOaiMetadataPrefix() != null ? institution.getOaiMetadataPrefix() : "oai_dc";

        int totalFetched = 0;
        int totalArticles = 0;
        int totalCreated = 0;
        int totalUpdated = 0;
        int totalSkipped = 0;
        int totalWrongYear = 0;
        ResumptionToken resumptionToken = null;
        int pageNum = 0;

        log.info("[OAI-PMH] Iniciando harvest para {}, año {}", institution.getName(), year);
        log.info("[OAI-PMH] Endpoint: {}", endpoint);

        do {
            pageNum++;
            log.debug("[OAI-PMH] Obteniendo página {}{}", pageNum, resumptionToken != null ? " (con resumptionToken)" : "");

            OaiPage page = oaiPmhClient.listRecords(endpoint, null, null, metadataPrefix, resumptionToken);

            log.debug("[OAI-PMH] Página {}: {} registros recibidos", pageNum, page.records().size());

            for (OaiRecord record : page.records()) {
                totalFetched++;

                if (totalFetched % 1000 == 0) {
                    log.info("[OAI-PMH] Progreso: {} registros procesados, {} artículos, {} creados, {} actualizados",
                            totalFetched, totalArticles, totalCreated, totalUpdated);
                }

                Integer pubYear = extractYear(record.dcDate());
                boolean isArticle = isArticle(record.dcType());
                boolean correctYear = pubYear != null && pubYear == year;

                log.info("[OAI-PMH] Registro #{}: dc:type={}, dc:date={}, identifiers={}, año={}",
                        totalFetched,
                        record.dcType() != null ? record.dcType() : "N/A",
                        record.dcDate() != null ? record.dcDate() : "N/A",
                        record.identifiers() != null ? record.identifiers() : "N/A",
                        pubYear != null ? pubYear : "N/A");

                if (!isArticle) {
                    totalSkipped++;
                    log.info("[OAI-PMH]   → Rechazado (tipo={} no es article)", record.dcType());
                    continue;
                }

                if (!correctYear) {
                    totalWrongYear++;
                    log.info("[OAI-PMH]   → Rechazado (año={} no coincide con {})", pubYear, year);
                    continue;
                }

                totalArticles++;
                log.info("[OAI-PMH]   → Aceptado (año={}, tipo=article)", pubYear);

                ProcessResult result = processRecord(record, institution);
                if (result == ProcessResult.CREATED) {
                    totalCreated++;
                    log.info("[OAI-PMH]   → Creada publicación (GREEN OA)");
                } else if (result == ProcessResult.UPDATED) {
                    totalUpdated++;
                    log.info("[OAI-PMH]   → Actualizada publicación existente");
                } else {
                    log.debug("[OAI-PMH]   → Sin cambios");
                }
            }

            resumptionToken = page.resumptionToken();
        } while (resumptionToken != null);

        institutionRepository.save(institution);

        log.info("[OAI-PMH] Harvest completado para {}", institution.getName());
        log.info("[OAI-PMH] Resumen: {} registros procesados, {} artículos año {}, {} creados, {} actualizados, {} rechazados tipo, {} rechazados año",
                totalFetched, totalArticles, year, totalCreated, totalUpdated, totalSkipped, totalWrongYear);

        return new int[]{totalFetched, totalUpdated};
    }

    private enum ProcessResult {
        CREATED, UPDATED, UNCHANGED
    }

    private boolean isArticle(String dcType) {
        if (dcType == null || dcType.isEmpty()) return false;
        String lower = dcType.toLowerCase().trim();
        for (String articleType : ARTICLE_TYPES) {
            if (lower.equals(articleType)) return true;
        }
        return false;
    }

    private ProcessResult processRecord(OaiRecord record, Institution institution) {
        UUID instId = institution.getId();
        
        // Extraer DOI, handle URL e ISSN según la institución
        String doi = null;
        String handleUrl = null;
        String issn = null;
        
        if ("UPC".equals(institution.getRebiunCode())) {
            // UPC: DOI, ISSN y handle en dc:identifier sin calificar
            for (String id : record.identifiers()) {
                if (doi == null && isDoi(id)) {
                    doi = id.toLowerCase();
                } else if (handleUrl == null && id.contains("hdl.handle.net/")) {
                    handleUrl = id;
                } else if (issn == null && isIssn(id)) {
                    issn = id;
                }
            }
        } else {
            // Lógica genérica para otras universidades
            for (String id : record.identifiers()) {
                if (doi == null && id.toLowerCase().contains("doi.org/")) {
                    String extracted = id.substring(id.toLowerCase().indexOf("doi.org/") + 8);
                    if (extracted.startsWith("/")) extracted = extracted.substring(1);
                    doi = extracted.toLowerCase();
                } else if (handleUrl == null && id.contains("hdl.handle.net/")) {
                    handleUrl = id;
                }
            }
        }

        if (doi == null && handleUrl == null) {
            return ProcessResult.UNCHANGED;
        }

        Optional<Publication> existing = Optional.empty();
        if (doi != null) {
            existing = publicationRepository.findByInstitutionIdAndDoi(instId, doi);
        } else if (handleUrl != null) {
            existing = publicationRepository.findByInstitutionIdAndRepositoryUrl(instId, handleUrl);
        }

        Integer pubYear = extractYear(record.dcDate());

        if (existing.isPresent()) {
            Publication pub = existing.get();
            boolean changed = false;
            StringBuilder changes = new StringBuilder();

            if (handleUrl != null) {
                pub.setRepositoryUrl(handleUrl);
                changed = true;
                changes.append("repositoryUrl ");
            }
            if (pub.getSource() == PublicationSource.OPENALEX) {
                pub.setSource(PublicationSource.OPENALEX_REPO);
                changed = true;
                changes.append("source ");
            }
            if (pub.getOaStatusResolved() != OaStatus.GREEN) {
                OaStatus previous = pub.getOaStatusResolved();
                pub.setOaStatusResolved(OaStatus.GREEN);
                changed = true;
                changes.append(previous != null ? previous + "→GREEN " : "→GREEN ");
            }
            if (record.title() != null && pub.getTitle() == null) {
                pub.setTitle(record.title());
                changed = true;
                changes.append("title ");
            }
            if (pubYear != null && pub.getPublicationYear() == null) {
                pub.setPublicationYear(pubYear);
                changed = true;
                changes.append("year ");
            }
            if ("UDG".equals(institution.getRebiunCode()) && record.source() != null && pub.getJournalName() == null) {
                pub.setJournalName(record.source());
                changed = true;
                changes.append("journalName ");
            }
            if (issn != null && pub.getJournalIssn() == null) {
                pub.setJournalIssn(issn);
                changed = true;
                changes.append("journalIssn ");
            }

            if (changed) {
                log.debug("[OAI-PMH] Actualizando {}: {}", doi != null ? "DOI " + doi : "handle " + handleUrl, changes.toString().trim());
                publicationRepository.save(pub);
                return ProcessResult.UPDATED;
            }
            return ProcessResult.UNCHANGED;
        } else {
            if (doi == null && handleUrl == null) {
                log.debug("[OAI-PMH] Saltando sin DOI ni handle");
                return ProcessResult.UNCHANGED;
            }

            Publication pub = new Publication();
            pub.setInstitution(institution);
            pub.setDoi(doi);
            pub.setTitle(record.title());
            pub.setPublicationYear(pubYear);
            pub.setRepositoryUrl(handleUrl);
            pub.setSource(PublicationSource.REPO);
            pub.setOaStatusResolved(OaStatus.GREEN);
            if ("UDG".equals(institution.getRebiunCode())) {
                pub.setJournalName(record.source());
            }
            pub.setJournalIssn(issn);

            publicationRepository.save(pub);
            log.info("[OAI-PMH] Creada publicación (GREEN) con doi={}, repositoryUrl={}, journal={}, issn={}",
                    doi, handleUrl, record.source(), issn);
            return ProcessResult.CREATED;
        }
    }

    private boolean isDoi(String value) {
        // Bare DOI: empieza con 10. seguido de 4+ dígitos y /
        return value.matches("^10\\.\\d{4,}/.*");
    }

    private boolean isIssn(String value) {
        // ISSN: XXXX-NNNN donde X es dígito y N es dígito o X
        return value.matches("^\\d{4}-\\d{3}[\\dXx]$");
    }

    private Integer extractYear(String dcDate) {
        if (dcDate == null || dcDate.isEmpty()) return null;
        try {
            String yearStr = dcDate.length() >= 4 ? dcDate.substring(0, 4) : dcDate;
            return Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

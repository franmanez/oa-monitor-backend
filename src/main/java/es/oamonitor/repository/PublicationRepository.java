package es.oamonitor.repository;

import es.oamonitor.model.Publication;
import es.oamonitor.model.enums.OaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, UUID> {

    Optional<Publication> findByInstitutionIdAndDoi(UUID institutionId, String doi);

    Page<Publication> findByInstitutionId(UUID institutionId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId")
    long countByInstitutionId(@Param("institutionId") UUID institutionId);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.oaStatusResolved = :status")
    long countByInstitutionIdAndOaStatusResolved(@Param("institutionId") UUID institutionId, @Param("status") OaStatus status);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.oaStatusResolved = :status AND p.publicationYear = :year")
    long countByInstitutionIdAndOaStatusResolvedAndPublicationYear(@Param("institutionId") UUID institutionId, @Param("status") OaStatus status, @Param("year") int year);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.publicationYear = :year")
    long countByInstitutionIdAndPublicationYear(@Param("institutionId") UUID institutionId, @Param("year") int year);

    @Query("SELECT p.oaStatusResolved, COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.publicationYear = :year GROUP BY p.oaStatusResolved")
    List<Object[]> statusCountByInstitutionAndYear(@Param("institutionId") UUID institutionId, @Param("year") int year);

    @Query("SELECT DISTINCT p.publicationYear FROM Publication p WHERE p.institution.id = :institutionId ORDER BY p.publicationYear DESC")
    List<Integer> findDistinctYearsByInstitutionId(@Param("institutionId") UUID institutionId);

    @Query("SELECT p FROM Publication p WHERE p.institution.id = :institutionId AND p.oaStatusResolved IN :statuses")
    List<Publication> findByInstitutionIdAndOaStatusResolvedIn(@Param("institutionId") UUID institutionId, @Param("statuses") List<OaStatus> statuses);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.foundInRepository = true")
    long countByInstitutionIdAndFoundInRepositoryTrue(@Param("institutionId") UUID institutionId);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.institution.id = :institutionId AND p.foundInRepository = true AND (:year IS NULL OR p.publicationYear = :year)")
    long countByInstitutionIdAndFoundInRepositoryTrueAndYear(@Param("institutionId") UUID institutionId, @Param("year") Integer year);

    @Query("SELECT p.institution.id, p.institution.name, COUNT(p), " +
           "SUM(CASE WHEN p.oaStatusResolved <> 'CLOSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.oaStatusResolved = 'CLOSED' THEN 1 ELSE 0 END) " +
           "FROM Publication p GROUP BY p.institution.id, p.institution.name")
    List<Object[]> aggregateByInstitution();

    @Query("SELECT p.oaStatusResolved, COUNT(p) FROM Publication p GROUP BY p.oaStatusResolved")
    List<Object[]> nationalStatusCounts();

    @Query("SELECT p FROM Publication p WHERE p.institution.id = :institutionId ORDER BY p.publicationYear DESC, p.title")
    List<Publication> findAllByInstitutionId(@Param("institutionId") UUID institutionId);

    @Query("SELECT p FROM Publication p WHERE p.institution.id = :institutionId AND (:year IS NULL OR p.publicationYear = :year) ORDER BY p.publicationYear DESC, p.title")
    List<Publication> findAllByInstitutionIdAndYear(@Param("institutionId") UUID institutionId, @Param("year") Integer year);

    @Query("SELECT p FROM Publication p WHERE p.institution.id = :institutionId " +
           "AND (:year IS NULL OR p.publicationYear = :year) " +
           "AND (:status IS NULL OR p.oaStatusResolved = :status) " +
           "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.doi) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.journalName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.publicationYear DESC, p.title")
    Page<Publication> findFiltered(@Param("institutionId") UUID institutionId,
                                   @Param("year") Integer year,
                                   @Param("status") OaStatus status,
                                   @Param("search") String search,
                                   Pageable pageable);

    @Query("SELECT COUNT(p) FROM Publication p")
    long nationalTotalPublications();

    @Query("SELECT p.institution.id, p.institution.name, COUNT(p), " +
           "SUM(CASE WHEN p.oaStatusResolved <> 'CLOSED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.oaStatusResolved = 'CLOSED' THEN 1 ELSE 0 END) " +
           "FROM Publication p WHERE p.publicationYear = :year " +
           "GROUP BY p.institution.id, p.institution.name")
    List<Object[]> aggregateByInstitutionAndYear(@Param("year") int year);

    @Query("SELECT DISTINCT p.publicationYear FROM Publication p ORDER BY p.publicationYear DESC")
    List<Integer> findDistinctYearsAll();

    @Query("SELECT p.journalName, p.journalIssn, COUNT(p), COALESCE(SUM(p.apcEstimatedEur), 0) " +
           "FROM Publication p WHERE p.institution.id = :institutionId " +
           "AND p.oaStatusResolved IN ('GOLD', 'HYBRID') " +
           "AND p.apcEstimatedEur IS NOT NULL " +
           "AND (:year IS NULL OR p.publicationYear = :year) " +
           "GROUP BY p.journalName, p.journalIssn " +
           "ORDER BY SUM(p.apcEstimatedEur) DESC")
    List<Object[]> journalApcByInstitution(@Param("institutionId") UUID institutionId, @Param("year") Integer year);

    @Query("SELECT p.journalName, p.journalIssn, COUNT(p) " +
           "FROM Publication p WHERE p.institution.id = :institutionId " +
           "AND (:year IS NULL OR p.publicationYear = :year) " +
           "GROUP BY p.journalName, p.journalIssn " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> topJournalsByInstitution(@Param("institutionId") UUID institutionId, @Param("year") Integer year);

    @Query("SELECT p.institution.id, p.institution.name, COUNT(p), " +
           "SUM(CASE WHEN p.oaStatusResolved <> 'CLOSED' THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(p.apcEstimatedEur), 0), " +
           "SUM(CASE WHEN p.apcEstimatedEur IS NOT NULL THEN 1 ELSE 0 END) " +
           "FROM Publication p WHERE p.publicationYear = :year " +
           "GROUP BY p.institution.id, p.institution.name")
    List<Object[]> aggregateInstitutionScatter(@Param("year") int year);
}

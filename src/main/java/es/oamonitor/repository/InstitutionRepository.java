package es.oamonitor.repository;

import es.oamonitor.model.Institution;
import es.oamonitor.model.enums.OaiStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {

    Optional<Institution> findByRorId(String rorId);

    boolean existsByRorId(String rorId);

    Page<Institution> findByOaiStatus(OaiStatus oaiStatus, Pageable pageable);
}

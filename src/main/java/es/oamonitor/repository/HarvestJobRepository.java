package es.oamonitor.repository;

import es.oamonitor.model.HarvestJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface HarvestJobRepository extends JpaRepository<HarvestJob, UUID> {

    Page<HarvestJob> findByInstitutionIdOrderByStartedAtDesc(UUID institutionId, Pageable pageable);
}

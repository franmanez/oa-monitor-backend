package es.oamonitor.repository;

import es.oamonitor.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, UUID> {

    Optional<Institution> findByRorId(String rorId);

    boolean existsByRorId(String rorId);
}

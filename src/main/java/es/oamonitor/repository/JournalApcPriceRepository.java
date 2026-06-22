package es.oamonitor.repository;

import es.oamonitor.model.JournalApcPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalApcPriceRepository extends JpaRepository<JournalApcPrice, String> {
}

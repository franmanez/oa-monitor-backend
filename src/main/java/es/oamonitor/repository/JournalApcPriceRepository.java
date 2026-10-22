package es.oamonitor.repository;

import es.oamonitor.model.JournalApcPrice;
import es.oamonitor.model.JournalApcPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalApcPriceRepository extends JpaRepository<JournalApcPrice, JournalApcPriceId> {

    Optional<JournalApcPrice> findByIssnAndYear(String issn, Integer year);

    @Query("SELECT j FROM JournalApcPrice j WHERE j.issn = :issn ORDER BY j.year DESC")
    List<JournalApcPrice> findAllByIssnOrderByYearDesc(@Param("issn") String issn);
}

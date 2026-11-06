package es.oamonitor.controller;

import es.oamonitor.dto.HistoricalDataDto;
import es.oamonitor.dto.JournalApcDto;
import es.oamonitor.service.HistoricalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/institutions")
public class HistoricalController {

    private final HistoricalService historicalService;

    public HistoricalController(HistoricalService historicalService) {
        this.historicalService = historicalService;
    }

    @GetMapping("/{id}/historical")
    public ResponseEntity<HistoricalDataDto> getHistoricalData(@PathVariable UUID id) {
        return historicalService.getHistoricalData(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/historical/years")
    public List<Integer> getAvailableYears(@PathVariable UUID id) {
        return historicalService.getAvailableYears(id);
    }

    @GetMapping("/{id}/historical/journals/apc")
    public List<JournalApcDto> getJournalsByApcForYear(
            @PathVariable UUID id,
            @RequestParam int year) {
        return historicalService.getJournalsByApcForYear(id, year);
    }
}

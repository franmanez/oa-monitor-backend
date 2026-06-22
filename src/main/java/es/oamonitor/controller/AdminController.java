package es.oamonitor.controller;

import es.oamonitor.dto.ApcImportResult;
import es.oamonitor.service.ApcImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ApcImportService apcImportService;

    public AdminController(ApcImportService apcImportService) {
        this.apcImportService = apcImportService;
    }

    @PostMapping("/apc/import")
    public ResponseEntity<ApcImportResult> importApc() {
        ApcImportResult result = apcImportService.importFromOpenApc();
        return ResponseEntity.ok(result);
    }
}

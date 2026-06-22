package es.oamonitor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/meta")
public class MetaController {

    @GetMapping("/live")
    public ResponseEntity<String> live() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/ready")
    public ResponseEntity<ReadyResponse> ready() {
        return ResponseEntity.ok(new ReadyResponse("UP", LocalDateTime.now()));
    }

    public record ReadyResponse(String status, LocalDateTime timestamp) {}
}

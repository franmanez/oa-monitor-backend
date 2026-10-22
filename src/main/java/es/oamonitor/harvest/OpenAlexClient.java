package es.oamonitor.harvest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OpenAlexClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAlexClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openalex.email}")
    private String email;

    @Value("${openalex.api-key:}")
    private String apiKey;

    @Value("${openalex.base-url}")
    private String baseUrl;

    @Value("${openalex.per-page}")
    private int perPage;

    public OpenAlexClient(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchWorks(String rorId, int publicationYear, String cursor) {
        String filter = "institutions.ror:" + rorId + ",publication_year:" + publicationYear + ",type:article";

        StringBuilder url = new StringBuilder(baseUrl + "/works")
                .append("?filter=").append(URLEncoder.encode(filter, StandardCharsets.UTF_8))
                .append("&per-page=").append(perPage)
                .append("&mailto=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));

        if (cursor != null && !cursor.isEmpty()) {
            url.append("&cursor=").append(URLEncoder.encode(cursor, StandardCharsets.UTF_8));
        }

        logger.info("OpenAlex URL: {}", url);

        try {
            URI uri = URI.create(url.toString());
            
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching from OpenAlex: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch works from OpenAlex for ROR " + rorId, e);
        }
    }
}

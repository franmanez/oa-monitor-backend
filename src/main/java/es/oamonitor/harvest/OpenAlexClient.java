package es.oamonitor.harvest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OpenAlexClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openalex.email}")
    private String email;

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

        try {
            URI uri = URI.create(url.toString());
            String json = restTemplate.getForObject(uri, String.class);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch works from OpenAlex for ROR " + rorId, e);
        }
    }
}

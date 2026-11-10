package com.bbr.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/** The only class that talks to the Banana API (adapter). If the API changes, only this file changes. */
@Component
public class BananaClient {

    /** What the Banana API sends back: an image URL and the number in it. */
    public record BananaPuzzle(String question, int solution) {}

    private static final Logger log = LoggerFactory.getLogger(BananaClient.class);

    private final RestClient http;
    private final ObjectMapper json;
    private final String url;

    public BananaClient(RestClient http, ObjectMapper json, @Value("${app.banana.url}") String url) {
        this.http = http;
        this.json = json;
        this.url = url;
    }

    /** Interoperability: Java calls the PHP Banana API over HTTP and reads its JSON. */
    public BananaPuzzle fetch() {
        try {
            String body = http.get().uri(url).retrieve().body(String.class);   // read as text, then parse
            BananaPuzzle puzzle = json.readValue(body, BananaPuzzle.class);
            if (puzzle.question() == null || puzzle.question().isBlank()) {
                throw new IllegalStateException("No image in the reply");
            }
            return puzzle;
        } catch (Exception e) {
            log.warn("Banana API failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Puzzle service is busy, try again");
        }
    }
}

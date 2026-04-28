package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private final RestTemplate restTemplate;

    @Value("${webhook.url}")
    private String webhookUrl;

    @Async
    public void sendWebhook(String eventType, Object payload) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventType", eventType);
            body.put("occurredAt", LocalDateTime.now().toString());
            body.put("data", payload);

            restTemplate.postForObject(webhookUrl, body, String.class);
            log.info("Webhook sent successfully for event: {}", eventType);
        } catch (Exception e) {
            log.error("Webhook delivery failed for event {}: {}", eventType, e.getMessage());
        }
    }

}
package org.example.uberbookingservice.services.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.entities.IdempotencyRecord;
import org.example.uberbookingservice.repositories.IdempotencyRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <T> ResponseEntity<T> execute(String operation, String idempotencyKey, Supplier<ResponseEntity<T>> supplier, Class<T> responseType) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return supplier.get();
        }

        return idempotencyRecordRepository.findByOperationAndIdempotencyKey(operation, idempotencyKey)
                .map(record -> ResponseEntity.status(record.getResponseStatus()).body(deserialize(record.getResponseBody(), responseType)))
                .orElseGet(() -> {
                    ResponseEntity<T> response = supplier.get();
                    persist(operation, idempotencyKey, response);
                    return response;
                });
    }

    private <T> void persist(String operation, String idempotencyKey, ResponseEntity<T> response) {
        try {
            idempotencyRecordRepository.save(IdempotencyRecord.builder()
                    .operation(operation)
                    .idempotencyKey(idempotencyKey)
                    .responseBody(objectMapper.writeValueAsString(response.getBody()))
                    .responseStatus(response.getStatusCode().value())
                    .build());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to persist idempotent response", exception);
        }
    }

    private <T> T deserialize(String payload, Class<T> responseType) {
        try {
            return objectMapper.readValue(payload, responseType);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize idempotent response", exception);
        }
    }
}

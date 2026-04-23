package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.dto.BookingAuditLogDto;
import org.example.uberbookingservice.entities.BookingAuditLog;
import org.example.uberbookingservice.repositories.BookingAuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingAuditService {

    private final BookingAuditLogRepository bookingAuditLogRepository;

    public void log(UUID bookingId, String action, String message, String actorType, UUID actorId) {
        bookingAuditLogRepository.save(BookingAuditLog.builder()
                .bookingId(bookingId)
                .action(action)
                .message(message)
                .actorType(actorType)
                .actorId(actorId)
                .build());
    }

    public List<BookingAuditLogDto> getAuditTrail(UUID bookingId) {
        return bookingAuditLogRepository.findAllByBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(log -> BookingAuditLogDto.builder()
                        .id(log.getId())
                        .bookingId(log.getBookingId())
                        .action(log.getAction())
                        .message(log.getMessage())
                        .actorType(log.getActorType())
                        .actorId(log.getActorId())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();
    }
}

package org.example.uberbookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingAuditLogDto {
    private UUID id;
    private UUID bookingId;
    private String action;
    private String message;
    private String actorType;
    private UUID actorId;
    private LocalDateTime createdAt;
}

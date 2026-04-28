package org.example.uberbookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID notificationId;
    private UUID recipientId;
    private UUID bookingId;
    private String eventType;
    private String title;
    private String message;
    private Boolean delivered;
    private LocalDateTime createdAt;
}

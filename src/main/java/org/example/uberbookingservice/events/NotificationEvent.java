package org.example.uberbookingservice.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationEvent {
    private final UUID notificationId;
    private final UUID recipientId;
    private final UUID bookingId;
    private final String eventType;
    private final String title;
    private final String message;
    private final String source;
    private final LocalDateTime occurredAt;
}

package org.example.uberbookingservice.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BookingLifecycleEvent {
    private final UUID bookingId;
    private final UUID passengerId;
    private final UUID driverId;
    private final String eventType;
    private final String bookingStatus;
    private final String source;
    private final String message;
    private final LocalDateTime occurredAt;
}

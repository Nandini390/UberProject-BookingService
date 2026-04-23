package org.example.uberbookingservice.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentLifecycleEvent {
    private final UUID paymentId;
    private final UUID bookingId;
    private final UUID passengerId;
    private final UUID driverId;
    private final String paymentMethod;
    private final String paymentStatus;
    private final Double amount;
    private final String currency;
    private final String eventType;
    private final String source;
    private final String providerReference;
    private final String message;
    private final LocalDateTime occurredAt;
}

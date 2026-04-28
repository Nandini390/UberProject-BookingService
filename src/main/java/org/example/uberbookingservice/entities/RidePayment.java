package org.example.uberbookingservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.uberbookingservice.payments.PaymentMethod;
import org.example.uberbookingservice.payments.PaymentReconciliationStatus;
import org.example.uberbookingservice.payments.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ride_payments", uniqueConstraints = @UniqueConstraint(name = "uk_ride_payment_booking", columnNames = "booking_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RidePayment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "actual_distance_meters")
    private Long actualDistanceMeters;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(length = 100)
    private String providerReference;

    @Column(length = 50)
    private String providerName;

    @Column(length = 100)
    private String gatewayTransactionId;

    @Column(length = 255)
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentReconciliationStatus reconciliationStatus;

    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime refundedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

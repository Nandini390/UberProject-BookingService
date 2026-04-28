package org.example.uberbookingservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.uberbookingservice.dispatch.DispatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_dispatch", uniqueConstraints = @UniqueConstraint(name = "uk_booking_dispatch_booking", columnNames = "booking_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDispatch {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Lob
    @Column(name = "candidate_driver_ids")
    private String candidateDriverIds;

    @Lob
    @Column(name = "rejected_driver_ids")
    private String rejectedDriverIds;

    @Column(name = "current_driver_id")
    private UUID currentDriverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", nullable = false, length = 40)
    private DispatchStatus dispatchStatus;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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

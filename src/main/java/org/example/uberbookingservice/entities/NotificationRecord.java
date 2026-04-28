package org.example.uberbookingservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID recipientId;

    @Column
    private UUID bookingId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Boolean delivered;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (delivered == null) {
            delivered = Boolean.FALSE;
        }
    }
}

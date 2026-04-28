package org.example.uberbookingservice.repositories;

import org.example.uberbookingservice.entities.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, UUID> {
    List<NotificationRecord> findTop50ByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}

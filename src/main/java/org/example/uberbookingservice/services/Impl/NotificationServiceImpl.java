package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.dto.NotificationDto;
import org.example.uberbookingservice.entities.NotificationRecord;
import org.example.uberbookingservice.events.NotificationEvent;
import org.example.uberbookingservice.repositories.NotificationRecordRepository;
import org.example.uberbookingservice.services.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRecordRepository notificationRecordRepository;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public void notifyUser(UUID recipientId, UUID bookingId, String eventType, String title, String message) {
        if (recipientId == null) {
            return;
        }
        NotificationRecord savedNotification = notificationRecordRepository.save(NotificationRecord.builder()
                .recipientId(recipientId)
                .bookingId(bookingId)
                .eventType(eventType)
                .title(title)
                .message(message)
                .delivered(Boolean.TRUE)
                .build());

        kafkaProducerService.publishNotificationEvent(NotificationEvent.builder()
                .notificationId(savedNotification.getId())
                .recipientId(recipientId)
                .bookingId(bookingId)
                .eventType(eventType)
                .title(title)
                .message(message)
                .source("UberBookingService")
                .occurredAt(LocalDateTime.now())
                .build());
    }

    @Override
    public List<NotificationDto> getNotifications(UUID recipientId) {
        return notificationRecordRepository.findTop50ByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(notification -> NotificationDto.builder()
                        .notificationId(notification.getId())
                        .recipientId(notification.getRecipientId())
                        .bookingId(notification.getBookingId())
                        .eventType(notification.getEventType())
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .delivered(notification.getDelivered())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }
}

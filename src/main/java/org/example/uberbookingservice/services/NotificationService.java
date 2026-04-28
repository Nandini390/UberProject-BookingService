package org.example.uberbookingservice.services;

import org.example.uberbookingservice.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void notifyUser(UUID recipientId, UUID bookingId, String eventType, String title, String message);

    List<NotificationDto> getNotifications(UUID recipientId);
}

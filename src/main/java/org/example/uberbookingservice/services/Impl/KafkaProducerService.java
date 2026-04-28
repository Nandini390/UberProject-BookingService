package org.example.uberbookingservice.services.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.events.BookingLifecycleEvent;
import org.example.uberbookingservice.events.NotificationEvent;
import org.example.uberbookingservice.events.PaymentLifecycleEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    public static final String BOOKING_LIFECYCLE_TOPIC = "booking.lifecycle";
    public static final String PAYMENT_LIFECYCLE_TOPIC = "payment.lifecycle";
    public static final String NOTIFICATION_LIFECYCLE_TOPIC = "notification.lifecycle";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishBookingLifecycleEvent(BookingLifecycleEvent event) {
        try {
            kafkaTemplate.send(BOOKING_LIFECYCLE_TOPIC, event.getBookingId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish booking lifecycle event", exception);
        }
    }

    public void publishPaymentLifecycleEvent(PaymentLifecycleEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_LIFECYCLE_TOPIC, event.getBookingId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish payment lifecycle event", exception);
        }
    }

    public void publishNotificationEvent(NotificationEvent event) {
        try {
            kafkaTemplate.send(NOTIFICATION_LIFECYCLE_TOPIC, event.getRecipientId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to publish notification event", exception);
        }
    }
}

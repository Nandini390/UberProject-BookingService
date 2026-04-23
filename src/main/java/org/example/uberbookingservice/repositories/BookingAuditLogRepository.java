package org.example.uberbookingservice.repositories;

import org.example.uberbookingservice.entities.BookingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingAuditLogRepository extends JpaRepository<BookingAuditLog, UUID> {
    List<BookingAuditLog> findAllByBookingIdOrderByCreatedAtAsc(UUID bookingId);
}

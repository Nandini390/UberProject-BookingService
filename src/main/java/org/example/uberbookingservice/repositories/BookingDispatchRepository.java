package org.example.uberbookingservice.repositories;

import org.example.uberbookingservice.dispatch.DispatchStatus;
import org.example.uberbookingservice.entities.BookingDispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingDispatchRepository extends JpaRepository<BookingDispatch, UUID> {
    Optional<BookingDispatch> findByBookingId(UUID bookingId);

    List<BookingDispatch> findAllByDispatchStatusAndExpiresAtBefore(DispatchStatus dispatchStatus, LocalDateTime expiresAt);
}

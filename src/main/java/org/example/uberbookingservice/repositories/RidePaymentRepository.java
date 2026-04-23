package org.example.uberbookingservice.repositories;

import org.example.uberbookingservice.entities.RidePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RidePaymentRepository extends JpaRepository<RidePayment, UUID> {
    Optional<RidePayment> findByBookingId(UUID bookingId);
}

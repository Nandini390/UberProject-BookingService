package org.example.uberbookingservice.repositories;

import org.example.uberbookingservice.entities.TripOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TripOtpRepository extends JpaRepository<TripOtp, UUID> {
    Optional<TripOtp> findOtpByBookingIdOrderByCreatedAtDesc(UUID bookingId);
    Optional<TripOtp> findByBookingId(UUID bookingId);
}

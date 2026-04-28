package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberbookingservice.payments.PaymentStatus;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;
import org.example.uberprojectentityservice.Models.ExactLocation;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {
    private UUID bookingId;
    private BookingStatus bookingStatus;
    private Optional<Driver> driver;
    private UUID passengerId;
    private ExactLocation startLocation;
    private ExactLocation endLocation;
    private Long totalDistanceMeters;
    private Long actualDistanceMeters;
    private Double estimatedFare;
    private Double finalFare;
    private Double cancellationCharge;
    private PaymentStatus paymentStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TripOtpResponseDto otp;
}

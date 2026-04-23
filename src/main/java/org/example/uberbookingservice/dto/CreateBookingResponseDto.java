package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberprojectentityservice.Models.ExactLocation;
import org.example.uberprojectentityservice.Models.Driver;
import org.example.uberbookingservice.payments.PaymentStatus;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingResponseDto {
    private UUID bookingId;
    private String bookingStatus;
    private Optional<Driver> driver;
    private UUID passengerId;
    private ExactLocation startLocation;
    private ExactLocation endLocation;
    private Long totalDistanceMeters;
    private Double estimatedFare;
    private PaymentStatus paymentStatus;
}

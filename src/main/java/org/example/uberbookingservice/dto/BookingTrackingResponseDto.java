package org.example.uberbookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.ExactLocation;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTrackingResponseDto {
    private UUID bookingId;
    private BookingStatus bookingStatus;
    private UUID driverId;
    private UUID passengerId;
    private ExactLocation pickupLocation;
    private ExactLocation dropLocation;
    private DriverTrackingDto driverLocation;
    private Long estimatedTripDistanceMeters;
    private Long actualTripDistanceMeters;
    private Double estimatedFare;
    private Double finalFare;
    private Double remainingDistanceMeters;
    private Long lastDriverUpdateAtEpochMs;
}

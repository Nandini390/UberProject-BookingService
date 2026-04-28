package org.example.uberbookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTripMetricsDto {
    private String bookingId;
    private String driverId;
    private String trackingStage;
    private Long actualDistanceMeters;
    private Double currentLatitude;
    private Double currentLongitude;
    private Long lastUpdatedAtEpochMs;
}

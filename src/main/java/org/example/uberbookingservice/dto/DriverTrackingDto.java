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
public class DriverTrackingDto {
    private String driverId;
    private Double latitude;
    private Double longitude;
    private Boolean online;
    private String bookingId;
    private String trackingStage;
    private Long lastUpdatedAtEpochMs;
}

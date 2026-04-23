package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberprojectentityservice.Models.ExactLocation;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {
    private UUID passengerId;
    private ExactLocation startLocation;
    private ExactLocation endLocation;
    private List<UUID> driverIds;
    private UUID bookingId;
}

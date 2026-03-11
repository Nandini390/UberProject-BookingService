package org.example.uberbookingservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDto {
    private Long passengerId;
//    private ExactLocation startLocation;
//    private ExactLocation endLocation;
    private List<Long> driverIds;
    private Long bookingId;
}

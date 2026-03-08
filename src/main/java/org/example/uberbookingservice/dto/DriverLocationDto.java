package org.example.uberbookingservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriverLocationDto {
     String driverId;
     Double latitude;
     Double longitude;
}

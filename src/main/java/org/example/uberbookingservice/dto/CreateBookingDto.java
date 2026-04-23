package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.uberprojectentityservice.Models.ExactLocation;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {
    @NotNull(message = "passengerId is required")
    private UUID passengerId;
    @NotNull(message = "startLocation is required")
    private ExactLocation startLocation;
    @NotNull(message = "endLocation is required")
    private ExactLocation endLocation;
}

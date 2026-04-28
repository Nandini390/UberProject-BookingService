package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDecisionRequestDto {
    @NotNull(message = "driverId is required")
    private UUID driverId;

    @Size(max = 255, message = "reason must be at most 255 characters")
    private String reason;
}

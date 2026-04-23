package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingRequestDto {
    private String bookingStatus;
    private UUID driverId;
    @Size(max = 255, message = "cancellationReason must be at most 255 characters")
    private String cancellationReason;
}

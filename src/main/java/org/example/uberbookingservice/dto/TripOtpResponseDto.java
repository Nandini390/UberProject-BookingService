package org.example.uberbookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripOtpResponseDto {
    private UUID bookingId;
    private String code;
    private Boolean verified;
    private LocalDateTime expiresAt;
}

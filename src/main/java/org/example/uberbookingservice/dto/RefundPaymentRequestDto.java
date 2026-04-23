package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundPaymentRequestDto {

    @Size(max = 255, message = "reason must be at most 255 characters")
    private String reason;
}

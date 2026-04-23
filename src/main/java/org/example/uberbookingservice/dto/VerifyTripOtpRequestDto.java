package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTripOtpRequestDto {
    @NotBlank(message = "otp code is required")
    @Size(min = 4, max = 10, message = "otp code length is invalid")
    private String code;
}

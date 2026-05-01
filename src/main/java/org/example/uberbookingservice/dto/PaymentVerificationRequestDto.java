package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class PaymentVerificationRequestDto {
    @NotBlank(message = "razorpayPaymentId is required")
    @Size(max = 100, message = "razorpayPaymentId must be at most 100 characters")
    private String razorpayPaymentId;

    @NotBlank(message = "razorpayOrderId is required")
    @Size(max = 100, message = "razorpayOrderId must be at most 100 characters")
    private String razorpayOrderId;

    @NotBlank(message = "razorpaySignature is required")
    @Size(max = 255, message = "razorpaySignature must be at most 255 characters")
    private String razorpaySignature;
}

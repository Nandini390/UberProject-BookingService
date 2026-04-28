package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.uberbookingservice.payments.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReconciliationRequestDto {
    @NotBlank(message = "providerReference is required")
    private String providerReference;

    @Size(max = 100, message = "gatewayTransactionId must be at most 100 characters")
    private String gatewayTransactionId;

    @NotNull(message = "paymentStatus is required")
    private PaymentStatus paymentStatus;

    @Size(max = 255, message = "message must be at most 255 characters")
    private String message;
}

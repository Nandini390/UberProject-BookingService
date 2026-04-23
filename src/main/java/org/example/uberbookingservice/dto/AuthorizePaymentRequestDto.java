package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.uberbookingservice.payments.PaymentMethod;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizePaymentRequestDto {

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    @Positive(message = "amount must be positive")
    private Double amount;

    @Size(max = 10, message = "currency must be at most 10 characters")
    private String currency;
}

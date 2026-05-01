package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberbookingservice.payments.PaymentMethod;
import org.example.uberbookingservice.payments.PaymentReconciliationStatus;
import org.example.uberbookingservice.payments.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private UUID paymentId;
    private UUID bookingId;
    private Double amount;
    private Long actualDistanceMeters;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String providerReference;
    private String providerName;
    private String gatewayTransactionId;
    private String checkoutKey;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private Integer amountInSubunits;
    private PaymentReconciliationStatus reconciliationStatus;
    private String failureReason;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

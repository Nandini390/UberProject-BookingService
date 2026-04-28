package org.example.uberbookingservice.services;

import org.example.uberbookingservice.dto.AuthorizePaymentRequestDto;
import org.example.uberbookingservice.dto.BookingAuditLogDto;
import org.example.uberbookingservice.dto.PaymentReconciliationRequestDto;
import org.example.uberbookingservice.dto.PaymentResponseDto;

import java.util.UUID;

public interface PaymentService {
    PaymentResponseDto authorizePayment(UUID bookingId, AuthorizePaymentRequestDto requestDto);

    PaymentResponseDto capturePayment(UUID bookingId, Double finalAmount, Long actualDistanceMeters);

    PaymentResponseDto refundPayment(UUID bookingId, String reason);

    PaymentResponseDto getPayment(UUID bookingId);

    PaymentResponseDto reconcilePayment(UUID bookingId, PaymentReconciliationRequestDto requestDto);
}

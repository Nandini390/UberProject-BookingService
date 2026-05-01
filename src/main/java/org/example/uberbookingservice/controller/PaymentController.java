package org.example.uberbookingservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.dto.AuthorizePaymentRequestDto;
import org.example.uberbookingservice.dto.PaymentReconciliationRequestDto;
import org.example.uberbookingservice.dto.PaymentResponseDto;
import org.example.uberbookingservice.dto.PaymentVerificationRequestDto;
import org.example.uberbookingservice.dto.RefundPaymentRequestDto;
import org.example.uberbookingservice.services.Impl.IdempotencyService;
import org.example.uberbookingservice.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/booking/{bookingId}/authorize")
    public ResponseEntity<PaymentResponseDto> authorizePayment(
            @PathVariable UUID bookingId,
            @Valid @RequestBody AuthorizePaymentRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return idempotencyService.execute(
                "PAYMENT_AUTHORIZE_" + bookingId,
                idempotencyKey,
                () -> new ResponseEntity<>(paymentService.authorizePayment(bookingId, requestDto), HttpStatus.CREATED),
                PaymentResponseDto.class
        );
    }

    @PostMapping("/booking/{bookingId}/capture")
    public ResponseEntity<PaymentResponseDto> capturePayment(@PathVariable UUID bookingId,
                                                             @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return idempotencyService.execute(
                "PAYMENT_CAPTURE_" + bookingId,
                idempotencyKey,
                () -> ResponseEntity.ok(paymentService.capturePayment(bookingId, null, null)),
                PaymentResponseDto.class
        );
    }

    @PostMapping("/booking/{bookingId}/verify")
    public ResponseEntity<PaymentResponseDto> verifyRazorpayPayment(
            @PathVariable UUID bookingId,
            @Valid @RequestBody PaymentVerificationRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return idempotencyService.execute(
                "PAYMENT_VERIFY_" + bookingId,
                idempotencyKey,
                () -> ResponseEntity.ok(paymentService.verifyRazorpayPayment(bookingId, requestDto)),
                PaymentResponseDto.class
        );
    }

    @PostMapping("/booking/{bookingId}/refund")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable UUID bookingId,
            @Valid @RequestBody(required = false) RefundPaymentRequestDto requestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        String reason = requestDto != null ? requestDto.getReason() : null;
        return idempotencyService.execute(
                "PAYMENT_REFUND_" + bookingId,
                idempotencyKey,
                () -> ResponseEntity.ok(paymentService.refundPayment(bookingId, reason)),
                PaymentResponseDto.class
        );
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(paymentService.getPayment(bookingId));
    }

    @PostMapping("/booking/{bookingId}/reconcile")
    public ResponseEntity<PaymentResponseDto> reconcilePayment(@PathVariable UUID bookingId,
                                                               @Valid @RequestBody PaymentReconciliationRequestDto requestDto) {
        return ResponseEntity.ok(paymentService.reconcilePayment(bookingId, requestDto));
    }
}

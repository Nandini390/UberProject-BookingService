package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.dto.AuthorizePaymentRequestDto;
import org.example.uberbookingservice.dto.PaymentResponseDto;
import org.example.uberbookingservice.entities.RidePayment;
import org.example.uberbookingservice.events.PaymentLifecycleEvent;
import org.example.uberbookingservice.payments.PaymentMethod;
import org.example.uberbookingservice.payments.PaymentStatus;
import org.example.uberbookingservice.repositories.BookingRepository;
import org.example.uberbookingservice.repositories.RidePaymentRepository;
import org.example.uberbookingservice.services.PaymentService;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String DEFAULT_CURRENCY = "INR";
    private static final double BASE_FARE = 60.0;
    private static final double PER_KM_FARE = 14.0;

    private final RidePaymentRepository ridePaymentRepository;
    private final BookingRepository bookingRepository;
    private final KafkaProducerService kafkaProducerService;
    private final BookingAuditService bookingAuditService;

    @Override
    @Transactional
    public PaymentResponseDto authorizePayment(UUID bookingId, AuthorizePaymentRequestDto requestDto) {
        Booking booking = getBooking(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot authorize payment for a cancelled booking");
        }
        RidePayment payment = ridePaymentRepository.findByBookingId(bookingId)
                .orElseGet(() -> RidePayment.builder()
                        .bookingId(bookingId)
                        .paymentMethod(requestDto.getPaymentMethod())
                        .currency(resolveCurrency(requestDto.getCurrency()))
                        .status(PaymentStatus.PENDING)
                        .providerReference(generateProviderReference(bookingId))
                        .build());

        if (payment.getStatus() == PaymentStatus.CAPTURED || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Payment cannot be re-authorized in status: " + payment.getStatus());
        }
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setCurrency(resolveCurrency(requestDto.getCurrency()));
        payment.setAmount(resolveAmount(booking, requestDto.getAmount()));
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setAuthorizedAt(LocalDateTime.now());
        payment.setFailureReason(null);

        RidePayment savedPayment = ridePaymentRepository.save(payment);
        publishPaymentEvent(booking, savedPayment, "PAYMENT_AUTHORIZED", "Payment authorized successfully");
        bookingAuditService.log(bookingId, "PAYMENT_AUTHORIZED", "Payment authorized for booking", "PASSENGER", booking.getPassenger() != null ? booking.getPassenger().getId() : null);
        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto capturePayment(UUID bookingId) {
        Booking booking = getBooking(bookingId);

        RidePayment payment = ridePaymentRepository.findByBookingId(bookingId)
                .orElseGet(() -> createCapturedCashPayment(booking));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Refunded payment cannot be captured again");
        }
        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return mapToResponse(payment);
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(LocalDateTime.now());
        payment.setFailureReason(null);
        RidePayment savedPayment = ridePaymentRepository.save(payment);
        publishPaymentEvent(booking, savedPayment, "PAYMENT_CAPTURED", "Payment captured successfully");
        bookingAuditService.log(bookingId, "PAYMENT_CAPTURED", "Payment captured for booking", "SYSTEM",
                booking.getPassenger() != null ? booking.getPassenger().getId() : null);
        return mapToResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto refundPayment(UUID bookingId, String reason) {
        Booking booking = getBooking(bookingId);

        RidePayment payment = ridePaymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("No payment exists for booking " + bookingId));

        if (payment.getStatus() != PaymentStatus.AUTHORIZED && payment.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Only authorized or captured payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setFailureReason(reason);
        RidePayment savedPayment = ridePaymentRepository.save(payment);
        String resolvedReason = reason == null || reason.isBlank() ? "Payment refunded" : reason;
        publishPaymentEvent(booking, savedPayment, "PAYMENT_REFUNDED", resolvedReason);
        bookingAuditService.log(bookingId, "PAYMENT_REFUNDED", resolvedReason, "SYSTEM",
                booking.getPassenger() != null ? booking.getPassenger().getId() : null);
        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponseDto getPayment(UUID bookingId) {
        RidePayment payment = ridePaymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("No payment exists for booking " + bookingId));
        return mapToResponse(payment);
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    private String resolveCurrency(String requestedCurrency) {
        return requestedCurrency == null || requestedCurrency.isBlank() ? DEFAULT_CURRENCY : requestedCurrency.trim().toUpperCase();
    }

    private double resolveAmount(Booking booking, Double requestedAmount) {
        if (requestedAmount != null) {
            return requestedAmount;
        }
        double distanceKm = booking.getTotalDistance() == null ? 0.0 : booking.getTotalDistance() / 1000.0;
        return Math.round((BASE_FARE + distanceKm * PER_KM_FARE) * 100.0) / 100.0;
    }

    private String generateProviderReference(UUID bookingId) {
        return "PAY-" + bookingId.toString().substring(0, 8).toUpperCase();
    }

    private RidePayment createCapturedCashPayment(Booking booking) {
        RidePayment payment = RidePayment.builder()
                .bookingId(booking.getId())
                .amount(resolveAmount(booking, null))
                .currency(DEFAULT_CURRENCY)
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.CAPTURED)
                .providerReference(generateProviderReference(booking.getId()))
                .capturedAt(LocalDateTime.now())
                .build();
        return ridePaymentRepository.save(payment);
    }

    private PaymentResponseDto mapToResponse(RidePayment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getStatus())
                .providerReference(payment.getProviderReference())
                .failureReason(payment.getFailureReason())
                .authorizedAt(payment.getAuthorizedAt())
                .capturedAt(payment.getCapturedAt())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private void publishPaymentEvent(Booking booking, RidePayment payment, String eventType, String message) {
        kafkaProducerService.publishPaymentLifecycleEvent(PaymentLifecycleEvent.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .passengerId(booking.getPassenger() != null ? booking.getPassenger().getId() : null)
                .driverId(booking.getDriver() != null ? booking.getDriver().getId() : null)
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .paymentStatus(payment.getStatus().name())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .eventType(eventType)
                .source("UberBookingService")
                .providerReference(payment.getProviderReference())
                .message(message)
                .occurredAt(LocalDateTime.now())
                .build());
    }
}

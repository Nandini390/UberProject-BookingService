package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.dto.TripOtpResponseDto;
import org.example.uberbookingservice.entities.TripOtp;
import org.example.uberbookingservice.repositories.TripOtpRepository;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripOtpService {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TripOtpRepository tripOtpRepository;

    @Transactional
    public TripOtpResponseDto generateOtp(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.CAB_ARRIVED) {
            throw new IllegalStateException("OTP can only be generated when cab has arrived");
        }

        Optional<TripOtp> existingOtp = tripOtpRepository.findByBookingId(booking.getId());
        if (existingOtp.isPresent() && !existingOtp.get().getVerified()) {
            return map(existingOtp.get());
        }

        TripOtp otp = TripOtp.builder()
                .bookingId(booking.getId())
                .code(String.format("%04d", SECURE_RANDOM.nextInt(10000)))
                .verified(false)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();

        TripOtp savedOtp = tripOtpRepository.save(otp);
        return map(savedOtp);
    }

    @Transactional
    public TripOtpResponseDto verifyOtp(UUID bookingId, String code) {
        TripOtp otp = tripOtpRepository.findOtpByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("No OTP found for booking " + bookingId));
        if (otp.isUsed()) {
            throw new IllegalStateException("OTP already used");
        }
        if (Boolean.TRUE.equals(otp.getVerified())) {
            return map(otp);
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("OTP has expired");
        }
        if (!otp.getCode().equals(code)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        otp.setVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        return map(tripOtpRepository.save(otp));
    }

    public void ensureVerified(UUID bookingId) {
        TripOtp otp = tripOtpRepository.findOtpByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new IllegalStateException("OTP verification is required before starting trip"));

        if (!Boolean.TRUE.equals(otp.getVerified())) {
            throw new IllegalStateException("OTP verification is required before starting trip");
        }
        if (otp.isUsed()) {
            throw new IllegalStateException("OTP already used");
        }
    }

    @Transactional
    public void consumeOtp(UUID bookingId) {
        TripOtp otp = tripOtpRepository.findOtpByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new IllegalStateException("OTP not found"));
        if (otp.isUsed()) {
            throw new IllegalStateException("OTP already used");
        }
        otp.setUsed(true);
        tripOtpRepository.save(otp);
    }


    private TripOtpResponseDto map(TripOtp otp) {
        return TripOtpResponseDto.builder()
                .bookingId(otp.getBookingId())
                .code(otp.getCode())
                .verified(otp.getVerified())
                .expiresAt(otp.getExpiresAt())
                .build();
    }
}

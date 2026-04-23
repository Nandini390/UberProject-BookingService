package org.example.uberbookingservice.services;

import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberbookingservice.dto.BookingAuditLogDto;
import org.example.uberbookingservice.dto.TripOtpResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingRequestDto;
import org.example.uberbookingservice.dto.UpdateBookingResponseDto;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails);

    UpdateBookingResponseDto assignDriver(UUID bookingId, UpdateBookingRequestDto bookingRequestDto);

    UpdateBookingResponseDto markCabArrived(UUID bookingId);

    UpdateBookingResponseDto startTrip(UUID bookingId);

    UpdateBookingResponseDto completeTrip(UUID bookingId);

    UpdateBookingResponseDto cancelBooking(UUID bookingId, UpdateBookingRequestDto bookingRequestDto);

    UpdateBookingResponseDto getBooking(UUID bookingId);

    List<UpdateBookingResponseDto> getBookingsByPassenger(UUID passengerId);

    List<UpdateBookingResponseDto> getBookingsByDriver(UUID driverId);

    TripOtpResponseDto verifyTripOtp(UUID bookingId, String code);

    List<BookingAuditLogDto> getAuditTrail(UUID bookingId);
}

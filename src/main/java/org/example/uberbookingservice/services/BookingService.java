package org.example.uberbookingservice.services;

import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberbookingservice.dto.BookingAuditLogDto;
import org.example.uberbookingservice.dto.BookingTrackingResponseDto;
import org.example.uberbookingservice.dto.AdminBookingReportDto;
import org.example.uberbookingservice.dto.TripOtpResponseDto;
import org.example.uberbookingservice.dto.DriverDecisionRequestDto;
import org.example.uberbookingservice.dto.NotificationDto;
import org.example.uberbookingservice.dto.PagedResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingRequestDto;
import org.example.uberbookingservice.dto.UpdateBookingResponseDto;
import org.example.uberprojectentityservice.Models.BookingStatus;

import java.time.LocalDateTime;
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

    BookingTrackingResponseDto getLiveTracking(UUID bookingId);

    UpdateBookingResponseDto rejectDriver(UUID bookingId, DriverDecisionRequestDto requestDto);

    PagedResponseDto<UpdateBookingResponseDto> getPassengerBookingHistory(UUID passengerId, BookingStatus status, int page, int size);

    PagedResponseDto<UpdateBookingResponseDto> getDriverBookingHistory(UUID driverId, BookingStatus status, int page, int size);

    PagedResponseDto<UpdateBookingResponseDto> searchBookings(UUID passengerId, UUID driverId, BookingStatus status, LocalDateTime from, LocalDateTime to, int page, int size);

    AdminBookingReportDto getAdminReport(LocalDateTime from, LocalDateTime to);

    List<NotificationDto> getNotifications(UUID recipientId);
}

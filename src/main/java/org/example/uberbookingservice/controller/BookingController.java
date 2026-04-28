package org.example.uberbookingservice.controller;

import jakarta.validation.Valid;
import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberbookingservice.dto.BookingAuditLogDto;
import org.example.uberbookingservice.dto.BookingTrackingResponseDto;
import org.example.uberbookingservice.dto.AdminBookingReportDto;
import org.example.uberbookingservice.dto.DriverDecisionRequestDto;
import org.example.uberbookingservice.dto.NotificationDto;
import org.example.uberbookingservice.dto.PagedResponseDto;
import org.example.uberbookingservice.dto.TripOtpResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingRequestDto;
import org.example.uberbookingservice.dto.VerifyTripOtpRequestDto;
import org.example.uberbookingservice.services.BookingService;
import org.example.uberbookingservice.services.Impl.IdempotencyService;
import org.example.uberbookingservice.services.Impl.WebhookService;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;
    private final WebhookService webhookService;
    private final IdempotencyService idempotencyService;

    public BookingController(BookingService bookingService,WebhookService webhookService, IdempotencyService idempotencyService){
        this.bookingService=bookingService;
        this.webhookService=webhookService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> CreateBooking(@Valid @RequestBody CreateBookingDto createBookingDto,
                                                                  @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey){
       return idempotencyService.execute(
               "BOOKING_CREATE",
               idempotencyKey,
               () -> {
                   CreateBookingResponseDto result = bookingService.createBooking(createBookingDto);
                   webhookService.sendWebhook("BOOKING_CREATED", result);

                   return new ResponseEntity<>(result, HttpStatus.CREATED);
               },
               CreateBookingResponseDto.class
       );
    }

    @PostMapping("{bookingId}/assign-driver")
    public ResponseEntity<UpdateBookingResponseDto> assignDriver(@Valid @RequestBody UpdateBookingRequestDto requestDto, @PathVariable UUID bookingId){
      return new ResponseEntity<>(bookingService.assignDriver(bookingId, requestDto),HttpStatus.OK);
    }

    @PostMapping("{bookingId}/reject-driver")
    public ResponseEntity<UpdateBookingResponseDto> rejectDriver(@Valid @RequestBody DriverDecisionRequestDto requestDto, @PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.rejectDriver(bookingId, requestDto));
    }

    @PostMapping("{bookingId}/arrived")
    public ResponseEntity<UpdateBookingResponseDto> markCabArrived(@PathVariable UUID bookingId){
        return new ResponseEntity<>(bookingService.markCabArrived(bookingId), HttpStatus.OK);
    }

    @PostMapping("{bookingId}/start")
    public ResponseEntity<UpdateBookingResponseDto> startTrip(@PathVariable UUID bookingId){
        return new ResponseEntity<>(bookingService.startTrip(bookingId), HttpStatus.OK);
    }

    @PostMapping("{bookingId}/otp/verify")
    public ResponseEntity<TripOtpResponseDto> verifyTripOtp(@PathVariable UUID bookingId, @Valid @RequestBody VerifyTripOtpRequestDto requestDto) {
        return ResponseEntity.ok(bookingService.verifyTripOtp(bookingId, requestDto.getCode()));
    }

    @PostMapping("{bookingId}/complete")
    public ResponseEntity<UpdateBookingResponseDto> completeTrip(@PathVariable UUID bookingId){
        return new ResponseEntity<>(bookingService.completeTrip(bookingId), HttpStatus.OK);
    }

    @PostMapping("{bookingId}/cancel")
    public ResponseEntity<UpdateBookingResponseDto> cancelBooking(@Valid @RequestBody(required = false) UpdateBookingRequestDto requestDto, @PathVariable UUID bookingId){
        return new ResponseEntity<>(bookingService.cancelBooking(bookingId, requestDto), HttpStatus.OK);
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> getBooking(@PathVariable UUID bookingId){
        return new ResponseEntity<>(bookingService.getBooking(bookingId), HttpStatus.OK);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<UpdateBookingResponseDto>> getBookingsByPassenger(@PathVariable UUID passengerId) {
        return new ResponseEntity<>(bookingService.getBookingsByPassenger(passengerId), HttpStatus.OK);
    }

    @GetMapping("/passenger/{passengerId}/history")
    public ResponseEntity<PagedResponseDto<UpdateBookingResponseDto>> getPassengerBookingHistory(
            @PathVariable UUID passengerId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.getPassengerBookingHistory(passengerId, status, page, size));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<UpdateBookingResponseDto>> getBookingsByDriver(@PathVariable UUID driverId) {
        return new ResponseEntity<>(bookingService.getBookingsByDriver(driverId), HttpStatus.OK);
    }

    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<PagedResponseDto<UpdateBookingResponseDto>> getDriverBookingHistory(
            @PathVariable UUID driverId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.getDriverBookingHistory(driverId, status, page, size));
    }

    @GetMapping("{bookingId}/audit")
    public ResponseEntity<List<BookingAuditLogDto>> getAuditTrail(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getAuditTrail(bookingId));
    }

    @GetMapping("{bookingId}/tracking")
    public ResponseEntity<BookingTrackingResponseDto> getLiveTracking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getLiveTracking(bookingId));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponseDto<UpdateBookingResponseDto>> searchBookings(
            @RequestParam(required = false) UUID passengerId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingService.searchBookings(passengerId, driverId, status, from, to, page, size));
    }

    @GetMapping("/admin/report")
    public ResponseEntity<AdminBookingReportDto> getAdminReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(bookingService.getAdminReport(from, to));
    }

    @GetMapping("/notifications/{recipientId}")
    public ResponseEntity<List<NotificationDto>> getNotifications(@PathVariable UUID recipientId) {
        return ResponseEntity.ok(bookingService.getNotifications(recipientId));
    }
}

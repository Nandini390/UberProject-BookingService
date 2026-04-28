package org.example.uberbookingservice.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.uberbookingservice.Apis.UberSocketApi;
import org.example.uberbookingservice.dispatch.DispatchStatus;
import org.example.uberbookingservice.dto.RideRequestDto;
import org.example.uberbookingservice.entities.BookingDispatch;
import org.example.uberbookingservice.repositories.BookingDispatchRepository;
import org.example.uberbookingservice.repositories.BookingRepository;
import org.example.uberbookingservice.repositories.DriverRepository;
import org.example.uberbookingservice.services.NotificationService;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;
import org.example.uberprojectentityservice.Models.ExactLocation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingDispatchService {

    private static final long DRIVER_RESPONSE_TIMEOUT_SECONDS = 45L;

    private final BookingDispatchRepository bookingDispatchRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final UberSocketApi uberSocketApi;
    private final BookingAuditService bookingAuditService;
    private final KafkaProducerService kafkaProducerService;
    private final NotificationService notificationService;

    public void startDispatch(Booking booking, UUID passengerId, ExactLocation startLocation, ExactLocation endLocation, List<UUID> driverIds) {
        if (driverIds == null || driverIds.isEmpty()) {
            return;
        }
        BookingDispatch dispatch = bookingDispatchRepository.findByBookingId(booking.getId())
                .orElse(BookingDispatch.builder()
                        .bookingId(booking.getId())
                        .rejectedDriverIds("")
                        .attemptCount(0)
                        .build());

        dispatch.setCandidateDriverIds(toCsv(driverIds));
        dispatch.setDispatchStatus(DispatchStatus.WAITING_DRIVER_RESPONSE);
        dispatch.setAttemptCount(0);
        bookingDispatchRepository.save(dispatch);
        offerNextDriver(booking, passengerId, startLocation, endLocation, dispatch, null, "Ride request created");
    }

    public boolean canDriverAccept(UUID bookingId, UUID driverId) {
        return bookingDispatchRepository.findByBookingId(bookingId)
                .filter(dispatch -> dispatch.getDispatchStatus() == DispatchStatus.WAITING_DRIVER_RESPONSE)
                .filter(dispatch -> driverId.equals(dispatch.getCurrentDriverId()))
                .isPresent();
    }

    public void markDriverAccepted(UUID bookingId, UUID driverId) {
        bookingDispatchRepository.findByBookingId(bookingId).ifPresent(dispatch -> {
            dispatch.setCurrentDriverId(driverId);
            dispatch.setDispatchStatus(DispatchStatus.DRIVER_ASSIGNED);
            dispatch.setExpiresAt(null);
            bookingDispatchRepository.save(dispatch);
        });
    }

    public boolean rejectDriver(UUID bookingId, UUID driverId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        BookingDispatch dispatch = bookingDispatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("No dispatch exists for booking " + bookingId));

        if (!driverId.equals(dispatch.getCurrentDriverId())) {
            return false;
        }
        Set<UUID> rejectedDrivers = parseCsv(dispatch.getRejectedDriverIds());
        rejectedDrivers.add(driverId);
        dispatch.setRejectedDriverIds(toCsv(rejectedDrivers));
        bookingDispatchRepository.save(dispatch);
        bookingAuditService.log(bookingId, "DRIVER_REJECTED", reason == null || reason.isBlank() ? "Driver rejected booking" : reason, "DRIVER", driverId);
        notificationService.notifyUser(booking.getPassenger() != null ? booking.getPassenger().getId() : null, bookingId, "DRIVER_REJECTED", "Driver unavailable", "Searching for another nearby driver.");
        return offerNextDriver(booking, booking.getPassenger() != null ? booking.getPassenger().getId() : null, booking.getStartLocation(), booking.getEndLocation(), dispatch, driverId, "Driver rejected request");
    }

    public boolean reassignAfterDriverCancellation(Booking booking, String reason) {
        BookingDispatch dispatch = bookingDispatchRepository.findByBookingId(booking.getId()).orElse(null);
        if (dispatch == null) {
            return false;
        }
        UUID cancelledDriverId = booking.getDriver() != null ? booking.getDriver().getId() : null;
        if (cancelledDriverId != null) {
            Set<UUID> rejectedDrivers = parseCsv(dispatch.getRejectedDriverIds());
            rejectedDrivers.add(cancelledDriverId);
            dispatch.setRejectedDriverIds(toCsv(rejectedDrivers));
        }
        booking.setDriver(null);
        booking.setBookingStatus(BookingStatus.ASSIGNING_DRIVER);
        bookingRepository.save(booking);
        bookingAuditService.log(booking.getId(), "DRIVER_CANCELLED", reason == null || reason.isBlank() ? "Driver cancelled before trip started" : reason, "DRIVER", cancelledDriverId);
        notificationService.notifyUser(booking.getPassenger() != null ? booking.getPassenger().getId() : null, booking.getId(), "DRIVER_CANCELLED", "Driver cancelled", "We are finding another nearby driver.");
        return offerNextDriver(booking, booking.getPassenger() != null ? booking.getPassenger().getId() : null, booking.getStartLocation(), booking.getEndLocation(), dispatch, cancelledDriverId, "Driver cancelled ride before start");
    }

    public void closeDispatch(UUID bookingId) {
        bookingDispatchRepository.findByBookingId(bookingId).ifPresent(dispatch -> {
            dispatch.setDispatchStatus(DispatchStatus.CLOSED);
            dispatch.setExpiresAt(null);
            bookingDispatchRepository.save(dispatch);
        });
    }

    @Scheduled(fixedDelay = 15000)
    public void expirePendingDispatches() {
        List<BookingDispatch> expiredDispatches = bookingDispatchRepository.findAllByDispatchStatusAndExpiresAtBefore(
                DispatchStatus.WAITING_DRIVER_RESPONSE,
                LocalDateTime.now()
        );
        for (BookingDispatch dispatch : expiredDispatches) {
            bookingRepository.findById(dispatch.getBookingId()).ifPresent(booking -> {
                offerNextDriver(
                        booking,
                        booking.getPassenger() != null ? booking.getPassenger().getId() : null,
                        booking.getStartLocation(),
                        booking.getEndLocation(),
                        dispatch,
                        dispatch.getCurrentDriverId(),
                        "Driver response timed out"
                );
            });
        }
    }

    private boolean offerNextDriver(Booking booking, UUID passengerId, ExactLocation startLocation, ExactLocation endLocation, BookingDispatch dispatch, UUID previousDriverId, String reason) {
        List<UUID> candidates = parseOrderedCsv(dispatch.getCandidateDriverIds());
        Set<UUID> rejectedDrivers = parseCsv(dispatch.getRejectedDriverIds());
        if (previousDriverId != null) {
            rejectedDrivers.add(previousDriverId);
            dispatch.setRejectedDriverIds(toCsv(rejectedDrivers));
        }

        UUID nextDriverId = candidates.stream()
                .filter(candidate -> !rejectedDrivers.contains(candidate))
                .filter(candidate -> driverRepository.findById(candidate)
                        .map(driver -> Boolean.TRUE.equals(driver.getIsAvailable()))
                        .orElse(false))
                .findFirst()
                .orElse(null);

        if (nextDriverId == null) {
            dispatch.setDispatchStatus(DispatchStatus.EXHAUSTED);
            dispatch.setCurrentDriverId(null);
            dispatch.setExpiresAt(null);
            bookingDispatchRepository.save(dispatch);
            booking.setBookingStatus(BookingStatus.CANCELLED);
            booking.setPreviousStatus(BookingStatus.ASSIGNING_DRIVER);
            bookingRepository.save(booking);
            bookingAuditService.log(booking.getId(), "DISPATCH_EXHAUSTED", "No more eligible drivers available", "SYSTEM", null);
            notificationService.notifyUser(passengerId, booking.getId(), "NO_DRIVER_AVAILABLE", "No drivers available", "No driver accepted your ride request.");
            return false;
        }

        dispatch.setCurrentDriverId(nextDriverId);
        dispatch.setDispatchStatus(DispatchStatus.WAITING_DRIVER_RESPONSE);
        dispatch.setAttemptCount((dispatch.getAttemptCount() == null ? 0 : dispatch.getAttemptCount()) + 1);
        dispatch.setRequestedAt(LocalDateTime.now());
        dispatch.setExpiresAt(LocalDateTime.now().plusSeconds(DRIVER_RESPONSE_TIMEOUT_SECONDS));
        bookingDispatchRepository.save(dispatch);

        Driver nextDriver = driverRepository.findById(nextDriverId).orElse(null);
        bookingAuditService.log(booking.getId(), "DISPATCH_DRIVER_REQUESTED", reason, "SYSTEM", nextDriverId);
        notificationService.notifyUser(passengerId, booking.getId(), "DRIVER_SEARCHING", "Finding driver", "Request sent to another nearby driver.");

        RideRequestDto rideRequest = RideRequestDto.builder()
                .passengerId(passengerId)
                .bookingId(booking.getId())
                .startLocation(startLocation)
                .endLocation(endLocation)
                .driverIds(List.of(nextDriverId))
                .build();
        raiseRideRequestAsync(rideRequest);
        return nextDriver != null;
    }

    private void raiseRideRequestAsync(RideRequestDto requestDto) {
        Call<Boolean> call = uberSocketApi.raiseRideRequest(requestDto);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable throwable) {
            }
        });
    }

    private Set<UUID> parseCsv(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(value.split(","))
                .filter(token -> !token.isBlank())
                .map(UUID::fromString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<UUID> parseOrderedCsv(String value) {
        return new ArrayList<>(parseCsv(value));
    }

    private String toCsv(Collection<UUID> ids) {
        return ids == null || ids.isEmpty()
                ? ""
                : ids.stream().map(UUID::toString).collect(Collectors.joining(","));
    }
}

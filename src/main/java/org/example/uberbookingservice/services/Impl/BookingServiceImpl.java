package org.example.uberbookingservice.services.Impl;

import org.example.uberbookingservice.Apis.LocationServiceApi;
import org.example.uberbookingservice.Apis.UberSocketApi;
import org.example.uberbookingservice.dto.*;
import org.example.uberbookingservice.events.BookingLifecycleEvent;
import org.example.uberbookingservice.payments.PaymentStatus;
import org.example.uberbookingservice.repositories.BookingRepository;
import org.example.uberbookingservice.repositories.DriverRepository;
import org.example.uberbookingservice.repositories.PassengerRepository;
import org.example.uberbookingservice.repositories.RidePaymentRepository;
import org.example.uberbookingservice.services.BookingService;
import org.example.uberbookingservice.services.PaymentService;
import org.example.uberprojectentityservice.Models.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private static final double BASE_FARE = 60.0;
    private static final double PER_KM_FARE = 14.0;
    private static final double MIN_CANCELLATION_CHARGE = 0.0;
    private static final double DRIVER_ASSIGNED_CANCELLATION_CHARGE = 40.0;
    private static final double CAB_ARRIVED_CANCELLATION_CHARGE = 75.0;

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final LocationServiceApi locationServiceApi;
    private final UberSocketApi uberSocketApi;
    private final KafkaProducerService kafkaProducerService;
    private final PaymentService paymentService;
    private final RidePaymentRepository ridePaymentRepository;
    private final BookingAuditService bookingAuditService;
    private final TripOtpService tripOtpService;


    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository, DriverRepository driverRepository, LocationServiceApi locationServiceApi, UberSocketApi uberSocketApi, KafkaProducerService kafkaProducerService, PaymentService paymentService, RidePaymentRepository ridePaymentRepository, BookingAuditService bookingAuditService, TripOtpService tripOtpService){
        this.passengerRepository=passengerRepository;
        this.bookingRepository = bookingRepository;
        this.driverRepository=driverRepository;
        this.locationServiceApi=locationServiceApi;
        this.uberSocketApi=uberSocketApi;
        this.kafkaProducerService = kafkaProducerService;
        this.paymentService = paymentService;
        this.ridePaymentRepository = ridePaymentRepository;
        this.bookingAuditService = bookingAuditService;
        this.tripOtpService = tripOtpService;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        Passenger passenger = passengerRepository.findById(bookingDetails.getPassengerId())
                .orElseThrow(() -> new IllegalArgumentException("Passenger not found: " + bookingDetails.getPassengerId()));
        Booking booking= Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .totalDistance(calculateDistanceMeters(bookingDetails.getStartLocation(), bookingDetails.getEndLocation()))
                .passenger(passenger)
                .build();
        Booking newBooking = bookingRepository.save(booking);
        publishEvent(newBooking, "BOOKING_CREATED", "Booking created and driver assignment started");
        audit(newBooking.getId(), "BOOKING_CREATED", "Passenger created a booking", "PASSENGER", passenger.getId());

        NearbyDriverDto request= NearbyDriverDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();
        processNearbyDriverAsync(
                request,
                bookingDetails.getPassengerId(),
                newBooking.getId(),
                bookingDetails.getStartLocation(),
                bookingDetails.getEndLocation()
        );

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .driver(Optional.ofNullable(newBooking.getDriver()))
                .passengerId(passenger.getId())
                .startLocation(newBooking.getStartLocation())
                .endLocation(newBooking.getEndLocation())
                .totalDistanceMeters(newBooking.getTotalDistance())
                .estimatedFare(calculateEstimatedFare(newBooking))
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }


    private void processNearbyDriverAsync(NearbyDriverDto requestDto, UUID passengerId, UUID bookingId, ExactLocation startLocation, ExactLocation endLocation){
        Call<DriverLocationDto[]> call= locationServiceApi.getNearbyDrivers(requestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body()!=null){
                  List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                  List<UUID> candidateDriverIds = driverLocations.stream()
                          .map(DriverLocationDto::getDriverId)
                          .map(UUID::fromString)
                          .collect(Collectors.toList());

                  List<UUID> eligibleDriverIds = driverRepository.findAllById(candidateDriverIds).stream()
                          .filter(driver -> driver.getDriverApprovalStatus() == DriverApprovalStatus.APPROVED)
                          .filter(driver -> Boolean.TRUE.equals(driver.getIsAvailable()))
                          .map(Driver::getId)
                          .collect(Collectors.toList());

                  try{
                      if(eligibleDriverIds.isEmpty()) {
                          bookingRepository.updateBookingStatusById(bookingId, BookingStatus.CANCELLED);
                          Booking cancelledBooking = getBookingEntity(bookingId);
                          publishEvent(cancelledBooking, "NO_DRIVER_AVAILABLE", "No eligible drivers were available nearby");
                          audit(cancelledBooking.getId(), "NO_DRIVER_AVAILABLE", "No eligible drivers found nearby", "SYSTEM", null);
                          return;
                      }

                      raiseRideRequestAsync(RideRequestDto.builder()
                              .passengerId(passengerId)
                              .bookingId(bookingId)
                              .startLocation(startLocation)
                              .endLocation(endLocation)
                              .driverIds(eligibleDriverIds)
                              .build());
                  } catch (Exception e) {
                      throw new RuntimeException(e);
                  }
            }else{
                    System.out.println("Request failed"+response.message());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                 t.printStackTrace();
            }
        });
    }


    @Override
    @Transactional
    public UpdateBookingResponseDto assignDriver(UUID bookingId, UpdateBookingRequestDto bookingRequestDto) {
            if (bookingRequestDto == null || bookingRequestDto.getDriverId() == null) {
                throw new IllegalArgumentException("driverId is required to assign a booking");
            }

            Booking booking = getBookingEntity(bookingId);
            if (booking.getBookingStatus() != BookingStatus.ASSIGNING_DRIVER && booking.getBookingStatus() != BookingStatus.PENDING) {
                throw new IllegalStateException("Booking is not waiting for driver assignment");
            }

            Driver driver = driverRepository.findById(bookingRequestDto.getDriverId())
                    .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + bookingRequestDto.getDriverId()));

            if (driver.getDriverApprovalStatus() != DriverApprovalStatus.APPROVED) {
                throw new IllegalStateException("Driver is not approved");
            }
            if (!Boolean.TRUE.equals(driver.getIsAvailable())) {
                throw new IllegalStateException("Driver is not available");
            }

            booking.setDriver(driver);
            booking.setBookingStatus(BookingStatus.ACCEPTED);
            driver.setIsAvailable(false);
            bookingRepository.save(booking);
            driverRepository.save(driver);
            publishEvent(booking, "DRIVER_ASSIGNED", "Driver assigned successfully");
            audit(booking.getId(), "DRIVER_ASSIGNED", "Driver assigned to booking", "DRIVER", driver.getId());
            return mapToResponse(booking);
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto markCabArrived(UUID bookingId) {
        Booking booking = getBookingEntity(bookingId);
        ensureStatus(booking, BookingStatus.ACCEPTED);
        booking.setBookingStatus(BookingStatus.CAB_ARRIVED);
        Booking savedBooking = bookingRepository.save(booking);
        TripOtpResponseDto otp = tripOtpService.generateOtp(savedBooking);
        publishEvent(savedBooking, "CAB_ARRIVED", "Driver arrived at pickup location");
        audit(savedBooking.getId(), "CAB_ARRIVED", "Driver marked arrival at pickup", "DRIVER", savedBooking.getDriver() != null ? savedBooking.getDriver().getId() : null);
        UpdateBookingResponseDto response = mapToResponse(savedBooking);
        response.setOtp(otp);
        return response;
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto startTrip(UUID bookingId) {
        Booking booking = getBookingEntity(bookingId);
        ensureStatus(booking, BookingStatus.CAB_ARRIVED);
        tripOtpService.ensureVerified(bookingId);
        tripOtpService.consumeOtp(bookingId);
        booking.setBookingStatus(BookingStatus.TRIP_STARTED);
        booking.setStartTime(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);
        publishEvent(savedBooking, "TRIP_STARTED", "Trip started");
        audit(savedBooking.getId(), "TRIP_STARTED", "Trip started after OTP verification", "SYSTEM", null);
        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto completeTrip(UUID bookingId) {
        Booking booking = getBookingEntity(bookingId);
        if (booking.getBookingStatus() != BookingStatus.TRIP_STARTED && booking.getBookingStatus() != BookingStatus.IN_RIDE) {
            throw new IllegalStateException("Trip can only be completed after it starts");
        }
        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setEndTime(LocalDateTime.now());

        Driver driver = booking.getDriver();
        if (driver != null) {
            driver.setIsAvailable(true);
            driverRepository.save(driver);
        }
        Booking savedBooking = bookingRepository.save(booking);
        paymentService.capturePayment(savedBooking.getId());
        publishEvent(savedBooking, "TRIP_COMPLETED", "Trip completed successfully");
        audit(savedBooking.getId(), "TRIP_COMPLETED", "Trip completed and payment capture triggered", "SYSTEM", null);
        return mapToResponse(savedBooking);
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto cancelBooking(UUID bookingId, UpdateBookingRequestDto bookingRequestDto) {
        Booking booking = getBookingEntity(bookingId);
        if (booking.getBookingStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed trip cannot be cancelled");
        }
        if (booking.getBookingStatus() == BookingStatus.TRIP_STARTED || booking.getBookingStatus() == BookingStatus.IN_RIDE) {
            throw new IllegalStateException("Trip already started and cannot be cancelled");
        }
        booking.setPreviousStatus(booking.getBookingStatus());
        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setEndTime(LocalDateTime.now());

        Driver driver = booking.getDriver();
        if (driver != null) {
            driver.setIsAvailable(true);
            driverRepository.save(driver);
        }
        Booking savedBooking = bookingRepository.save(booking);
        if (ridePaymentRepository.findByBookingId(savedBooking.getId()).isPresent()) {
            try {
                paymentService.refundPayment(savedBooking.getId(), bookingRequestDto != null ? bookingRequestDto.getCancellationReason() : "Booking cancelled");
            } catch (IllegalStateException e) {
                System.err.println("Refund failed for booking " + bookingId + ": " + e.getMessage());
            }
        }
        publishEvent(savedBooking, "BOOKING_CANCELLED", bookingRequestDto != null && bookingRequestDto.getCancellationReason() != null
                ? bookingRequestDto.getCancellationReason()
                : "Booking cancelled");
        audit(savedBooking.getId(), "BOOKING_CANCELLED", bookingRequestDto != null && bookingRequestDto.getCancellationReason() != null
                ? bookingRequestDto.getCancellationReason()
                : "Booking cancelled", "SYSTEM", null);
        return mapToResponse(savedBooking, calculateCancellationCharge(savedBooking));
    }

    @Override
    public UpdateBookingResponseDto getBooking(UUID bookingId) {
        return mapToResponse(getBookingEntity(bookingId));
    }

    @Override
    public List<UpdateBookingResponseDto> getBookingsByPassenger(UUID passengerId) {
        return bookingRepository.findAllByPassengerIdOrderByCreatedAtDesc(passengerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<UpdateBookingResponseDto> getBookingsByDriver(UUID driverId) {
        return bookingRepository.findAllByDriverIdOrderByCreatedAtDesc(driverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TripOtpResponseDto verifyTripOtp(UUID bookingId, String code) {
        TripOtpResponseDto otp = tripOtpService.verifyOtp(bookingId, code);
        Booking booking = getBookingEntity(bookingId);
        audit(bookingId, "OTP_VERIFIED", "Trip OTP verified successfully", "PASSENGER",
                booking.getPassenger() != null ? booking.getPassenger().getId() : null);
        return otp;
    }

    @Override
    public List<BookingAuditLogDto> getAuditTrail(UUID bookingId) {
        getBookingEntity(bookingId);
        return bookingAuditService.getAuditTrail(bookingId);
    }




    private void raiseRideRequestAsync(RideRequestDto requestDto){
        Call<Boolean> call=uberSocketApi.raiseRideRequest(requestDto);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Boolean result= response.body();
                    System.out.println("Driver response is: "+result.toString());
                }else{
                    System.out.println("Request for ride failed: "+response.message());
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                 t.printStackTrace();
            }
        });
    }

    private Booking getBookingEntity(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    private void ensureStatus(Booking booking, BookingStatus expectedStatus) {
        if (booking.getBookingStatus() != expectedStatus) {
            throw new IllegalStateException("Booking must be in " + expectedStatus + " state");
        }
    }

    private UpdateBookingResponseDto mapToResponse(Booking booking) {
        return mapToResponse(booking, calculateCancellationCharge(booking));
    }

    private UpdateBookingResponseDto mapToResponse(Booking booking, Double cancellationCharge) {
        return UpdateBookingResponseDto.builder()
                .bookingId(booking.getId())
                .bookingStatus(booking.getBookingStatus())
                .driver(Optional.ofNullable(booking.getDriver()))
                .passengerId(booking.getPassenger() != null ? booking.getPassenger().getId() : null)
                .startLocation(booking.getStartLocation())
                .endLocation(booking.getEndLocation())
                .totalDistanceMeters(booking.getTotalDistance())
                .estimatedFare(calculateEstimatedFare(booking))
                .cancellationCharge(cancellationCharge)
                .paymentStatus(resolvePaymentStatus(booking.getId()))
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .build();
    }

    private Long calculateDistanceMeters(ExactLocation start, ExactLocation end) {
        if (start == null || end == null) {
            return 0L;
        }
        double earthRadiusMeters = 6371000.0;
        double latDistance = Math.toRadians(end.getLatitude() - start.getLatitude());
        double lonDistance = Math.toRadians(end.getLongitude() - start.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(start.getLatitude())) * Math.cos(Math.toRadians(end.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(earthRadiusMeters * c);
    }

    private Double calculateEstimatedFare(Booking booking) {
        double distanceKm = booking.getTotalDistance() == null ? 0.0 : booking.getTotalDistance() / 1000.0;
        return Math.round((BASE_FARE + distanceKm * PER_KM_FARE) * 100.0) / 100.0;
    }

    private Double calculateCancellationCharge(Booking booking) {
        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            return MIN_CANCELLATION_CHARGE;
        }
        if (booking.getDriver() == null) {
            return MIN_CANCELLATION_CHARGE;
        }

        BookingStatus statusBeforeCancel = derivePreCancelStatus(booking);

        if (statusBeforeCancel == BookingStatus.CAB_ARRIVED) {
            return CAB_ARRIVED_CANCELLATION_CHARGE;            // ₹75
        }
        if (statusBeforeCancel == BookingStatus.ACCEPTED) {
            return DRIVER_ASSIGNED_CANCELLATION_CHARGE;        // ₹40
        }
        return MIN_CANCELLATION_CHARGE;
    }

    private BookingStatus derivePreCancelStatus(Booking booking) {
        return booking.getPreviousStatus() != null ? booking.getPreviousStatus() : BookingStatus.ASSIGNING_DRIVER;
    }

    private void publishEvent(Booking booking, String eventType, String message) {
        kafkaProducerService.publishBookingLifecycleEvent(BookingLifecycleEvent.builder()
                .bookingId(booking.getId())
                .passengerId(booking.getPassenger() != null ? booking.getPassenger().getId() : null)
                .driverId(booking.getDriver() != null ? booking.getDriver().getId() : null)
                .eventType(eventType)
                .bookingStatus(booking.getBookingStatus().name())
                .source("UberBookingService")
                .message(message)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private void audit(UUID bookingId, String action, String message, String actorType, UUID actorId) {
        bookingAuditService.log(bookingId, action, message, actorType, actorId);
    }

    private PaymentStatus resolvePaymentStatus(UUID bookingId) {
        return ridePaymentRepository.findByBookingId(bookingId)
                .map(payment -> payment.getStatus())
                .orElse(PaymentStatus.PENDING);
    }
}

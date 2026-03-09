package org.example.uberbookingservice.services;

import org.example.uberbookingservice.Apis.LocationServiceApi;
import org.example.uberbookingservice.Apis.UberSocketApi;
import org.example.uberbookingservice.dto.*;
import org.example.uberbookingservice.repositories.BookingRepository;
import org.example.uberbookingservice.repositories.DriverRepository;
import org.example.uberbookingservice.repositories.PassengerRepository;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;
import org.example.uberprojectentityservice.Models.Passenger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService{

    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final LocationServiceApi locationServiceApi;
    private final UberSocketApi uberSocketApi;


    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository, DriverRepository driverRepository, LocationServiceApi locationServiceApi, UberSocketApi uberSocketApi){
        this.passengerRepository=passengerRepository;
        this.bookingRepository = bookingRepository;
        this.driverRepository=driverRepository;
        this.locationServiceApi=locationServiceApi;
        this.uberSocketApi=uberSocketApi;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        Optional<Passenger> passenger=passengerRepository.findById(bookingDetails.getPassengerId());
        Booking booking= Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .passenger(passenger.get())
                .build();
        Booking newBooking = bookingRepository.save(booking);

        NearbyDriverDto request= NearbyDriverDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();
        processNearbyDriverAsync(request,bookingDetails.getPassengerId());

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .build();
    }


    private void processNearbyDriverAsync(NearbyDriverDto requestDto, Long passengerId){
        Call<DriverLocationDto[]> call= locationServiceApi.getNearbyDrivers(requestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body()!=null){
                  List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                  driverLocations.forEach(driverLocationDto -> {
                  System.out.println(driverLocationDto.getDriverId() + " " + "lat: " + driverLocationDto.getLatitude()+ " " + "long: " + driverLocationDto.getLongitude());
                });
                  raiseRideRequestAsync(RideRequestDto.builder().passengerId(passengerId).build());
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
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId) {
            Optional<Driver> driver=driverRepository.findById(bookingRequestDto.getDriverId().get());
            bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED,driver.get());
            Optional<Booking> booking=bookingRepository.findById(bookingId);
            return UpdateBookingResponseDto.builder()
                    .bookingId(bookingId)
                    .bookingStatus(booking.get().getBookingStatus())
                    .driver(Optional.ofNullable(booking.get().getDriver()))
                    .build();
    }

    private void raiseRideRequestAsync(RideRequestDto requestDto){
        Call<Boolean> call=uberSocketApi.getNearbyDrivers(requestDto);
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
}

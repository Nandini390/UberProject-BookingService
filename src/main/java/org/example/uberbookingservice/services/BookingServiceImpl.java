package org.example.uberbookingservice.services;

import org.example.uberbookingservice.Apis.LocationServiceApi;
import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberbookingservice.dto.DriverLocationDto;
import org.example.uberbookingservice.dto.NearbyDriverDto;
import org.example.uberbookingservice.repositories.BookingRepository;
import org.example.uberbookingservice.repositories.PassengerRepository;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
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
    private final RestTemplate restTemplate;
//    private static final String LOCATION_SERVICE="http://localhost:7777";
    private final LocationServiceApi locationServiceApi;


    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository, RestTemplate restTemplate, LocationServiceApi locationServiceApi){
        this.passengerRepository=passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate=restTemplate;
        this.locationServiceApi=locationServiceApi;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        Optional<Passenger> passenger=passengerRepository.findById(bookingDetails.getPassengerId());
        Booking booking= Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
//                .endLocation(bookingDetails.getEndLocation())
                .passenger(passenger.get())
                .build();
        Booking newBooking = bookingRepository.save(booking);

        //make an api call to location service to fetch nearby drivers by RestTemplate
//        NearbyDriverDto request= NearbyDriverDto.builder()
//                .latitude(bookingDetails.getStartLocation().getLatitude())
//                .longitude(bookingDetails.getStartLocation().getLongitude())
//                .build();
//        ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(LOCATION_SERVICE+"/api/location/nearby/drivers",request, DriverLocationDto[].class);
//
//        if(result.getStatusCode().is2xxSuccessful() && result.getBody()!=null){
//            List<DriverLocationDto> driverLocations = Arrays.asList(result.getBody());
//            driverLocations.forEach(driverLocationDto -> {
//                System.out.println(driverLocationDto.getDriverId() + " " + "lat: " + driverLocationDto.getLatitude()+ " " + "long: " + driverLocationDto.getLongitude());
//            });
//        }

        //make an api call to location service to fetch nearby drivers by Retrofit
        NearbyDriverDto request= NearbyDriverDto.builder()
                .latitude(bookingDetails.getStartLocation().getLatitude())
                .longitude(bookingDetails.getStartLocation().getLongitude())
                .build();
        processNearbyDriverAsync(request);

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .build();
    }

    private void processNearbyDriverAsync(NearbyDriverDto requestDto){
        Call<DriverLocationDto[]> call= locationServiceApi.getNearbyDrivers(requestDto);
        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if(response.isSuccessful() && response.body()!=null){
                  List<DriverLocationDto> driverLocations = Arrays.asList(response.body());
                  driverLocations.forEach(driverLocationDto -> {
                  System.out.println(driverLocationDto.getDriverId() + " " + "lat: " + driverLocationDto.getLatitude()+ " " + "long: " + driverLocationDto.getLongitude());
                });
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
}

package org.example.uberbookingservice.Apis;

import org.example.uberbookingservice.dto.DriverLocationDto;
import org.example.uberbookingservice.dto.DriverTrackingDto;
import org.example.uberbookingservice.dto.BookingTripMetricsDto;
import org.example.uberbookingservice.dto.NearbyDriverDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;

import java.util.UUID;

public interface LocationServiceApi {
    @POST("/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearbyDriverDto nearbyDriverDto);

    @GET("/api/location/drivers/{driverId}")
    Call<DriverTrackingDto> getDriverLocation(@Path("driverId") UUID driverId);

    @GET("/api/location/bookings/{bookingId}/metrics")
    Call<BookingTripMetricsDto> getBookingTripMetrics(@Path("bookingId") UUID bookingId);
}

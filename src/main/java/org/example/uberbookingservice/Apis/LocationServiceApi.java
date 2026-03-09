package org.example.uberbookingservice.Apis;

import org.example.uberbookingservice.dto.DriverLocationDto;
import org.example.uberbookingservice.dto.NearbyDriverDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LocationServiceApi {
    @POST("/api/location/nearby/drivers")
    Call<DriverLocationDto[]> getNearbyDrivers(@Body NearbyDriverDto nearbyDriverDto);
}

package org.example.uberbookingservice.Apis;

import org.example.uberbookingservice.dto.NearbyDriverDto;
import org.example.uberbookingservice.dto.RideRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UberSocketApi {
    @POST("/api/socket/newRide")
    Call<Boolean> raiseRideRequest(@Body RideRequestDto requestDto);
}

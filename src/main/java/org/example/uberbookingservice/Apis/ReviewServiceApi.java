package org.example.uberbookingservice.Apis;

import org.example.uberbookingservice.dto.CreateReviewRequestDto;
import org.example.uberbookingservice.dto.ReviewResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ReviewServiceApi {
    @POST("/api/v1/review")
    Call<ReviewResponseDto> createReview(@Body CreateReviewRequestDto requestDto);
}

package org.example.uberbookingservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequestDto {
    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "rating is required")
    @DecimalMin(value = "1.0", message = "rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "rating must be at most 5.0")
    private Double rating;

    private UUID bookingId;
}

package org.example.uberbookingservice.dto;

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
public class ReviewResponseDto {
    private UUID id;
    private String content;
    private Double rating;
    private UUID booking;
    private String createdAt;
    private String updatedAt;
}

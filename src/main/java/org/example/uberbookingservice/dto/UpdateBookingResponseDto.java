package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {
    private Long bookingId;
    private BookingStatus bookingStatus;
    private Optional<Driver> driver;
}

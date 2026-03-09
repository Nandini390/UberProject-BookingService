package org.example.uberbookingservice.dto;

import lombok.*;
import org.example.uberprojectentityservice.Models.BookingStatus;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingRequestDto {
    private String bookingStatus;
    private Optional<Long> driverId;
}

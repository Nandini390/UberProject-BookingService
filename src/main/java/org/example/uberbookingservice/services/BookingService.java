package org.example.uberbookingservice.services;

import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberprojectentityservice.Models.Booking;
import org.springframework.stereotype.Service;

import java.awt.print.Book;

public interface BookingService {
    CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails);
}

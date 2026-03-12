package org.example.uberbookingservice.controller;

import org.example.uberbookingservice.dto.CreateBookingDto;
import org.example.uberbookingservice.dto.CreateBookingResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingResponseDto;
import org.example.uberbookingservice.dto.UpdateBookingRequestDto;
import org.example.uberbookingservice.services.BookingService;
import org.example.uberbookingservice.services.WebhookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private final BookingService bookingService;
    private final WebhookService webhookService;

    public BookingController(BookingService bookingService,WebhookService webhookService){
        this.bookingService=bookingService;
        this.webhookService=webhookService;
    }

    @PostMapping
    public ResponseEntity<CreateBookingResponseDto> CreateBooking(@RequestBody CreateBookingDto createBookingDto){
        Map<String,Object> payload = new HashMap<>();
        payload.put("passengerId",createBookingDto.getPassengerId());
        payload.put("Start Location",createBookingDto.getStartLocation());
        payload.put("End Location",createBookingDto.getEndLocation());
        String webhookURL = "http://localhost:6700/webhook";
        webhookService.sendWebhook(webhookURL,payload);
       return new ResponseEntity<>(bookingService.createBooking(createBookingDto), HttpStatus.CREATED);
    }

    @PostMapping("{bookingId}")
    public ResponseEntity<UpdateBookingResponseDto> updateBooking(@RequestBody UpdateBookingRequestDto requestDto, @PathVariable Long bookingId){
      return new ResponseEntity<>(bookingService.updateBooking(requestDto,bookingId),HttpStatus.OK);
    }
}

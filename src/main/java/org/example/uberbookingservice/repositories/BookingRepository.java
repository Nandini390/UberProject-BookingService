package org.example.uberbookingservice.repositories;

import org.example.uberprojectentityservice.Models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {

}

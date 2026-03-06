package org.example.uberbookingservice.repositories;

import org.example.uberprojectentityservice.Models.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<Passenger,Long> {
}

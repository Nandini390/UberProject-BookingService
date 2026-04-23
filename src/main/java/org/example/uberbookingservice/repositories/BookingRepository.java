package org.example.uberbookingservice.repositories;

import jakarta.transaction.Transactional;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

  @Modifying
  @Transactional
  @Query("UPDATE Booking b set b.bookingStatus= :status, b.driver= :driver WHERE b.id= :id")
  int updateBookingStatusAndDriverById(@Param("id") UUID id, @Param("status") BookingStatus status, @Param("driver") Driver driver);

  @Modifying
  @Transactional
  @Query("UPDATE Booking b set b.bookingStatus= :status WHERE b.id= :id")
  int updateBookingStatusById(@Param("id") UUID id, @Param("status") BookingStatus status);

  List<Booking> findAllByPassengerIdOrderByCreatedAtDesc(UUID passengerId);

  List<Booking> findAllByDriverIdOrderByCreatedAtDesc(UUID driverId);
}

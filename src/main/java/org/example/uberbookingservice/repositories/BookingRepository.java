package org.example.uberbookingservice.repositories;

import jakarta.transaction.Transactional;
import org.example.uberprojectentityservice.Models.Booking;
import org.example.uberprojectentityservice.Models.BookingStatus;
import org.example.uberprojectentityservice.Models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

  Page<Booking> findAllByPassengerId(UUID passengerId, Pageable pageable);

  Page<Booking> findAllByDriverId(UUID driverId, Pageable pageable);

  @Query("""
          SELECT b FROM Booking b
          WHERE (:passengerId IS NULL OR b.passenger.id = :passengerId)
            AND (:driverId IS NULL OR b.driver.id = :driverId)
            AND (:status IS NULL OR b.bookingStatus = :status)
            AND (:fromDate IS NULL OR b.createdAt >= :fromDate)
            AND (:toDate IS NULL OR b.createdAt <= :toDate)
          ORDER BY b.createdAt DESC
          """)
  Page<Booking> searchBookings(@Param("passengerId") UUID passengerId,
                               @Param("driverId") UUID driverId,
                               @Param("status") BookingStatus status,
                               @Param("fromDate") LocalDateTime fromDate,
                               @Param("toDate") LocalDateTime toDate,
                               Pageable pageable);

  @Query("""
          SELECT COUNT(b) FROM Booking b
          WHERE (:fromDate IS NULL OR b.createdAt >= :fromDate)
            AND (:toDate IS NULL OR b.createdAt <= :toDate)
          """)
  long countWithinRange(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

  @Query("""
          SELECT COUNT(b) FROM Booking b
          WHERE b.bookingStatus = :status
            AND (:fromDate IS NULL OR b.createdAt >= :fromDate)
            AND (:toDate IS NULL OR b.createdAt <= :toDate)
          """)
  long countByStatusWithinRange(@Param("status") BookingStatus status,
                                @Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate);
}

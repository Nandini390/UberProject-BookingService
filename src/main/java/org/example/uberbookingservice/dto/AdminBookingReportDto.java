package org.example.uberbookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingReportDto {
    private LocalDateTime from;
    private LocalDateTime to;
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private double totalRevenue;
    private double refundedAmount;
}

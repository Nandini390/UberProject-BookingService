package org.example.uberbookingservice.services.Impl;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.razorpay.Utils;
import org.example.uberbookingservice.config.RazorpayProperties;
import org.example.uberprojectentityservice.Models.Booking;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RazorpayGatewayService {

    private final RazorpayProperties razorpayProperties;

    public RazorpayGatewayService(RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    public String getCheckoutKey() {
        ensureConfigured();
        return razorpayProperties.getKeyId().trim();
    }

    public String resolveCurrency(String requestedCurrency) {
        String currency = requestedCurrency == null || requestedCurrency.isBlank()
                ? razorpayProperties.getCurrency()
                : requestedCurrency;
        return currency == null || currency.isBlank() ? "INR" : currency.trim().toUpperCase();
    }

    public int toSubunits(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be positive for Razorpay payment");
        }
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    public Order createOrder(Booking booking, Double amount, String currency) {
        ensureConfigured();
        try {
            JSONObject request = new JSONObject();
            request.put("amount", toSubunits(amount));
            request.put("currency", resolveCurrency(currency));
            request.put("receipt", buildReceipt(booking));

            JSONObject notes = new JSONObject();
            notes.put("bookingId", booking.getId().toString());
            if (booking.getPassenger() != null) {
                notes.put("passengerId", booking.getPassenger().getId().toString());
            }
            request.put("notes", notes);

            return client().orders.create(request);
        } catch (RazorpayException exception) {
            throw new IllegalStateException("Razorpay order creation failed: " + exception.getMessage(), exception);
        }
    }

    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        ensureConfigured();
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", razorpayOrderId);
            options.put("razorpay_payment_id", razorpayPaymentId);
            options.put("razorpay_signature", razorpaySignature);
            return Utils.verifyPaymentSignature(options, razorpayProperties.getKeySecret().trim());
        } catch (RazorpayException exception) {
            throw new IllegalStateException("Razorpay signature verification failed: " + exception.getMessage(), exception);
        }
    }

    public Payment fetchPayment(String razorpayPaymentId) {
        ensureConfigured();
        try {
            return client().payments.fetch(razorpayPaymentId);
        } catch (RazorpayException exception) {
            throw new IllegalStateException("Unable to fetch Razorpay payment: " + exception.getMessage(), exception);
        }
    }

    public Payment capturePayment(String razorpayPaymentId, Double amount, String currency) {
        ensureConfigured();
        try {
            JSONObject request = new JSONObject();
            request.put("amount", toSubunits(amount));
            request.put("currency", resolveCurrency(currency));
            return client().payments.capture(razorpayPaymentId, request);
        } catch (RazorpayException exception) {
            throw new IllegalStateException("Razorpay payment capture failed: " + exception.getMessage(), exception);
        }
    }

    public Refund refundPayment(String razorpayPaymentId, Double amount, String reason) {
        ensureConfigured();
        try {
            JSONObject request = new JSONObject();
            request.put("amount", toSubunits(amount));
            request.put("speed", "normal");
            JSONObject notes = new JSONObject();
            notes.put("reason", reason == null || reason.isBlank() ? "Booking cancelled" : reason);
            request.put("notes", notes);
            return client().payments.refund(razorpayPaymentId, request);
        } catch (RazorpayException exception) {
            throw new IllegalStateException("Razorpay refund failed: " + exception.getMessage(), exception);
        }
    }

    private RazorpayClient client() throws RazorpayException {
        return new RazorpayClient(razorpayProperties.getKeyId().trim(), razorpayProperties.getKeySecret().trim());
    }

    private void ensureConfigured() {
        if (razorpayProperties.getKeyId() == null || razorpayProperties.getKeyId().isBlank()
                || razorpayProperties.getKeySecret() == null || razorpayProperties.getKeySecret().isBlank()) {
            throw new IllegalStateException("Razorpay test credentials are missing. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET before using online payments.");
        }
    }

    private String buildReceipt(Booking booking) {
        String bookingId = booking.getId().toString().replace("-", "");
        return ("booking_" + bookingId).substring(0, Math.min(40, 8 + bookingId.length()));
    }
}

package com.ra.st.controller;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ra.st.model.dto.PaypalRequest;
import com.ra.st.service.PayPalService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    @Autowired
    private PayPalService payPalService;

    @PostMapping()
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody PaypalRequest paypalRequest) {
        Map<String, String> response = new HashMap<>();
        Double amount;

        try {
            amount = Double.parseDouble(paypalRequest.getTotal());
        } catch (NumberFormatException e) {
            log.error("Invalid amount format: {}", paypalRequest.getTotal());
            response.put("message", "Số tiền không hợp lệ");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String approvalUrl = payPalService.createPayment(
                    amount,
                    paypalRequest.getCurrency(),
                    "Thanh toán đơn hàng",
                    "http://localhost:5173/user/cart/checkout/cancel",
                    "http://localhost:5173/user/cart/checkout/success",
                    paypalRequest.getCurrency().equalsIgnoreCase("VND") // Chuyển đổi nếu là VND
            );
            response.put("redirectUrl", approvalUrl);
            return ResponseEntity.ok(response);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi khởi tạo thanh toán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> successPay(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        Map<String, String> response = new HashMap<>();
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                response.put("message", "Thanh toán thành công! Payment ID: " + payment.getId());
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Thanh toán không được phê duyệt!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (PayPalRESTException e) {
            log.error("Error executing PayPal payment: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi thực thi thanh toán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelPay() {
        log.info("PayPal payment cancelled");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Thanh toán đã bị hủy!");
        return ResponseEntity.ok(response);
    }
}
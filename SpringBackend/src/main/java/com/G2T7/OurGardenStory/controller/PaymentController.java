package com.G2T7.OurGardenStory.controller;

import com.G2T7.OurGardenStory.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@RestController
public class PaymentController {
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(path = "/payment")
    public ResponseEntity makePayment(@RequestHeader Map<String, String> headers) {
        try {
            String chargeId = paymentService.checkout(headers.get("username"));
            if (chargeId == null) {
                return ResponseEntity.badRequest().body("An error occurred while trying to create a charge.");
            }
            return ResponseEntity.ok("Success! You have successfully checked out!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

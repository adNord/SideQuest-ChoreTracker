package com.ChoreTracker.ChoreTracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ChoreTracker.ChoreTracker.models.UserPrincipal;
import com.ChoreTracker.ChoreTracker.service.StripeService;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Object> createCheckoutSession(Authentication authentication) throws Exception {
        String userId = getUserId(authentication);
        return stripeService.createCheckoutSession(userId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        return stripeService.handleWebhook(payload, sigHeader);
    }

    private String getUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) return up.getId();
        return "";
    }
}

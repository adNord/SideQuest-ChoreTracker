package com.ChoreTracker.ChoreTracker.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ChoreTracker.ChoreTracker.dtos.CheckoutSessionResponse;
import com.ChoreTracker.ChoreTracker.models.Household;
import com.ChoreTracker.ChoreTracker.models.User;
import com.ChoreTracker.ChoreTracker.repositories.HouseholdRepository;
import com.ChoreTracker.ChoreTracker.repositories.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

@Service
public class StripeService {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.price.id}")
    private String stripePriceId;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    public StripeService(UserRepository userRepository, HouseholdRepository householdRepository) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
    }

    public ResponseEntity<Object> createCheckoutSession(String userId) throws Exception {
        Stripe.apiKey = stripeSecretKey;

        Optional<User> userOptional = userRepository.findById(userId);
        String username = userOptional.map(User::getUsername).orElse("Unknown User");

        String customDescription = "Uppgraderar kontot för användare " + username + " till premium! (användar-ID: " + userId + ")";

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(stripePriceId)
                    .setQuantity(1L)
                    .build()
            )
            .putMetadata("userId", userId)
            .putMetadata("username", username)
            .putMetadata("description", customDescription)
            .build();

        Session session = Session.create(params);
        CheckoutSessionResponse response = new CheckoutSessionResponse(session.getUrl());
        return ResponseEntity.ok(response);
    }

    public void upgradeUserToPremium(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            System.err.println("User not found: " + userId);
            return;
        }

        User user = userOptional.get();
        String householdId = user.getHouseholdId();
        
        if (householdId == null) {
            System.err.println("User has no household: " + userId);
            return;
        }

        Optional<Household> householdOptional = householdRepository.findById(householdId);
        if (householdOptional.isEmpty()) {
            System.err.println("Household not found: " + householdId);
            return;
        }

        Household household = householdOptional.get();
        household.setPremium(true);
        user.setPremium(true);
        householdRepository.save(household);
        userRepository.save(user);
        
        System.out.println("Upgraded household " + householdId + " to premium for user " + user.getUsername());
    }

    public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;
            
            if (dataObjectDeserializer.getObject().isPresent()) {
                stripeObject = dataObjectDeserializer.getObject().get();
            }

            if (stripeObject instanceof Session) {
                Session session = (Session) stripeObject;
                String userId = session.getMetadata().get("userId");
                String username = session.getMetadata().get("username");
                
                System.out.println("Payment successful for user: " + username + " (ID: " + userId + ")");
                
                upgradeUserToPremium(userId);
            }
        }

        return ResponseEntity.ok("Webhook received");
    }
}

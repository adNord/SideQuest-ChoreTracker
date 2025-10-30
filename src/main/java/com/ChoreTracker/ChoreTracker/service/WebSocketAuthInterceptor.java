package com.ChoreTracker.ChoreTracker.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ChoreTracker.ChoreTracker.security.CustomUserDetailsService;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JWTService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketAuthInterceptor(JWTService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Try to get token from Authorization header
            String token = accessor.getFirstNativeHeader("Authorization");
            
            // If not in header, try to get from query parameters (for SockJS)
            if (token == null) {
                String query = (String) accessor.getSessionAttributes().get("query");
                if (query != null && query.contains("token=")) {
                    token = query.substring(query.indexOf("token=") + 6);
                    int ampersandIndex = token.indexOf("&");
                    if (ampersandIndex != -1) {
                        token = token.substring(0, ampersandIndex);
                    }
                }
            }
            
            // Remove "Bearer " prefix if present
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token != null && !token.isEmpty()) {
                try {
                    String username = jwtService.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(authentication);
                        System.out.println("WebSocket authenticated for user: " + username);
                    } else {
                        System.err.println("Invalid JWT token for WebSocket connection");
                    }
                } catch (Exception e) {
                    System.err.println("WebSocket authentication failed: " + e.getMessage());
                }
            } else {
                System.out.println("No JWT token provided for WebSocket connection");
            }
        }
        
        return message;
    }
}


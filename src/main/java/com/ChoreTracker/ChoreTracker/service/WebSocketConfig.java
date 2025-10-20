package com.ChoreTracker.ChoreTracker.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // @Autowired
    // private JWTUtil jwtUtil;

    // @Bean
    // public HandshakeHandler handshakeHandler() {
    //     CustomHandshakeHandler handshakeHandler = new CustomHandshakeHandler();
    //     handshakeHandler.setJwtUtil(jwtUtil);
    //     return handshakeHandler;
    // }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173", 
                        "http://localhost:8080"
                )
                .withSockJS();
    }

}

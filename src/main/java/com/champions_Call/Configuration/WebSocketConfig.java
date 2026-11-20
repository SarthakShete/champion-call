
package com.champions_Call.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Our security interceptor.
    // Spring injects it automatically because it is a @Component.
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // Client connects to this endpoint to establish WebSocket communication.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // /topic: broadcast scoreboard updates to viewers.
        // /queue: send user-specific messages, such as errors.
        registry.enableSimpleBroker("/topic", "/queue");

        // Messages sent to /app are routed to @MessageMapping methods.
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations.
        registry.setUserDestinationPrefix("/user");
    }

    // addition method -> is method used to connece with the interceptor...
    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        // IMPORTANT:
        // Register our interceptor on the incoming STOMP channel.
        //
        // Every incoming CONNECT, SEND and SUBSCRIBE message
        // passes through this interceptor before further processing.
        //
        // This allows us to reject unauthorized scoring requests
        // before they reach CricketWebSocketController.

        // What happens internally?
        // 1. A client sends a STOMP message.
        // 2. Spring receives that message.
        // 3. Spring passes it through WebSocketAuthInterceptor.
        // 4. The interceptor checks authorization.
        // 5. If authorized, the message continues to your controller.
        // 6. If unauthorized, the interceptor rejects it.
        registration.interceptors(webSocketAuthInterceptor);
    }
}

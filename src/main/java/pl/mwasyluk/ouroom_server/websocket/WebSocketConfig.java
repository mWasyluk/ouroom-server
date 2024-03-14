package pl.mwasyluk.ouroom_server.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.exceptions.WebSocketInterceptorException;

@RequiredArgsConstructor

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final SecurityChannelInterceptor securityInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // handshake endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.setErrorHandler(new StompSubProtocolErrorHandler() {
            @Override
            public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable e) {
                if (e.getCause() instanceof WebSocketInterceptorException wse) {
                    e = wse;
                }
                return super.handleClientMessageProcessingError(clientMessage, e);
            }
        });
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(securityInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // endpoint for @MessageMapping methods in @Controller (under consideration)
        registry.setApplicationDestinationPrefixes("/app");
        // endpoint for the default message broker
        registry.enableSimpleBroker("/ws/topic");
    }
}

package pl.mwasyluk.ouroom_server.websocket;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.WebSocketInterceptorException;

@RequiredArgsConstructor
@Slf4j

@Component
public class SecurityChannelInterceptor implements ChannelInterceptor {
    private final AuthenticationManager authenticationManager;

    private UUID retrieveIdFromDestination(String destination) {
        if (destination == null) {
            return null;
        }
        String[] split = destination.split("/");
        if (split.length != 5 || !destination.startsWith("/ws/topic/")) {
            throw new WebSocketInterceptorException("The message destination value does not match the convention: "
                                                    + "/ws/topic/<user_id>/<type>");
        }
        return UUID.fromString(split[3]);
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return null;
        }
        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            String authorization = accessor.getFirstNativeHeader("X-Authorization");
            if (authorization == null || !authorization.startsWith("Basic ")) {
                throw new WebSocketInterceptorException(
                        "CONNECT messages require X-Authorization header with basic auth token.");
            }

            String base64Token = authorization.split(" ")[1];
            byte[] decoded = Base64.getDecoder().decode(base64Token);
            String token = new String(decoded, StandardCharsets.UTF_8);
            int delim = token.indexOf(":");
            if (delim == -1) {
                throw new WebSocketInterceptorException("Invalid basic authentication token");
            }

            UsernamePasswordAuthenticationToken result = UsernamePasswordAuthenticationToken
                    .unauthenticated(token.substring(0, delim), token.substring(delim + 1));
            accessor.setUser(authenticationManager.authenticate(result));
        }

        if (command == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null) {
                throw new WebSocketInterceptorException(
                        "SUBSCRIBE messages require authentication");
            }
            User principal = (User) ((Authentication) accessor.getUser()).getPrincipal();
            UUID principalId = principal.getId();

            String destination = accessor.getDestination();
            UUID destinationId;
            try {
                destinationId = retrieveIdFromDestination(destination);
                assert destinationId != null;
            } catch (Exception e) {
                throw new WebSocketInterceptorException(
                        "Destination for SUBSCRIBE message cannot be empty and has to contain a valid target UUID");
            }

            if (!principalId.equals(destinationId)) {
                throw new WebSocketInterceptorException("Principal with id " + principalId
                                                        + " is not allowed to subscribe to the topic " + destinationId);
            }
        }
        return message;
    }
}

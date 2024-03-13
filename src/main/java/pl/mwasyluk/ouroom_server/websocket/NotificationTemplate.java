package pl.mwasyluk.ouroom_server.websocket;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.mwasyluk.ouroom_server.dto.notification.NotificationView;
import pl.mwasyluk.ouroom_server.exceptions.ConversionException;
import pl.mwasyluk.ouroom_server.repos.MemberRepository;

@RequiredArgsConstructor
@Component
public class NotificationTemplate {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    private void sendToAll(
            @NonNull Collection<UUID> collection,
            @NonNull Topic topic,
            @NonNull String message
    ) {
        for (UUID id : collection) {
            simpMessagingTemplate.convertAndSend("/ws/topic/" + id + "/" + topic.getValue(), message);
        }
    }

    public void notifyAllMembers(@NonNull UUID membershipId, @NonNull Topic topic, @NonNull NotificationView view) {
        Set<UUID> allMemberIds = memberRepository.findAllByMembershipId(membershipId).stream()
                .map(m -> m.getUser().getId())
                .collect(Collectors.toSet());

        notifyAllUsers(allMemberIds, topic, view);
    }

    public void notifyAllUsers(@NonNull Set<UUID> userIdSet, @NonNull Topic topic, @NonNull NotificationView view) {
        try {
            sendToAll(userIdSet, topic, objectMapper.writeValueAsString(view));
        } catch (JsonProcessingException e) {
            throw new ConversionException("Sendable could not be written to a string.");
        }
    }
}

package pl.mwasyluk.ouroom_server.web.http.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.mwasyluk.ouroom_server.data.service.ConversationService;
import pl.mwasyluk.ouroom_server.data.service.ProfileService;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.domain.message.Conversation;
import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;
import pl.mwasyluk.ouroom_server.web.websocket.MessagingTemplate;

@RequiredArgsConstructor

@RestController
@RequestMapping("${apiPrefix}/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final ProfileService profileService;
    private final PrincipalService principalService;
    private final MessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<?> getAllParticipatorConversations() {
        return conversationService.getAllConversationsByParticipator(
                        principalService.getPrincipalProfileId())
                .getResponseEntity();
    }

    @GetMapping("/{conversationIdAsString}")
    public ResponseEntity<?> getConversationById(@PathVariable String conversationIdAsString) {
        return conversationService.getById(
                        principalService.getPrincipalProfileId(),
                        conversationIdAsString)
                .getResponseEntity();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createConversation(@RequestBody Conversation conversation)
            throws JsonProcessingException {
        List<Profile> participators = conversation.getParticipators().stream()
                .map(Profile::getId)
                .map(id -> profileService.getProfileById(id).getBody())
                .filter(obj -> obj instanceof Profile)
                .map(obj -> (Profile) obj)
                .collect(Collectors.toList());

        // return 4** if any of profileService::getProfileById is null (it means that
        // some ids do not exist in the database)
        if (conversation.getParticipators().size() != participators.size()) {
            return ServiceResponse.INCORRECT_ID
                    .getResponseEntity();
        }

        ServiceResponse<?> serviceResponse = conversationService.createConversation(
                principalService.getPrincipalProfileId(),
                participators);

        if (serviceResponse.getBody() instanceof Conversation) {
            Conversation createdConversation = (Conversation) serviceResponse.getBody();
            List<UUID> participatorsIdsList = createdConversation.getParticipators().stream()
                    .map(Profile::getId)
                    .collect(Collectors.toList());
            ObjectMapper mapper = new ObjectMapper();
            messagingTemplate.sendConversationToAll(participatorsIdsList,
                    mapper.writeValueAsString(createdConversation));
        }

        return serviceResponse.getResponseEntity();
    }

    @DeleteMapping(value = "/delete/{conversationIdAsString}")
    public ResponseEntity<?> deleteConversationById(@PathVariable String conversationIdAsString) {
        return conversationService.deleteConversationById(
                        principalService.getPrincipalProfileId(),
                        conversationIdAsString)
                .getResponseEntity();
    }
}

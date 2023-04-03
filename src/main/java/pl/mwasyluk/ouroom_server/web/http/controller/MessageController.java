package pl.mwasyluk.ouroom_server.web.http.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.mwasyluk.ouroom_server.data.service.ConversationService;
import pl.mwasyluk.ouroom_server.data.service.MessageService;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.domain.message.Message;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;
import pl.mwasyluk.ouroom_server.web.websocket.MessagingTemplate;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("${apiPrefix}/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final PrincipalService principalService;
    private final MessagingTemplate messagingTemplate;

    @PostMapping("/send/conversation/{conversationIdAsString}")
    public ResponseEntity<?> addMessageToConversation(@PathVariable String conversationIdAsString,
            @RequestBody Message.TempMessage message) throws JsonProcessingException {
        Message messageToPersist = new Message(principalService.getPrincipalProfileId(), message);
        messageToPersist.setSentDate(new Date());

        ServiceResponse<?> serviceResponse = conversationService.addMessageToConversationById(
                principalService.getPrincipalProfileId(),
                conversationIdAsString,
                messageToPersist);

        if (serviceResponse.getBody() instanceof Message) {
            Message persistentMessage = (Message) serviceResponse.getBody();
            ObjectMapper mapper = new ObjectMapper();
            messagingTemplate.sendMessageToAllConversationParticipators(
                    persistentMessage.getConversation().getId(),
                    mapper.writeValueAsString(persistentMessage));
        }

        return serviceResponse.getResponseEntity();
    }

    @GetMapping("/conversation/{conversationIdAsString}")
    public ResponseEntity<?> getMessagesByConversationId(@PathVariable String conversationIdAsString,
            @RequestParam(name = "page", defaultValue = "0") Integer page) {
        int pageSize = 30;

        return messageService.getAllMessagesByConversationId(
                principalService.getPrincipalProfileId(),
                conversationIdAsString,
                PageRequest.of(page, pageSize))
                .getResponseEntity();
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateMessage(@RequestBody Message updatedMessage) {
        return messageService.updateMessage(
                principalService.getPrincipalProfileId(),
                updatedMessage)
                .getResponseEntity();
    }

    @DeleteMapping("/delete/{messageIdAsString}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageIdAsString) {
        return messageService.deleteMessage(
                principalService.getPrincipalProfileId(),
                messageIdAsString)
                .getResponseEntity();
    }
}

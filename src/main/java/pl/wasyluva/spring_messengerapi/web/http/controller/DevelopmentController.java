package pl.wasyluva.spring_messengerapi.web.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wasyluva.spring_messengerapi.data.service.AccountService;
import pl.wasyluva.spring_messengerapi.data.service.ConversationService;
import pl.wasyluva.spring_messengerapi.data.service.MessageService;
import pl.wasyluva.spring_messengerapi.data.service.ProfileService;

@Profile("dev")
@RequiredArgsConstructor
@RestController
@RequestMapping("${apiPrefix}/dev/all")
public class DevelopmentController {
    private final AccountService accountService;
    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts(){
        return accountService.getAllAccounts().getResponseEntity();
    }

    private final ConversationService conversationService;
    @GetMapping("/conversations")
    public ResponseEntity<?> getAllConversations(){
        return conversationService.getAll().getResponseEntity();
    }

    private final MessageService messageService;
    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages(){
        return messageService.getAllPersistedMessages().getResponseEntity();
    }

    private final ProfileService profileService;
    @GetMapping("/profiles")
    public ResponseEntity<?> getAllUserProfiles(){
        return profileService.getAllProfiles().getResponseEntity();
    }
}

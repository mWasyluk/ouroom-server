package pl.mwasyluk.ouroom_server.web.http.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.data.service.AccountService;
import pl.mwasyluk.ouroom_server.data.service.ConversationService;
import pl.mwasyluk.ouroom_server.data.service.MessageService;
import pl.mwasyluk.ouroom_server.data.service.ProfileService;

@Profile("dev")
@RequiredArgsConstructor
@RestController
@RequestMapping("${apiPrefix}/dev/all")
public class DevelopmentController {
    private final AccountService accountService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ProfileService profileService;

    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts() {
        return accountService.getAllAccounts().getResponseEntity();
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getAllConversations() {
        return conversationService.getAll().getResponseEntity();
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages() {
        return messageService.getAllPersistedMessages().getResponseEntity();
    }

    @GetMapping("/profiles")
    public ResponseEntity<?> getAllUserProfiles() {
        return profileService.getAllProfiles().getResponseEntity();
    }
}

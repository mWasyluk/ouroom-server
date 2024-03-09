package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collection;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import pl.mwasyluk.ouroom_server.dto.chat.ChatDetailsView;
import pl.mwasyluk.ouroom_server.dto.chat.ChatForm;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.services.chat.ChatService;

@Tag(name = "Chat API")
@RequiredArgsConstructor

@RestController
@RequestMapping("${server.api.prefix}/chats")
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Get all memberships of principal")
    @GetMapping
    public ResponseEntity<Collection<ChatPresentableView>> readAllWithPrincipal() {
        return ResponseEntity.ok(chatService.readAllWithPrincipal());
    }

    @Operation(summary = "Get chat details by ID")
    @GetMapping("/details")
    public ResponseEntity<ChatDetailsView> readDetails(
            @RequestParam UUID chatId
    ) {
        return ResponseEntity.ok(chatService.read(chatId));
    }

    @Operation(summary = "Create new chat")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatPresentableView> create(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) MultipartFile file
    ) {
        ChatForm form = new ChatForm();
        form.setName(name);
        form.setFile(file);

        return ResponseEntity.ok(chatService.create(form));
    }

    @Operation(summary = "Update chat presentable by ID")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatPresentableView> update(
            @RequestParam UUID chatId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) boolean clearImage,
            @RequestParam(required = false) MultipartFile file
    ) {
        ChatForm form = new ChatForm();
        form.setChatId(chatId);
        form.setName(name);
        form.setClearImage(clearImage);
        form.setFile(file);

        return ResponseEntity.ok(chatService.update(form));
    }

    @Operation(summary = "Delete chat by ID")
    @DeleteMapping
    public ResponseEntity<?> delete(
            @RequestParam UUID chatId
    ) {
        chatService.delete(chatId);
        return ResponseEntity.ok().build();
    }
}

package pl.mwasyluk.ouroom_server.controllers;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.dto.chat.ChatPresentableView;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.repos.ChatRepository;
import pl.mwasyluk.ouroom_server.repos.UserRepository;
import pl.mwasyluk.ouroom_server.services.user.UserService;

import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validateAdminPrincipal;

@Tag(name = "Admin API")
@RequiredArgsConstructor

@RestController
@RequestMapping("${server.api.prefix}/admin")
public class AdminController {
    private final UserService userService;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get all existing user details or particular by ID")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUserDetails(
            @RequestParam(required = false) UUID userId
    ) {
        validateAdminPrincipal();

        if (userId == null) {
            return getAllUserDetails();
        } else {
            return getUserDetailsByUserId(userId);
        }
    }

    private ResponseEntity<?> getAllUserDetails() {
        List<User> all = userRepository.findAll();
        return ResponseEntity.ok(all.stream().map(UserDetailsView::new).collect(Collectors.toList()));
    }

    private ResponseEntity<?> getUserDetailsByUserId(UUID userId) {
        return ResponseEntity.ok(userService.readDetails(userId));
    }

    @Operation(summary = "Disable user")
    @DeleteMapping("/users")
    public ResponseEntity<?> disableById(
            @RequestParam UUID userId
    ) {
        validateAdminPrincipal();

        userService.disable(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all existing chat presentables")
    @GetMapping("/chats")
    public ResponseEntity<Collection<ChatPresentableView>> readAll() {
        validateAdminPrincipal();

        return ResponseEntity.ok(chatRepository.findAll().stream()
                .map(ChatPresentableView::new)
                .collect(Collectors.toList()));
    }
}

package pl.mwasyluk.ouroom_server.controllers;

import java.util.EnumSet;
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

import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsForm;
import pl.mwasyluk.ouroom_server.dto.user.UserDetailsView;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableForm;
import pl.mwasyluk.ouroom_server.dto.user.UserPresentableView;
import pl.mwasyluk.ouroom_server.services.user.UserService;

@Tag(name = "User API")
@RequiredArgsConstructor

@RestController
@RequestMapping("${server.api.prefix}/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Create new user")
    @PostMapping
    public ResponseEntity<UserDetailsView> createNewUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) EnumSet<UserAuthority> authorities
    ) {
        UserDetailsForm form = new UserDetailsForm();
        form.setEmail(email);
        form.setPassword(password);
        form.setAuthorities(authorities);

        return ResponseEntity.ok(userService.create(form));
    }

    @Operation(summary = "Get user's presentable")
    @GetMapping
    public ResponseEntity<UserPresentableView> getPresentableByUserId(
            @RequestParam(required = false) UUID userId
    ) {
        return ResponseEntity.ok(userService.readPresentable(userId));
    }

    @Operation(summary = "Get principal's details")
    @GetMapping("/details")
    public ResponseEntity<UserDetailsView> getPrincipalDetails() {
        return ResponseEntity.ok(userService.readDetails(null));
    }

    @Operation(summary = "Update principal's details")
    @PatchMapping("/details")
    public ResponseEntity<UserDetailsView> updatePrincipalDetails(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password
    ) {
        UserDetailsForm form = new UserDetailsForm();
        form.setEmail(email);
        form.setPassword(password);

        return ResponseEntity.ok(userService.updateAccount(form));
    }

    @Operation(summary = "Update principal's profile")
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserPresentableView> updatePrincipalProfile(
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String lastname,
            @RequestParam(required = false) boolean clearImage,
            @RequestParam(required = false) MultipartFile imageFile
    ) {
        UserPresentableForm form = new UserPresentableForm();
        form.setFirstname(firstname);
        form.setLastname(lastname);
        form.setClearImage(clearImage);
        form.setFile(imageFile);

        return ResponseEntity.ok(userService.updateProfile(form));
    }

    @Operation(summary = "Disable principal")
    @DeleteMapping
    public ResponseEntity<?> deletePrincipal() {
        userService.disable(null);
        return ResponseEntity.ok().build();
    }
}

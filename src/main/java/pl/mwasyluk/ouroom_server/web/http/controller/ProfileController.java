package pl.mwasyluk.ouroom_server.web.http.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import pl.mwasyluk.ouroom_server.data.service.ProfileService;
import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("${apiPrefix}/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final PrincipalService principalService;

    @GetMapping
    public ResponseEntity<?> getPrincipalProfile() {
        return profileService.getProfileByAccountId(principalService.getPrincipalAccountId())
                .getResponseEntity();
    }

    @GetMapping("/{userUuid}")
    public ResponseEntity<?> getUserProfileByUserId(@PathVariable String userUuid) {
        return profileService.getProfileById(userUuid)
                .getResponseEntity();
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProfilesByName(@RequestParam(name = "q") String query) {
        return profileService.getProfilesByNameQuery(query)
                .getResponseEntity();
    }

    @PostMapping(value = "/create", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createProfile(@RequestParam("profile") String profileString,
            @RequestParam(name = "avatar", required = false) MultipartFile avatarFile)
            throws IOException, HttpMediaTypeNotSupportedException {
        Profile profile = new ObjectMapper().readValue(profileString, Profile.class);
        return profileService.createProfile(principalService.getPrincipalAccountId(), profile, avatarFile)
                .getResponseEntity();
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUserProfile(@RequestBody Profile profile) {
        return profileService.updateProfile(principalService.getPrincipalProfileId(), profile)
                .getResponseEntity();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProfile() {
        return profileService.deleteProfile(principalService.getPrincipalAccountId())
                .getResponseEntity();
    }
}

package pl.mwasyluk.ouroom_server.web.http.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

import pl.mwasyluk.ouroom_server.data.service.ProfileService;
import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;

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

    @PostMapping(value = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
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

package pl.mwasyluk.ouroom_server.web.http.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.data.service.AvatarService;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.domain.userdetails.ProfileAvatar;
import pl.mwasyluk.ouroom_server.util.UuidUtils;
import pl.mwasyluk.ouroom_server.web.http.support.PrincipalService;

@RequiredArgsConstructor
@RestController
@RequestMapping("${apiPrefix}/images/avatars")
public class AvatarController {
    private final AvatarService avatarService;
    private final PrincipalService principalService;

    @GetMapping(value = "/default")
    public ResponseEntity<?> getDefaultAvatarImage() {
        InputStream is = this.getClass().getResourceAsStream("/static/default-avatar.png");
        byte[] defaultAvatarBytesArray;
        try {
            defaultAvatarBytesArray = StreamUtils.copyToByteArray(is);
        } catch (IOException e) {
            return ServiceResponse.NOT_FOUND.getResponseEntity();
        }

        if (defaultAvatarBytesArray.length < 1) {
            return ServiceResponse.NOT_FOUND.getResponseEntity();
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(defaultAvatarBytesArray);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getAvatarImage(@PathVariable(name = "id") String avatarUuidAsString,
            HttpServletResponse response) {
        if (!UuidUtils.isStringCorrectUuid(avatarUuidAsString)) {
            return ServiceResponse.INCORRECT_ID.getResponseEntity();
        }
        ServiceResponse<?> avatarServiceResponse = avatarService.getAvatarByUuid(UUID.fromString(avatarUuidAsString));
        if (!(avatarServiceResponse.getBody() instanceof ProfileAvatar)) {
            return avatarServiceResponse.getResponseEntity();
        }
        ProfileAvatar avatar = (ProfileAvatar) avatarServiceResponse.getBody();

        return ResponseEntity.ok().contentType(avatar.getMediaType()).body(avatar.getBytesArray());
    }

    @PostMapping(value = "/update", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateProfileAvatar(@RequestParam("image") MultipartFile multipartFile,
            HttpServletRequest request) throws IOException, HttpMediaTypeNotSupportedException {
        return avatarService.updateProfileAvatar(principalService.getPrincipalProfileId(), multipartFile)
                .getResponseEntity();
    }
}

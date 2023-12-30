package pl.mwasyluk.ouroom_server.data.service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.data.repository.AvatarRepository;
import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;
import pl.mwasyluk.ouroom_server.domain.userdetails.Profile;
import pl.mwasyluk.ouroom_server.domain.userdetails.ProfileAvatar;

@RequiredArgsConstructor
@Service
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final ProfileService profileService;

    public ServiceResponse<?> getAvatarByUuid(UUID avatarUuid) {
        Optional<ProfileAvatar> avatarByUuid = avatarRepository.findById(avatarUuid);

        if (!avatarByUuid.isPresent() || avatarByUuid.get().getBytesArray() == null) {
            return ServiceResponse.NOT_FOUND;
        }

        return new ServiceResponse<>(avatarByUuid.get(), HttpStatus.OK);
    }

    public ServiceResponse<?> updateProfileAvatar(@NonNull UUID requestingProfileUuid, MultipartFile file)
            throws HttpMediaTypeNotSupportedException, IOException {
        if (file.isEmpty() || file.getContentType() == null
            || !(ProfileAvatar.SUPPORTED_MEDIA_TYPES_VALUES.contains(file.getContentType()))) {
            throw new HttpMediaTypeNotSupportedException("");
        }

        MediaType mediaType = MediaType.valueOf(file.getContentType());
        ProfileAvatar avatar = new ProfileAvatar(file.getBytes(), mediaType);
        ServiceResponse<?> profileServiceResponse = profileService.getProfileById(requestingProfileUuid);

        if (!(profileServiceResponse.getBody() instanceof Profile)) {
            return profileServiceResponse;
        }

        Profile requestingProfile = (Profile) profileServiceResponse.getBody();
        requestingProfile.setAvatar(avatar);

        return profileService.updateProfile(requestingProfileUuid, requestingProfile);
    }
}

package pl.mwasyluk.ouroom_server.services.media;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.repos.MediaRepository;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static pl.mwasyluk.ouroom_server.services.PrincipalValidator.validatePrincipal;

@RequiredArgsConstructor

@Service
public class DefaultMediaService implements MediaService {
    private final MediaRepository mediaRepository;

    @Override
    public @NonNull Media read(@NonNull UUID mediaId) {
        // validation
        validatePrincipal();

        // verification
        Optional<Media> mediaOptional = mediaRepository.findById(mediaId);
        if (mediaOptional.isEmpty()) {
            throw new ServiceException(NOT_FOUND, "Media with the given ID does not exist.");
        }

        // execution
        return mediaOptional.get();
    }

    @Override
    public @NonNull Media create(MultipartFile file) {
        // validation
        validatePrincipal();
        if (file == null || file.isEmpty()) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "Media requires a non-empty file.");
        }

        // verification
        Media result;
        try {
            result = Media.of(DataSource.of(file.getBytes()));
        } catch (Exception e) {
            throw new ServiceException(UNPROCESSABLE_ENTITY, "The file's byte array is not a valid media content.");
        }

        // execution
        return mediaRepository.save(result);
    }
}

package pl.mwasyluk.ouroom_server.services.media;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.media.Media;

public interface MediaService {
    @NonNull Media read(@NonNull UUID mediaId);
    @NonNull Media create(MultipartFile file);
}

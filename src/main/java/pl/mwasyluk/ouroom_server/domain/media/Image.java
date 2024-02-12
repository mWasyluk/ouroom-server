package pl.mwasyluk.ouroom_server.domain.media;

import java.util.Set;

import org.springframework.http.MediaType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Image extends BaseMedia {
    public static Set<MediaType> SUPPORTED_MEDIA_TYPES = Set.of(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.IMAGE_GIF
    );

    protected Image(@NonNull MediaType mediaType, byte @NonNull [] bytes) {
        super(mediaType, bytes);
    }

    @Override
    protected void validate() {
        super.validate();
        if (!SUPPORTED_MEDIA_TYPES.contains(this.getType())) {
            throw new IllegalArgumentException(
                    "Cannot instantiate Image object with " + this.getType() + " media type.");
        }
    }
}

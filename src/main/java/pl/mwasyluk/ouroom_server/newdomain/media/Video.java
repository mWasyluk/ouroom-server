package pl.mwasyluk.ouroom_server.newdomain.media;

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
public class Video extends BaseMedia {
    public static Set<MediaType> SUPPORTED_MEDIA_TYPES = Set.of(
            VideoMediaType.MP4,
            VideoMediaType.MPEG
    );

    protected Video(@NonNull MediaType mediaType, byte @NonNull [] content) {
        super(mediaType, content);
    }

    @Override
    protected void validate() {
        super.validate();
        if (!SUPPORTED_MEDIA_TYPES.contains(this.getType())) {
            throw new IllegalArgumentException(
                    "Cannot instantiate Video object with " + this.getType() + " media type.");
        }
    }
}

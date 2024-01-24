package pl.mwasyluk.ouroom_server.newdomain.media;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Entity;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
public class Video extends BaseMedia {
    public static boolean isFormatSupported(MediaGroup.Format format) {
        return MediaGroup.VIDEO.getFormats().contains(format);
    }

    public static boolean isFormatSupported(String format) {
        Optional<MediaGroup.Format> match = MediaGroup.VIDEO.getFormats()
                .stream()
                .filter(f -> f.getMimeType().equals(format))
                .findFirst();
        return match.isPresent();
    }

    public Video(MediaGroup.Format format, byte[] bytes) {
        super(format, bytes);
        if (!isFormatSupported(format)) {
            throw new IllegalArgumentException("Format '" + format + "' is not supported for images.");
        }
    }

    @Override
    public @NonNull MediaGroup getMediaGroup() {
        return MediaGroup.VIDEO;
    }
}

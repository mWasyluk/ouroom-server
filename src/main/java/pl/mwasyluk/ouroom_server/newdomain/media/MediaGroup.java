package pl.mwasyluk.ouroom_server.newdomain.media;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public enum MediaGroup {
    IMAGE(Format.JPEG, Format.PNG, Format.GIF),
    VIDEO(Format.MP4, Format.MPEG);

    @Getter
    private final Set<Format> formats;

    MediaGroup(Format... formats) {
        this.formats = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(formats)));
    }

    public enum Format {
        JPEG("image/jpeg"),
        PNG("image/png"),
        GIF("image/gif"),
        MP4("video/mp4"),
        MPEG("video/mpeg");

        public static boolean isFormatSupported(String format) {
            return Arrays.stream(Format.values())
                    .map(Enum::toString)
                    .anyMatch(m -> m.equals(format.toUpperCase()));
        }

        @Getter
        private final String mimeType;

        Format(String t) {
            mimeType = t;
        }
    }
}

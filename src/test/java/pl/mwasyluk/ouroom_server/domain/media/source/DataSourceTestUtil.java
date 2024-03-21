package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import pl.mwasyluk.ouroom_server.SpringMessengerApiApplication;

public class DataSourceTestUtil {
    //    public static final InputStream PNG_BYTES_STREAM
//            = Objects.requireNonNull(DataSourceTestUtil.class.getResourceAsStream("/media/png.png"));
//    public static final InputStream JPEG_BYTES_STREAM
//            = Objects.requireNonNull(DataSourceTestUtil.class.getResourceAsStream("/media/jpg.jpg"));
//    public static final InputStream GIF_BYTES_STREAM
//            = Objects.requireNonNull(DataSourceTestUtil.class.getResourceAsStream("/media/gif.gif"));
//    public static final InputStream MP4_BYTES_STREAM
//            = Objects.requireNonNull(DataSourceTestUtil.class.getResourceAsStream("/media/mp4.mp4"));
//
    public static final String JPEG_URL_VALUE = "https://samplelib.com/lib/preview/jpeg/sample-clouds-400x300.jpg";
    public static final String PNG_URL_VALUE = "https://samplelib.com/lib/preview/png/sample-clouds2-400x300.png";
    public static final String GIF_URL_VALUE = "https://samplelib.com/lib/preview/gif/sample-animated-400x300.gif";
    public static final String MP4_URL_VALUE = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4";

    // get access to all files in the resources/media folder and return them as a list of InputStreams
    public static final InputStream JPEG_BYTES_STREAM;
    public static final InputStream PNG_BYTES_STREAM;
    public static final InputStream GIF_BYTES_STREAM;
    public static final InputStream MP4_BYTES_STREAM;

    public static final DataSource EX_JPEG_SOURCE;
    public static final DataSource EX_PNG_SOURCE;
    public static final DataSource EX_GIF_SOURCE;
    public static final DataSource EX_MP4_SOURCE;
    public static final DataSource IN_JPEG_SOURCE;
    public static final DataSource IN_PNG_SOURCE;
    public static final DataSource IN_GIF_SOURCE;
    public static final DataSource IN_MP4_SOURCE;

    public static final byte[] PNG_BYTES;
    public static final byte[] JPEG_BYTES;
    public static final byte[] GIF_BYTES;
    public static final byte[] MP4_BYTES;

    static {
        try {
            PNG_BYTES_STREAM =
                    Objects.requireNonNull(SpringMessengerApiApplication.class.getResource("/media/png.png"))
                            .openStream();
            JPEG_BYTES_STREAM =
                    Objects.requireNonNull(SpringMessengerApiApplication.class.getResource("/media/jpg.jpg"))
                            .openStream();
            GIF_BYTES_STREAM =
                    Objects.requireNonNull(SpringMessengerApiApplication.class.getResource("/media/gif.gif"))
                            .openStream();
            MP4_BYTES_STREAM =
                    Objects.requireNonNull(SpringMessengerApiApplication.class.getResource("/media/mp4.mp4"))
                            .openStream();

            PNG_BYTES = PNG_BYTES_STREAM.readAllBytes();
            JPEG_BYTES = JPEG_BYTES_STREAM.readAllBytes();
            GIF_BYTES = GIF_BYTES_STREAM.readAllBytes();
            MP4_BYTES = MP4_BYTES_STREAM.readAllBytes();

            EX_JPEG_SOURCE = DataSource.of(JPEG_URL_VALUE);
            EX_PNG_SOURCE = DataSource.of(PNG_URL_VALUE);
            EX_GIF_SOURCE = DataSource.of(GIF_URL_VALUE);
            EX_MP4_SOURCE = DataSource.of(MP4_URL_VALUE);

            IN_JPEG_SOURCE = DataSource.of(JPEG_BYTES);
            IN_PNG_SOURCE = DataSource.of(PNG_BYTES);
            IN_GIF_SOURCE = DataSource.of(GIF_BYTES);
            IN_MP4_SOURCE = DataSource.of(MP4_BYTES);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

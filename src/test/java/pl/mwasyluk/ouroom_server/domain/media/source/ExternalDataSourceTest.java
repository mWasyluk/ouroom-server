package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import pl.mwasyluk.ouroom_server.domain.media.VideoMediaType;
import pl.mwasyluk.ouroom_server.exceptions.ConversionException;

import static org.junit.jupiter.api.Assertions.*;

class ExternalDataSourceTest {
    private static final URL JPEG_URL;
    private static final URL PNG_URL;
    private static final URL GIF_URL;
    private static final URL MP4_URL;

    static {
        try {
            PNG_URL = new URL(DataSourceTestUtil.PNG_URL_VALUE);
            JPEG_URL = new URL(DataSourceTestUtil.JPEG_URL_VALUE);
            GIF_URL = new URL(DataSourceTestUtil.GIF_URL_VALUE);
            MP4_URL = new URL(DataSourceTestUtil.MP4_URL_VALUE);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    static class ImageUrlArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(PNG_URL),
                    Arguments.of(JPEG_URL),
                    Arguments.of(GIF_URL),
                    Arguments.of(MP4_URL)
            );
        }
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when null given")
        void throwsExceptionWhenNullGiven() {
            assertThrowsExactly(NullPointerException.class, () -> new ExternalDataSource(null));
        }

        @ParameterizedTest
        @ArgumentsSource(ImageUrlArguments.class)
        @DisplayName("does not throw exception when valid url given")
        void doesNotThrowExceptionWhenValidUrlGiven(URL url) {
            assertDoesNotThrow(() -> new ExternalDataSource(url));
        }
    }

    @Nested
    @DisplayName("getContentType method")
    class GetContentTypeMethodTest {
        @Test
        @DisplayName("returns correct type")
        void returnsCorrectType() {
            DataSource jpeg = new ExternalDataSource(JPEG_URL);
            DataSource png = new ExternalDataSource(PNG_URL);
            DataSource gif = new ExternalDataSource(GIF_URL);
            DataSource mp4 = new ExternalDataSource(MP4_URL);

            assertAll(() -> {
                assertEquals(MediaType.IMAGE_JPEG_VALUE, jpeg.getContentType());
                assertEquals(MediaType.IMAGE_PNG_VALUE, png.getContentType());
                assertEquals(MediaType.IMAGE_GIF_VALUE, gif.getContentType());
                assertEquals(VideoMediaType.VIDEO_MP4.toString(), mp4.getContentType());
            });
        }

        @Test
        @DisplayName("returns octet-stream type when url does not exist")
        void returnsOctetStreamTypeWhenUrlDoesNotExist() throws MalformedURLException {
            DataSource dataSource = new ExternalDataSource(new URL("http://locast.asdgasadg/api"));

            assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, dataSource.getContentType());
        }
    }

    @Nested
    @DisplayName("getInputStream method")
    class GetInputStreamMethodTest {
        @ParameterizedTest
        @ArgumentsSource(ImageUrlArguments.class)
        @DisplayName("returns valid stream")
        void returnsValidStream(URL url) throws IOException {
            DataSource source = new ExternalDataSource(url);

            int available;
            available = source.getInputStream().available();

            assertTrue(available > 12);
        }

        @Test
        @DisplayName("throws exception when url does not exist")
        void throwsExceptionWhenUrlDoesNotExist() throws MalformedURLException {
            DataSource dataSource = new ExternalDataSource(new URL("http://locast.asdgasadg/api"));

            assertThrowsExactly(ConversionException.class, dataSource::getInputStream);
        }

        @Test
        @DisplayName("throws exception when url is not valid")
        void throwsExceptionWhenUrlIsNotValid() throws MalformedURLException {
            DataSource dataSource =
                    new ExternalDataSource(new URL("https:/samplelib.com/lib/preview/gif/sample-animated-400x300.gif"));

            assertThrowsExactly(ConversionException.class, dataSource::getInputStream);
        }
    }
}

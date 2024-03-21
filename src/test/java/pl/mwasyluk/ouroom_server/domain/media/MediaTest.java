package pl.mwasyluk.ouroom_server.domain.media;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.stream.Stream;

import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.*;
import static pl.mwasyluk.ouroom_server.domain.media.VideoMediaType.*;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.*;

class MediaTest {
    public static class SupportedDataSources implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(IMAGE_JPEG, EX_JPEG_SOURCE),
                    Arguments.of(IMAGE_PNG, EX_PNG_SOURCE),
                    Arguments.of(IMAGE_GIF, EX_GIF_SOURCE),
                    Arguments.of(VIDEO_MP4, EX_MP4_SOURCE),
                    Arguments.of(IMAGE_JPEG, IN_JPEG_SOURCE),
                    Arguments.of(IMAGE_PNG, IN_PNG_SOURCE)

                    // FIXME: disabled due to issue [#5](https://github.com/mWasyluk/ouroom-server/issues/5);
//                    Arguments.of(VIDEO_MP4, IN_MP4_SOURCE)
            );
        }
    }

    public static class UnsupportedDataSources implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
                throws MalformedURLException {
            return Stream.of(
                    Arguments.of(DataSource.of("https://localhost:7777/com")),
                    Arguments.of(DataSource.of("https://google.com")),
                    Arguments.of(DataSource.of("https://youtube.com")),
                    Arguments.of(DataSource.of("mediatestdatawitest".getBytes()))
            );
        }
    }

    @Nested
    @DisplayName("of method")
    class OfMethodTest {
        @ParameterizedTest
        @DisplayName("returns Image instance when image media type given")
        @ArgumentsSource(ImageTest.ImageDataSources.class)
        void returnsImageInstanceWhenImageMediaTypeGiven(DataSource source) {
            Media media = Media.of(source);

            assertTrue(media instanceof Image);
        }

        // FIXME: disabled due to issue [#5](https://github.com/mWasyluk/ouroom-server/issues/5)
        @Disabled("Disabled due to issue #5")
        @ParameterizedTest
        @DisplayName("returns Video instance when video media type given")
        @ArgumentsSource(VideoTest.VideoDataSources.class)
        void returnsVideoInstanceWhenVideoMediaTypeGiven(DataSource source) {
            Media media = Media.of(source);

            assertTrue(media instanceof Video);
        }

        @ParameterizedTest
        @DisplayName("throws exception when media type not supported")
        @ArgumentsSource(UnsupportedDataSources.class)
        void throwsExceptionWhenMediaTypeNotSupported(DataSource source) {
            assertThrowsExactly(InitializationException.class,
                    () -> Media.of(source));
        }
    }

    @Nested
    @DisplayName("getType method")
    class GetTypeMethodTest {
        @ParameterizedTest
        @DisplayName("returns correct type when media type is supported")
        @ArgumentsSource(SupportedDataSources.class)
        void returnsCorrectTypeWhenMediaTypeIsSupported(MediaType expectedType, DataSource source) {
            Media media;
            try {
                media = new Image(source);
            } catch (Exception e1) {
                media = new Video(source);
            }

            assertEquals(expectedType, media.getType());
        }

        @Test
        @DisplayName("returns exact type when media type is known but not supported")
        void returnsExactTypeWhenMediaTypeIsKnownButNotSupported() {
            DataSource mockDataSource = new DataSource() {
                @Override
                public InputStream getInputStream() {
                    return null;
                }

                @Override
                public String getContentType() {
                    return "application/pdf";
                }
            };

            Media media = new Media(mockDataSource) {
                @Override
                protected void validate() {
                    // do nothing
                }
            };

            assertEquals(APPLICATION_PDF, media.getType());
        }

        @Test
        @DisplayName("throws exception when media type has not been found")
        void throwsExceptionWhenMediaTypeIsNotSupported() {
            Media media = new Media(DataSource.of("mediatestdatawitest".getBytes())) {
                @Override
                protected void validate() {
                    // do nothing
                }
            };

            assertThrowsExactly(InvalidMediaTypeException.class, media::getType);
        }
    }
}

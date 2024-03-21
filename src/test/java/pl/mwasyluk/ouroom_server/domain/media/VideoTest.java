package pl.mwasyluk.ouroom_server.domain.media;

import java.io.IOException;
import java.util.stream.Stream;

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
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.*;

class VideoTest {
    Video newOf(DataSource source) {
        return new Video(source);
    }

    static class VideoDataSources implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(EX_MP4_SOURCE),
                    Arguments.of(IN_MP4_SOURCE)
            );
        }
    }

    static class NonVideoDataSources implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(EX_JPEG_SOURCE),
                    Arguments.of(EX_PNG_SOURCE),
                    Arguments.of(EX_GIF_SOURCE),
                    Arguments.of(IN_JPEG_SOURCE),
                    Arguments.of(IN_PNG_SOURCE)
            );
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when null data source given")
        void throwsExceptionWhenNullDataSourceGiven() {
            assertThrowsExactly(NullPointerException.class, () -> newOf(null));
        }

        @Test
        @DisplayName("throws exception when given media type not supported")
        void throwsExceptionWhenGivenMediaTypeNotSupported() {
            assertThrowsExactly(InitializationException.class,
                    () -> newOf(DataSource.of("https://localhost:3233/test")));
        }

        @ParameterizedTest
        @DisplayName("throws exception when given data source is not a video")
        @ArgumentsSource(NonVideoDataSources.class)
        void throwsExceptionWhenGivenDataSourceIsNotAVideo(DataSource source) {
            assertThrowsExactly(InitializationException.class, () -> newOf(source));
        }

        // FIXME: disabled due to issue [#5](https://github.com/mWasyluk/ouroom-server/issues/5)
        @Disabled("Disabled due to issue #5")
        @ParameterizedTest
        @DisplayName("does not throw exception when given media type is a video")
        @ArgumentsSource(VideoDataSources.class)
        void doesNotThrowExceptionWhenGivenMediaTypeIsAVideo(DataSource source) {
            assertDoesNotThrow(() -> newOf(source));
        }
    }
}

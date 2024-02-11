package pl.mwasyluk.ouroom_server.newdomain.media;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class ImageTest {
    private static final byte[] MOCK_CONTENT = "mock".getBytes();

    Image newOf(MediaType mediaType, byte[] content) {
        return new Image(mediaType, content);
    }

    static class SupportedMediaTypes implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Image.SUPPORTED_MEDIA_TYPES.stream().map(Arguments::of);
        }
    }

    static class UnsupportedMediaTypes implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Video.SUPPORTED_MEDIA_TYPES.stream().map(Arguments::of);
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when null media type given")
        void throwsExceptionWhenNullMediaTypeGiven() {
            assertThrowsExactly(NullPointerException.class, () -> newOf(null, MOCK_CONTENT));
        }

        @Test
        @DisplayName("throws exception when null content given")
        void throwsExceptionWhenNullContentGiven() {
            assertThrowsExactly(NullPointerException.class, () -> newOf(MediaType.IMAGE_JPEG, null));
        }

        @Test
        @DisplayName("throws exception when empty content given")
        void throwsExceptionWhenEmptyContentGiven() {
            assertThrowsExactly(IllegalArgumentException.class, () -> newOf(MediaType.IMAGE_JPEG, new byte[0]));
        }

        @ParameterizedTest
        @DisplayName("throws exception when given media type not supported")
        @ArgumentsSource(UnsupportedMediaTypes.class)
        void throwsExceptionWhenGivenMediaTypeNotSupported(MediaType mediaType) {
            assertThrowsExactly(IllegalArgumentException.class, () -> newOf(mediaType, MOCK_CONTENT));
        }

        @ParameterizedTest
        @DisplayName("does not throw exception when given media type is supported")
        @ArgumentsSource(SupportedMediaTypes.class)
        void doesNotThrowExceptionWhenGivenMediaTypeIsSupported(MediaType mediaType) {
            assertDoesNotThrow(() -> newOf(mediaType, MOCK_CONTENT));
        }
    }
}

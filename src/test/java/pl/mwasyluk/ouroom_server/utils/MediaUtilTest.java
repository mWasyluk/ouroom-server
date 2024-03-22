package pl.mwasyluk.ouroom_server.utils;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaUtilTest {
    static class UnsupportedTypesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("image/svg+xml"),
                    Arguments.of("video/quicktime"),
                    Arguments.of("audio/mpeg"),
                    Arguments.of("application/pdf"),
                    Arguments.of("text/plain")
            );
        }
    }

    @Nested
    @DisplayName("isMimeTypeSupported method")
    class IsMimeTypeSupported {
        @Test
        @DisplayName("returns true for supported image type")
        void returnsTrueForSupportedImageType() {
            assertAll(() -> MediaUtil.SUPPORTED_IMAGE_TYPES.forEach(type ->
                    assertTrue(MediaUtil.isMimeTypeSupported(type.toString()))
            ));
        }

        @Test
        @DisplayName("returns true for supported video type")
        void returnsTrueForSupportedVideoType() {
            assertAll(() -> MediaUtil.SUPPORTED_VIDEO_TYPES.forEach(type ->
                    assertTrue(MediaUtil.isMimeTypeSupported(type.toString()))
            ));
        }

        @ParameterizedTest
        @DisplayName("returns false for unsupported type")
        @ArgumentsSource(UnsupportedTypesProvider.class)
        void returnsFalseForUnsupportedType(String mimeType) {
            assertFalse(MediaUtil.isMimeTypeSupported(mimeType));
        }
    }

    @Nested
    @DisplayName("isImageType method")
    class IsImageType {
        @Test
        @DisplayName("returns true for supported image type")
        void returnsTrueForSupportedImageType() {
            assertAll(() -> MediaUtil.SUPPORTED_IMAGE_TYPES.forEach(type ->
                    assertTrue(MediaUtil.isImageType(type.toString()))
            ));
        }

        @Test
        @DisplayName("returns false for supported video type")
        void returnsFalseForSupportedVideoType() {
            assertAll(() -> MediaUtil.SUPPORTED_VIDEO_TYPES.forEach(type ->
                    assertFalse(MediaUtil.isImageType(type.toString()))
            ));
        }

        @ParameterizedTest
        @DisplayName("returns false for unsupported type")
        @ArgumentsSource(UnsupportedTypesProvider.class)
        void returnsFalseForUnsupportedType(String mimeType) {
            assertFalse(MediaUtil.isImageType(mimeType));
        }
    }

    @Nested
    @DisplayName("isVideoType method")
    class IsVideoType {
        @Test
        @DisplayName("returns false for supported image type")
        void returnsFalseForSupportedImageType() {
            assertAll(() -> MediaUtil.SUPPORTED_IMAGE_TYPES.forEach(type ->
                    assertFalse(MediaUtil.isVideoType(type.toString()))
            ));
        }

        @Test
        @DisplayName("returns true for supported video type")
        void returnsTrueForSupportedVideoType() {
            assertAll(() -> MediaUtil.SUPPORTED_VIDEO_TYPES.forEach(type ->
                    assertTrue(MediaUtil.isVideoType(type.toString()))
            ));
        }

        @ParameterizedTest
        @DisplayName("returns false for unsupported type")
        @ArgumentsSource(UnsupportedTypesProvider.class)
        void returnsFalseForUnsupportedType(String mimeType) {
            assertFalse(MediaUtil.isVideoType(mimeType));
        }
    }
}

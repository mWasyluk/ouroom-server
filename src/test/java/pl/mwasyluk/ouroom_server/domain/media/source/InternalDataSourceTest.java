package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.util.Objects;
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

import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

import static org.junit.jupiter.api.Assertions.*;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.*;

class InternalDataSourceTest {

    static class BytesArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(PNG_BYTES),
                    Arguments.of(JPEG_BYTES),
                    Arguments.of(GIF_BYTES),
                    Arguments.of(MP4_BYTES)
            );
        }
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {
        @Test
        @DisplayName("throws exception when null given")
        void throwsExceptionWhenNullGiven() {
            assertThrowsExactly(NullPointerException.class, () -> new InternalDataSource(null));
        }

        @Test
        @DisplayName("throws exception when empty array given")
        void throwsExceptionWhenEmptyArrayGiven() {
            assertThrowsExactly(InitializationException.class, () -> new InternalDataSource(new byte[]{}));
        }

        @ParameterizedTest
        @ArgumentsSource(BytesArguments.class)
        @DisplayName("does not throw exception when valid file byte array given")
        void doesNotThrowExceptionWhenValidFileByteArrayGiven(byte[] bytes) {
            assertDoesNotThrow(() -> new InternalDataSource(bytes));
        }
    }

    @Nested
    @DisplayName("getContentType method")
    class GetContentTypeMethodTest {
        @Test
        @DisplayName("returns correct type")
        void returnsCorrectType() {
            DataSource jpeg = new InternalDataSource(JPEG_BYTES);
            DataSource png = new InternalDataSource(PNG_BYTES);
            DataSource gif = new InternalDataSource(GIF_BYTES);
//            DataSource mp4 = new InternalDataSource(MP4_BYTES);

            assertAll(() -> {
                assertEquals(MediaType.IMAGE_JPEG_VALUE, jpeg.getContentType());
                assertEquals(MediaType.IMAGE_PNG_VALUE, png.getContentType());
                assertEquals(MediaType.IMAGE_GIF_VALUE, gif.getContentType());
//                assertEquals(VideoMediaType.MP4.toString(), mp4.getContentType());
            });
        }

        @Test
        @DisplayName("returns null when bytes do not represent media")
        void returnsNullWhenBytesDoNotRepresentMedia() {
            DataSource ds = new InternalDataSource("teststringvalue".getBytes());

            assertNull(ds.getContentType());
        }
    }

    @Nested
    @DisplayName("getInputStream method")
    class GetInputStreamMethodTest {
        @ParameterizedTest
        @ArgumentsSource(BytesArguments.class)
        @DisplayName("returns valid stream")
        void returnsValidStream(byte[] bytes) throws IOException {
            DataSource source = new InternalDataSource(bytes);

            int available;
            available = source.getInputStream().available();

            assertTrue(available > 12);
        }

        @ParameterizedTest
        @ArgumentsSource(BytesArguments.class)
        @DisplayName("returns stream of the same bytes")
        void returnsStreamOfTheSameBytes(byte[] bytes) throws IOException {
            DataSource source = new InternalDataSource(bytes);

            byte[] arr = source.getInputStream().readAllBytes();

            assertArrayEquals(bytes, arr);
        }
    }
}

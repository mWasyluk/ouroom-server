package pl.mwasyluk.ouroom_server.domain.media.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil.JPEG_BYTES;

class DataSourceTest {

    static class InvalidUrlValArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("htp://test.com"),
                    Arguments.of("http://test.com:<"),
                    Arguments.of("test.com"),
                    Arguments.of("")
            );
        }
    }

    static class ValidUrlValArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(DataSourceTestUtil.JPEG_URL_VALUE),
                    Arguments.of(DataSourceTestUtil.PNG_URL_VALUE),
                    Arguments.of(DataSourceTestUtil.MP4_URL_VALUE)
            );
        }
    }

    @Nested
    @DisplayName("getData method")
    class GetDataMethodTest {
        @Test
        @DisplayName("returns bytes array when external source")
        void returnsBytesArrayWhenExternalSource() throws MalformedURLException {
            DataSource ex = new ExternalDataSource(new URL(DataSourceTestUtil.JPEG_URL_VALUE));

            assertTrue(ex.getData().length > 12);
        }

        @Test
        @DisplayName("returns bytes array when internal source")
        void returnsBytesArrayWhenInternalSource() {
            DataSource ex = new InternalDataSource(JPEG_BYTES);

            assertTrue(ex.getData().length > 12);
        }
    }

    @Nested
    @DisplayName("of method")
    class OfMethodTest {
        @ParameterizedTest
        @ArgumentsSource(InvalidUrlValArguments.class)
        @DisplayName("throws exception when string is not a valid url")
        void throwsExceptionWhenStringIsNotAValidUrl(String invalidUrlVal) {
            assertThrowsExactly(MalformedURLException.class, () -> DataSource.of(invalidUrlVal));
        }

        @ParameterizedTest
        @ArgumentsSource(ValidUrlValArguments.class)
        @DisplayName("returns external data source when a valid url value given")
        void returnsExternalDataSourceWhenAValidUrlValueGiven(String validUrlVal) throws MalformedURLException {
            assertInstanceOf(ExternalDataSource.class, DataSource.of(validUrlVal));
        }

        @ParameterizedTest
        @ArgumentsSource(ValidUrlValArguments.class)
        @DisplayName("returns external data source when a valid url given")
        void returnsExternalDataSourceWhenAValidUrlGiven(String validUrlVal) throws MalformedURLException {
            URL url = new URL(validUrlVal);

            assertInstanceOf(ExternalDataSource.class, DataSource.of(url));
        }

        @Test
        @DisplayName("returns InternalDataSource when byte array is not empty")
        void returnsInternalDataSourceWhenByteArrayIsNotEmpty() {
            assertInstanceOf(InternalDataSource.class, DataSource.of(new byte[]{0x10, 0x2b}));
        }

        @Test
        @DisplayName("throws exception when string is null")
        void throwsExceptionWhenStringIsNull() {
            assertThrowsExactly(NullPointerException.class, () -> DataSource.of((String) null));
        }

        @Test
        @DisplayName("throws exception when url is null")
        void throwsExceptionWhenUrlIsNull() {
            assertThrowsExactly(NullPointerException.class, () -> DataSource.of((URL) null));
        }

        @Test
        @DisplayName("throws exception when byte array is null")
        void throwsExceptionWhenByteArrayIsNull() {
            assertThrowsExactly(NullPointerException.class, () -> DataSource.of((URL) null));
        }
    }
}

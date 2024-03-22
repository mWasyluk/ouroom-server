package pl.mwasyluk.ouroom_server.domain.container;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSource;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BaseConversationTest {
    @Nested
    @DisplayName("getName method")
    class GetNameMethodTest {
        @Test
        @DisplayName("returns null by default")
        void returnsNullByDefault() {
            BaseConversation baseConversation = new Chat();

            assertNull(baseConversation.getName());
        }

        @Test
        @DisplayName("returns name when set")
        void returnsNameWhenSet() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");

            assertEquals("name", baseConversation.getName());
        }

        @Test
        @DisplayName("returns null when name is not set")
        void returnsNullWhenNameIsNotSet() {
            BaseConversation baseConversation = new Chat();

            assertNull(baseConversation.getName());
        }

        @Test
        @DisplayName("returns trimmed name when name is set")
        void returnsTrimmedNameWhenNameIsSet() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName(" name  \n     ");

            assertEquals("name", baseConversation.getName());
        }
    }

    @Nested
    @DisplayName("setName method")
    class SetNameMethodTest {
        @Test
        @DisplayName("sets name when name is not set")
        void setsNameWhenNameIsNotSet() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");

            assertEquals("name", baseConversation.getName());
        }

        @Test
        @DisplayName("sets name when name is set")
        void setsNameWhenNameIsSet() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");
            baseConversation.setName("new name");

            assertEquals("new name", baseConversation.getName());
        }

        @Test
        @DisplayName("sets trimmed name")
        void setsTrimmedName() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName(" name  \n     ");

            assertEquals("name", baseConversation.getName());
        }

        @Test
        @DisplayName("sets null when null provided")
        void setsNullWhenNullProvided() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");
            baseConversation.setName(null);

            assertNull(baseConversation.getName());
        }

        @Test
        @DisplayName("sets null when empty string provided")
        void setsNullWhenEmptyStringProvided() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");
            baseConversation.setName("");

            assertNull(baseConversation.getName());
        }

        @Test
        @DisplayName("sets null when blank string provided")
        void setsNullWhenBlankStringProvided() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setName("name");
            baseConversation.setName("  \n  ");

            assertNull(baseConversation.getName());
        }
    }

    @Nested
    @DisplayName("setImage method")
    class SetImageMethodTest {
        Image testImage = (Image) Media.of(DataSource.of(DataSourceTestUtil.JPEG_BYTES));

        @Test
        @DisplayName("sets image when image provided")
        void setsImageWhenImageProvided() {
            BaseConversation baseConversation = new Chat();

            baseConversation.setImage(testImage);

            assertEquals(testImage, baseConversation.getImage());
        }

        @Test
        @DisplayName("sets null when null provided")
        void setsNullWhenNullProvided() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setImage(testImage);

            baseConversation.setImage(null);

            assertNull(baseConversation.getImage());
        }
    }

    @Nested
    @DisplayName("getImage method")
    class GetImageMethodTest {
        Image testImage = (Image) Media.of(DataSource.of(DataSourceTestUtil.JPEG_BYTES));

        @Test
        @DisplayName("returns null by default")
        void returnsNullByDefault() {
            BaseConversation baseConversation = new Chat();

            assertNull(baseConversation.getImage());
        }

        @Test
        @DisplayName("returns image when set")
        void returnsImageWhenSet() {
            BaseConversation baseConversation = new Chat();
            baseConversation.setImage(testImage);

            assertEquals(testImage, baseConversation.getImage());
        }
    }
}

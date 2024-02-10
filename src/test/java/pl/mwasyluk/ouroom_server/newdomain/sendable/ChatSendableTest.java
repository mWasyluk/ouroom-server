package pl.mwasyluk.ouroom_server.newdomain.sendable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
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

import pl.mwasyluk.ouroom_server.newdomain.media.Media;
import pl.mwasyluk.ouroom_server.newdomain.media.VideoMediaType;
import pl.mwasyluk.ouroom_server.newdomain.user.User;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatSendableTest {
    private static final User MOCK_USER = User.mockOf(UUID.randomUUID());
    private static final Media MOCK_JPEG_MEDIA = Media.of(MediaType.IMAGE_JPEG, "mock".getBytes());
    private static final Media MOCK_MP4_MEDIA = Media.of(VideoMediaType.MP4, "mock".getBytes());

    ChatSendable messageNullSendable = new ChatSendable(MOCK_USER, "text", null);
    ChatSendable messageMediaSendable = new ChatSendable(MOCK_USER, "text", MOCK_JPEG_MEDIA);
    ChatSendable nullMediaSendable = new ChatSendable(MOCK_USER, null, MOCK_JPEG_MEDIA);

    static class InvalidArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(null, null),
                    Arguments.of("", null),
                    Arguments.of("  ", null)
            );
        }
    }

    static class ValidArguments implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(null, MOCK_JPEG_MEDIA),
                    Arguments.of("     \n  ", MOCK_MP4_MEDIA),
                    Arguments.of("text", MOCK_MP4_MEDIA),
                    Arguments.of("text", null)
            );
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {
        // Exception when invalid
        @ParameterizedTest
        @DisplayName("throws exception when content is invalid")
        @ArgumentsSource(InvalidArguments.class)
        void throwsExceptionWhenContentIsInvalid(String message, Media media) {
            assertThrowsExactly(IllegalArgumentException.class, () -> new ChatSendable(MOCK_USER, message, media));
        }

        // no exception when valid
        @ParameterizedTest
        @DisplayName("instantiates object when content is valid")
        @ArgumentsSource(ValidArguments.class)
        void instantiatesObjectWhenContentIsValid(String message, Media media) {
            assertInstanceOf(ChatSendable.class, new ChatSendable(MOCK_USER, message, media));
        }

        // sets correct media type
        @Test
        @DisplayName("correctly determines the TEXT type")
        void correctlyDeterminesTheTextType() {
            ChatSendable o1 = messageNullSendable;
            ChatSendable o2 = new ChatSendable(MOCK_USER, "źŁĄĆŚóźż", null);
            ChatSendable o3 = new ChatSendable(MOCK_USER, "\\(", null);

            assertAll(() -> {
                assertEquals(SendableType.TEXT, o1.getType());
                assertEquals(SendableType.TEXT, o2.getType());
                assertEquals(SendableType.TEXT, o3.getType());
            });
        }

        // sets blank message as null
        @Test
        @DisplayName("sets message to null and media to given when given message is blank")
        void setsMessageToNullAndMediaToGivenWhenGivenMessageIsBlank() {
            ChatSendable o1 = new ChatSendable(MOCK_USER, "     \n  ", MOCK_JPEG_MEDIA);
            ChatSendable o2 = new ChatSendable(MOCK_USER, "", MOCK_JPEG_MEDIA);

            assertAll(() -> {
                assertNull(o1.message);
                assertEquals(MOCK_JPEG_MEDIA, o1.media);
                assertNull(o2.message);
                assertEquals(MOCK_JPEG_MEDIA, o2.media);
            });
        }

        // sets trimmed messages
        @Test
        @DisplayName("sets trimmed messages")
        void setsTrimmedMessages() {
            ChatSendable o1 = new ChatSendable(MOCK_USER, "       text  ", null);
            ChatSendable o2 = new ChatSendable(MOCK_USER, "\ntext \n", null);

            assertAll(() -> {
                assertEquals("text", o1.message);
                assertEquals("text", o2.message);
            });
        }

        @Test
        @DisplayName("correctly determines the MEDIA type")
        void correctlyDeterminesTheMediaType() {
            ChatSendable o1 = nullMediaSendable;
            ChatSendable o2 = messageMediaSendable;

            assertAll(() -> {
                assertEquals(SendableType.MEDIA, o1.getType());
                assertEquals(SendableType.MEDIA, o2.getType());
            });
        }

        // createdAt to actual datetime
        @Test
        @DisplayName("sets createdAt as the current date time")
        void setsCreatedAtAsTheCurrentDateTime() {
            ChatSendable o1 = messageNullSendable;

            long secondsFromCreation = Duration.between(o1.getCreatedAt(), LocalDateTime.now()).getSeconds();

            assertTrue(secondsFromCreation < 1);
        }

        // state as Created
        @Test
        @DisplayName("sets state as created")
        void setsStateAsCreated() {
            ChatSendable o1 = messageNullSendable;

            assertEquals(SendableState.CREATED, o1.getState());
        }
    }

    @Nested
    @DisplayName("updateState method")
    class UpdateStateMethodTest {
        // sets state and returns true when new state higher than current
        @Test
        @DisplayName("sets state and returns true when new state higher than current")
        void setsStateAndReturnsTrueWhenNewStateHigherThanCurrent() {
            ChatSendable o1 = messageNullSendable;

            assertAll(() -> {
                assertTrue(o1.updateState(SendableState.SENT));
                assertEquals(SendableState.SENT, o1.getState());
                assertTrue(o1.updateState(SendableState.DELIVERED));
                assertEquals(SendableState.DELIVERED, o1.getState());
                assertTrue(o1.updateState(SendableState.READ));
                assertEquals(SendableState.READ, o1.getState());
            });
        }

        // sets state and returns true when new state is few higher than current
        @Test
        @DisplayName("sets state and returns true when new state few higher than current")
        void setsStateAndReturnsTrueWhenNewStateFewHigherThanCurrent() {
            ChatSendable o1 = messageNullSendable;

            assertAll(() -> {
                assertTrue(o1.updateState(SendableState.READ));
                assertEquals(SendableState.READ, o1.getState());
            });
        }

        // sets state and returns true when new state is the same
        @Test
        @DisplayName("returns true when new state is the same")
        void returnsTrueWhenNewStateIsTheSame() {
            ChatSendable o1 = messageNullSendable;
            o1.state = SendableState.DELIVERED;

            assertAll(() -> {
                assertTrue(o1.updateState(SendableState.DELIVERED));
                assertEquals(SendableState.DELIVERED, o1.getState());
            });
        }

        // does not set state and returns false when new is null || new is lower
        @Test
        @DisplayName("does not set state and returns false when new state is null")
        void doesNotSetStateAndReturnsFalseWhenNewStateIsNull() {
            ChatSendable o1 = messageNullSendable;

            assertAll(() -> {
                assertFalse(o1.updateState(null));
                assertEquals(SendableState.CREATED, o1.getState());
            });
        }

        @Test
        @DisplayName("does not set state and returns false when new state is lower")
        void doesNotSetStateAndReturnsFalseWhenNewStateIsLower() {
            ChatSendable o1 = messageNullSendable;
            o1.state = SendableState.DELIVERED;

            assertAll(() -> {
                assertFalse(o1.updateState(SendableState.SENT));
                assertEquals(SendableState.DELIVERED, o1.getState());
            });
        }
    }

    @Nested
    @DisplayName("updateMessage method")
    class UpdateMessageMethodTest {

        @ParameterizedTest
        @DisplayName("returns false when content is invalid")
        @ArgumentsSource(InvalidArguments.class)
        void returnsFalseWhenContentIsInvalid(String message, Media media) {
            ChatSendable o1 = messageNullSendable;
            o1.setMedia(media);

            assertFalse(o1.updateMessage(message));
        }

        @ParameterizedTest
        @DisplayName("returns true when content is valid")
        @ArgumentsSource(ValidArguments.class)
        void returnsTrueWhenContentIsValid(String message, Media media) {
            ChatSendable o1 = messageNullSendable;
            o1.setMedia(media);

            assertTrue(o1.updateMessage(message));
        }

        // current null, new null -> not edited
        @Test
        @DisplayName("does not set edited when current null and new null")
        void doesNotSetEditedWhenCurrentNullAndNewNull() {
            ChatSendable o1 = nullMediaSendable;
            o1.updateMessage(null);

            assertFalse(o1.isEdited());
        }

        // current null, new blank -> not edited
        @Test
        @DisplayName("does not set edited when current null and new blank")
        void doesNotSetEditedWhenCurrentNullAndNewBlank() {
            ChatSendable o1 = nullMediaSendable;
            o1.updateMessage("  \n   ");

            assertFalse(o1.isEdited());
        }

        // current null, new notblank -> edited
        @Test
        @DisplayName("sets edited when current null and new not blank")
        void setsEditedWhenCurrentNullAndNewNotBlank() {
            ChatSendable o1 = nullMediaSendable;
            o1.updateMessage("   Text ");

            assertTrue(o1.isEdited());
        }

        // current notblank, new null / blank -> edited + set as null
        @Test
        @DisplayName("sets edited and as null when current not blank and new is null")
        void setsEditedAndAsNullWhenCurrentNotBlankAndNewIsNull() {
            ChatSendable o1 = messageMediaSendable;
            o1.updateMessage(null);

            assertAll(() -> {
                assertTrue(o1.isEdited());
                assertNull(o1.message);
            });
        }

        @Test
        @DisplayName("sets edited and as null when current not blank and new is blank")
        void setsEditedAndAsNullWhenCurrentNotBlankAndNewIsBlank() {
            ChatSendable o1 = messageMediaSendable;
            o1.updateMessage("  \n   ");

            assertAll(() -> {
                assertTrue(o1.isEdited());
                assertNull(o1.message);
            });
        }

        // current notblank, new equal -> not edited
        @Test
        @DisplayName("does not set edited when current not blank and new is equal")
        void doesNotSetEditedWhenCurrentNotBlankAndNewIsEqual() {
            ChatSendable o1 = messageMediaSendable;
            o1.updateMessage(new String(o1.message));

            assertFalse(o1.isEdited());
        }

        // current notblank, new equal current among white spaces -> not edited
        @Test
        @DisplayName("does not set edited when current not blank and new is the same string among white spaces")
        void doesNotSetEditedWhenCurrentNotBlankAndNewIsTheSameStringAmongWhiteSpaces() {
            ChatSendable o1 = messageNullSendable;
            o1.updateMessage("   " + o1.message + " ");

            assertFalse(o1.isEdited());
        }

        // current notblank, new nonequal -> edited
        @Test
        @DisplayName("sets edited when current not blank and new is different string")
        void setsEditedWhenCurrentNotBlankAndNewIsDifferentString() {
            ChatSendable o1 = messageNullSendable;
            o1.updateMessage("  Test: " + o1.message);

            assertTrue(o1.isEdited());
        }

        // sets trimmed message
        @Test
        @DisplayName("sets trimmed message when new is different string among white spaces")
        void setsTrimmedMessageWhenNewIsDifferentStringAmongWhiteSpaces() {
            ChatSendable o1 = nullMediaSendable;
            o1.updateMessage("   New String ");

            assertEquals("New String", o1.message);
        }
    }

    @Nested
    @DisplayName("updateMedia method")
    class UpdateMediaMethodTest {

        @ParameterizedTest
        @DisplayName("returns false when content is invalid")
        @ArgumentsSource(InvalidArguments.class)
        void returnsFalseWhenContentIsInvalid(String message, Media media) {
            ChatSendable o1 = nullMediaSendable;
            o1.setMessage(message);

            assertFalse(o1.updateMedia(media));
        }

        @ParameterizedTest
        @DisplayName("returns true when content is valid")
        @ArgumentsSource(ValidArguments.class)
        void returnsTrueWhenContentIsValid(String message, Media media) {
            ChatSendable o1 = nullMediaSendable;
            o1.setMessage(message);

            assertTrue(o1.updateMedia(media));
        }

        // current null, new null -> not edited
        @Test
        @DisplayName("does not set edited when current null and new null")
        void doesNotSetEditedWhenCurrentNullAndNewNull() {
            ChatSendable o1 = messageNullSendable;
            o1.updateMedia(null);

            assertFalse(o1.isEdited());
        }

        // current null, new non-null -> edited
        @Test
        @DisplayName("sets edited when current null and new non null")
        void setsEditedWhenCurrentNullAndNewNonNull() {
            ChatSendable o1 = messageNullSendable;
            o1.updateMedia(MOCK_JPEG_MEDIA);

            assertTrue(o1.isEdited());
        }

        // current non-null, new null -> edited + set as null
        @Test
        @DisplayName("sets edited and as null when current non null and new is null")
        void setsEditedAndAsNullWhenCurrentNonNullAndNewIsNull() {
            ChatSendable o1 = messageMediaSendable;
            o1.updateMedia(null);

            assertAll(() -> {
                assertTrue(o1.isEdited());
                assertNull(o1.media);
            });
        }

        // current non-null, new equal -> not edited
        @Test
        @DisplayName("does not set edited when current non null and new is equal")
        void doesNotSetEditedWhenCurrentNonNullAndNewIsEqual() {
            ChatSendable o1 = messageMediaSendable;
            o1.updateMedia(o1.media);

            assertFalse(o1.isEdited());
        }

        // current non-null, new non-equal -> edited
        @Test
        @DisplayName("sets edited when current non null and new is non-equal")
        void setsEditedWhenCurrentNonNullAndNewIsNonEqual() {
            ChatSendable o1 = messageMediaSendable;
            o1.setMedia(MOCK_JPEG_MEDIA);

            o1.updateMedia(MOCK_MP4_MEDIA);

            assertTrue(o1.isEdited());
        }
    }
}

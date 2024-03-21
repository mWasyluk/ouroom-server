package pl.mwasyluk.ouroom_server.domain.sendable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatSendableTest {

    private static final User MOCK_USER = User.mockOf(UUID.randomUUID());
    private final String DEFAULT_MESSAGE = "text";

    private ChatSendable newSendable(String message) {
        return new ChatSendable(MOCK_USER, message);
    }

    private ChatSendable newSendable() {
        return newSendable(new String(DEFAULT_MESSAGE));
    }

    static class InvalidArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("  "),
                    Arguments.of("     \n  ")
            );
        }
    }

    static class ValidArguments implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("."),
                    Arguments.of(" \n  :D  "),
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
        void throwsExceptionWhenContentIsInvalid(String message) {
            assertThrowsExactly(InitializationException.class, () -> newSendable(message));
        }

        // no exception when valid
        @ParameterizedTest
        @DisplayName("instantiates object when content is valid")
        @ArgumentsSource(ValidArguments.class)
        void instantiatesObjectWhenContentIsValid(String message) {
            assertInstanceOf(ChatSendable.class, newSendable(message));
        }

        // sets trimmed messages
        @Test
        @DisplayName("sets trimmed messages")
        void setsTrimmedMessages() {
            ChatSendable o1 = newSendable("       text  ");
            ChatSendable o2 = newSendable("\ntext \n");

            assertAll(() -> {
                assertEquals("text", o1.message);
                assertEquals("text", o2.message);
            });
        }

        // createdAt to actual datetime
        @Test
        @DisplayName("sets createdAt as the current date time")
        void setsCreatedAtAsTheCurrentDateTime() {
            ChatSendable o1 = newSendable();

            long secondsFromCreation = Duration.between(o1.getCreatedAt(), ZonedDateTime.now()).getSeconds();

            assertTrue(secondsFromCreation < 1);
        }

        // state as Created
        @Test
        @DisplayName("sets state as sent")
        void setsStateAsSent() {
            ChatSendable o1 = newSendable();

            assertEquals(SendableState.SENT, o1.getState());
        }
    }

    @Nested
    @DisplayName("updateState method")
    class UpdateStateMethodTest {
        // sets state and returns true when new state higher than current
        @Test
        @DisplayName("sets state and returns true when new state higher than current")
        void setsStateAndReturnsTrueWhenNewStateHigherThanCurrent() {
            ChatSendable o1 = newSendable();

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
            ChatSendable o1 = newSendable();

            assertAll(() -> {
                assertTrue(o1.updateState(SendableState.READ));
                assertEquals(SendableState.READ, o1.getState());
            });
        }

        // sets state and returns true when new state is the same
        @Test
        @DisplayName("returns true when new state is the same")
        void returnsTrueWhenNewStateIsTheSame() {
            ChatSendable o1 = newSendable();
            o1.state = SendableState.DELIVERED;

            assertAll(() -> {
                assertTrue(o1.updateState(SendableState.DELIVERED));
                assertEquals(SendableState.DELIVERED, o1.getState());
            });
        }

        // does not set state and returns false when new is null || new is lower
        @Test
        @DisplayName("throws exception when new state is null")
        void doesNotSetStateAndReturnsFalseWhenNewStateIsNull() {
            ChatSendable o1 = newSendable();

            assertAll(() -> {
                assertThrowsExactly(NullPointerException.class, () -> o1.updateState(null));
                assertEquals(SendableState.SENT, o1.getState());
            });
        }

        @Test
        @DisplayName("does not set state and returns false when new state is lower")
        void doesNotSetStateAndReturnsFalseWhenNewStateIsLower() {
            ChatSendable o1 = newSendable();
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
        void returnsFalseWhenContentIsInvalid(String message) {
            ChatSendable o1 = newSendable();

            assertFalse(o1.updateMessage(message));
        }

        @Test
        @DisplayName("return false when content is null")
        void returnFalseWhenContentIsNull() {
            ChatSendable o1 = newSendable();

            assertFalse(o1.updateMessage(null));
        }

        @ParameterizedTest
        @DisplayName("returns true when content is valid")
        @ArgumentsSource(ValidArguments.class)
        void returnsTrueWhenContentIsValid(String message) {
            ChatSendable o1 = newSendable();

            assertTrue(o1.updateMessage(message));
        }

        @Test
        @DisplayName("returns false and does not change state when new is null")
        void returnsFalseAndDoesNotChangeStateWhenNewIsNull() {
            ChatSendable o1 = newSendable();
            o1.updateMessage(null);

            assertAll(() -> {
                assertFalse(o1.isEdited());
                assertEquals(DEFAULT_MESSAGE, o1.message);
            });
        }

        @Test
        @DisplayName("returns false and does not change state when new is blank")
        void returnsFalseAndDoesNotChangeStateWhenNewIsBlank() {
            ChatSendable o1 = newSendable();
            o1.updateMessage("  \n   ");

            assertAll(() -> {
                assertFalse(o1.isEdited());
                assertEquals(DEFAULT_MESSAGE, o1.message);
            });
        }

        // current notblank, new equal -> not edited
        @Test
        @DisplayName("does not set edited when new is equal")
        void doesNotSetEditedWhenNewIsEqual() {
            ChatSendable o1 = newSendable();
            o1.updateMessage(DEFAULT_MESSAGE);

            assertFalse(o1.isEdited());
        }

        @Test
        @DisplayName("does not set edited when new is equal after trimming")
        void doesNotSetEditedWhenNewIsEqualAfterTrimming() {
            ChatSendable o1 = newSendable();
            o1.updateMessage("  \n " + DEFAULT_MESSAGE + "    ");

            assertFalse(o1.isEdited());
        }

        // current notblank, new nonequal -> edited
        @Test
        @DisplayName("sets edited when new is a different string")
        void setsEditedWhenNewIsDifferentString() {
            ChatSendable o1 = newSendable();
            o1.updateMessage("Test");

            assertTrue(o1.isEdited());
        }

        // sets trimmed message
        @Test
        @DisplayName("sets trimmed message when new is different string among white spaces")
        void setsTrimmedMessageWhenNewIsDifferentStringAmongWhiteSpaces() {
            ChatSendable o1 = newSendable();
            o1.updateMessage("   New String ");

            assertEquals("New String", o1.message);
        }
    }
}

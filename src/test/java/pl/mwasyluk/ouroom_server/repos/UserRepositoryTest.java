package pl.mwasyluk.ouroom_server.repos;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {
    private final User mockUser = new User("mock", "mock");
    private final User mockUser2 = new User("mock2", "mock");

    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @Nested
    @DisplayName("findByUsername method")
    class FindByUsernameMethodTest {
        @Test
        @DisplayName("returns user with given username")
        void returnsUserWithGivenUsername() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            Optional<User> user = userRepository.findByUsername(mockUser.getUsername());

            assertTrue(user.isPresent());
            assertEquals(mockUser, user.get());
        }

        @Test
        @DisplayName("returns empty optional when user with given username does not exist")
        void returnsEmptyOptionalWhenUserWithGivenUsernameDoesNotExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            Optional<User> user = userRepository.findByUsername("nonexistent");

            assertTrue(user.isEmpty());
        }

        @Test
        @DisplayName("returns empty optional when username is null")
        void returnsEmptyOptionalWhenUsernameIsNull() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            Optional<User> user = userRepository.findByUsername(null);

            assertTrue(user.isEmpty());
        }

        @Test
        @DisplayName("returns empty optional when username is equal but with different casing")
        void returnsEmptyOptionalWhenUsernameIsEqualButWithDifferentCasing() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            Optional<User> user = userRepository.findByUsername(mockUser.getUsername().toUpperCase());

            assertTrue(user.isEmpty());
        }
    }

    @Nested
    @DisplayName("countAllByIdIn method")
    class CountAllByIdInMethodTest {
        @Test
        @DisplayName("returns number of users with given IDs when all exist")
        void returnsNumberOfUsersWithGivenIDsWhenAllExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            userRepository.save(new User("mock3", "mock"));
            userRepository.save(new User("mock4", "mock"));

            int count = userRepository.countAllByIdIn(Set.of(mockUser.getId(), mockUser2.getId()));

            assertEquals(2, count);
        }

        @Test
        @DisplayName("returns 0 when empty set is provided")
        void returnsZeroWhenEmptySetIsProvided() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            int count = userRepository.countAllByIdIn(Set.of());

            assertEquals(0, count);
        }

        @Test
        @DisplayName("returns 0 when null is provided")
        void returnsZeroWhenNullIsProvided() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            int count = userRepository.countAllByIdIn(null);

            assertEquals(0, count);
        }

        @Test
        @DisplayName("returns 0 when no users with given IDs exist")
        void returnsZeroWhenNoUsersWithGivenIDsExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            int count = userRepository.countAllByIdIn(
                    Set.of(UUID.randomUUID(), UUID.randomUUID(), new User("mock3", "mock").getId()));

            assertEquals(0, count);
        }

        @Test
        @DisplayName("returns number of existing users when some IDs exist")
        void returnsNumberOfExistingUsersWhenSomeIDsExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            int count = userRepository.countAllByIdIn(
                    Set.of(mockUser.getId(), UUID.randomUUID(), new User("mock3", "mock").getId()));

            assertEquals(1, count);
        }
    }

    @Nested
    @DisplayName("allExistByIdIn method")
    class AllExistByIdInMethodTest {
        @Test
        @DisplayName("returns true when all users with given IDs exist")
        void returnsTrueWhenAllUsersWithGivenIDsExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);
            userRepository.save(new User("mock3", "mock"));
            userRepository.save(new User("mock4", "mock"));

            boolean allExist = userRepository.allExistByIdIn(Set.of(mockUser.getId(), mockUser2.getId()));

            assertTrue(allExist);
        }

        @Test
        @DisplayName("returns true when empty set is provided")
        void returnsTrueWhenEmptySetIsProvided() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            boolean allExist = userRepository.allExistByIdIn(Set.of());

            assertTrue(allExist);
        }

        @Test
        @DisplayName("returns true when null is provided")
        void returnsTrueWhenNullIsProvided() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            boolean allExist = userRepository.allExistByIdIn(null);

            assertTrue(allExist);
        }

        @Test
        @DisplayName("returns false when no users with given IDs exist")
        void returnsFalseWhenNoUsersWithGivenIDsExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            boolean allExist = userRepository.allExistByIdIn(
                    Set.of(UUID.randomUUID(), UUID.randomUUID(), new User("mock3", "mock").getId()));

            assertFalse(allExist);
        }

        @Test
        @DisplayName("returns false when some IDs do not exist")
        void returnsFalseWhenSomeIDsDoNotExist() {
            userRepository.save(mockUser);
            userRepository.save(mockUser2);

            boolean allExist = userRepository.allExistByIdIn(
                    Set.of(mockUser.getId(), UUID.randomUUID(), new User("mock3", "mock").getId()));

            assertFalse(allExist);
        }
    }
}

package pl.mwasyluk.ouroom_server.domain.user;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pl.mwasyluk.ouroom_server.domain.media.Image;
import pl.mwasyluk.ouroom_server.domain.media.Media;
import pl.mwasyluk.ouroom_server.domain.media.source.DataSourceTestUtil;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {
        @Test
        @DisplayName("sets USER authority when no authorities are given")
        void setsUserAuthorityWhenNoAuthoritiesAreGiven() {
            User user = new User("username", "password");

            assertArrayEquals(new UserAuthority[]{UserAuthority.USER}, user.getAuthorities().toArray());
        }

        @Test
        @DisplayName("sets given authorities")
        void setsGivenAuthorities() {
            User user = new User("username", "password", Set.of(UserAuthority.ADMIN, UserAuthority.USER));

            assertArrayEquals(
                    new UserAuthority[]{UserAuthority.ADMIN, UserAuthority.USER},
                    user.getAuthorities().toArray());
        }

        @Test
        @DisplayName("throws exception when username is null")
        void throwsExceptionWhenUsernameIsNull() {
            assertThrows(NullPointerException.class, () -> new User(null, "password"));
        }

        @Test
        @DisplayName("throws exception when password is null")
        void throwsExceptionWhenPasswordIsNull() {
            assertThrows(NullPointerException.class, () -> new User("username", null));
        }

        @Test
        @DisplayName("throws exception when authorities are null")
        void throwsExceptionWhenAuthoritiesAreNull() {
            assertThrows(NullPointerException.class, () -> new User("username", "password", null));
        }

        @Test
        @DisplayName("throws exception when authorities set is empty")
        void throwsExceptionWhenAuthoritiesSetIsEmpty() {
            assertAll(() -> {
                assertThrowsExactly(NullPointerException.class,
                        () -> new User("username", "password", Collections.singleton(null)));
                assertThrowsExactly(InitializationException.class,
                        () -> new User("username", "password", Set.of()));
            });
        }

        @Disabled("Disabled until the blockages mechanism is implemented")
        @Test
        @DisplayName("sets disabled account by default")
        void setsDisabledAccountByDefault() {
            User user = new User("username", "password");

            assertFalse(user.isEnabled());
        }

        @Test
        @DisplayName("sets enabled account by default")
        void setsEnabledAccountByDefault() {
            User user = new User("username", "password");

            assertTrue(user.isEnabled());
        }

        @Test
        @DisplayName("sets LOCAL provider by default")
        void setsLocalProviderByDefault() {
            User user = new User("username", "password");

            assertEquals(AuthProvider.LOCAL, user.getProvider());
        }

        @Test
        @DisplayName("sets UserDetails boolean flags to true by default")
        void setsUserDetailsBooleanFlagsToTrueByDefault() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertTrue(user.isAccountNonExpired());
                assertTrue(user.isAccountNonLocked());
                assertTrue(user.isCredentialsNonExpired());
            });
        }
    }

    @Nested
    @DisplayName("getName method")
    class GetNameMethodTest {
        @Test
        @DisplayName("returns null when profile is null")
        void returnsNullWhenProfileIsNull() {
            User user = new User("username", "password");

            assertNull(user.getName());
        }

        @Test
        @DisplayName("returns firstname and lastname separated by space")
        void returnsFirstnameAndLastnameSeparatedBySpace() {
            User user = new User("username", "password");
            user.profileBuilder()
                    .setFirstname("Firstname")
                    .setLastname("Lastname")
                    .apply();

            assertEquals("Firstname Lastname", user.getName());
        }
    }

    @Nested
    @DisplayName("getImage method")
    class GetImageMethodTest {
        @Test
        @DisplayName("returns null when profile is null")
        void returnsNullWhenProfileIsNull() {
            User user = new User("username", "password");

            assertNull(user.getImage());
        }

        @Test
        @DisplayName("returns image from profile")
        void returnsImageFromProfile() {
            User user = new User("username", "password");
            Image image = (Image) Media.of(DataSourceTestUtil.IN_JPEG_SOURCE);

            user.profileBuilder()
                    .setFirstname("Firstname")
                    .setLastname("Lastname")
                    .setImage(image)
                    .apply();

            assertEquals(image, user.getImage());
        }
    }

    @Nested
    @DisplayName("accountBuilder method")
    class AccountBuilderMethodTest {
        @Test
        @DisplayName("allows to change username")
        void allowsToChangeUsername() {
            User user = new User("username", "password");

            user.accountBuilder().setUsername("newUsername").apply();

            assertEquals("newUsername", user.getUsername());
        }

        @Test
        @DisplayName("allows to change password")
        void allowsToChangePassword() {
            User user = new User("username", "password");

            user.accountBuilder().setPassword("newPassword").apply();

            assertEquals("newPassword", user.getPassword());
        }

        @Test
        @DisplayName("allows to change authorities")
        void allowsToChangeAuthorities() {
            User user = new User("username", "password");

            user.accountBuilder().setAuthorities(Set.of(UserAuthority.ADMIN)).apply();

            assertArrayEquals(new UserAuthority[]{UserAuthority.ADMIN}, user.getAuthorities().toArray());
        }

        @Test
        @DisplayName("allows to change provider")
        void allowsToChangeProvider() {
            User user = new User("username", "password");

            user.accountBuilder().setProvider(AuthProvider.GOOGLE).apply();

            assertEquals(AuthProvider.GOOGLE, user.getProvider());
        }

        @Test
        @DisplayName("allows to change user details boolean flags")
        void allowsToChangeUserDetailsBooleanFlags() {
            User user = new User("username", "password");

            user.accountBuilder()
                    .setAccountNonExpired(false)
                    .setAccountNonLocked(false)
                    .setCredentialsNonExpired(false)
                    .setEnabled(false)
                    .apply();

            assertAll(() -> {
                assertFalse(user.isAccountNonExpired());
                assertFalse(user.isAccountNonLocked());
                assertFalse(user.isCredentialsNonExpired());
                assertFalse(user.isEnabled());
            });
        }

        @Test
        @DisplayName("throws exception when username is null or blank")
        void throwsExceptionWhenUsernameIsNullOrBlank() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class, () -> user.accountBuilder().setUsername(null).apply());
                assertThrows(InitializationException.class, () -> user.accountBuilder().setUsername("  \n  ").apply());
            });
        }

        @Test
        @DisplayName("throws exception when password is null or blank")
        void throwsExceptionWhenPasswordIsNullOrBlank() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class, () -> user.accountBuilder().setPassword(null).apply());
                assertThrows(InitializationException.class, () -> user.accountBuilder().setPassword("  \n  ").apply());
            });
        }

        @Test
        @DisplayName("throws exception when authorities are null or empty")
        void throwsExceptionWhenAuthoritiesAreNullOrEmpty() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class,
                        () -> user.accountBuilder().setAuthorities(null).apply());
                assertThrows(InitializationException.class,
                        () -> user.accountBuilder().setAuthorities(Set.of()).apply());
            });
        }

        @Test
        @DisplayName("throws exception when provider is null")
        void throwsExceptionWhenProviderIsNull() {
            User user = new User("username", "password");

            assertThrows(InitializationException.class, () -> user.accountBuilder().setProvider(null).apply());
        }

        @Test
        @DisplayName("does not override current properties when not specified")
        void doesNotOverrideCurrentPropertiesWhenNotSpecified() {
            User user = new User("username", "password");

            user.accountBuilder().apply();

            assertAll(() -> {
                assertEquals("username", user.getUsername());
                assertEquals("password", user.getPassword());
                assertArrayEquals(new UserAuthority[]{UserAuthority.USER}, user.getAuthorities().toArray());
                assertEquals(AuthProvider.LOCAL, user.getProvider());
                assertAll(() -> {
                    assertTrue(user.isAccountNonExpired());
                    assertTrue(user.isAccountNonLocked());
                    assertTrue(user.isCredentialsNonExpired());
                });
            });
        }

        @Test
        @DisplayName("does not override username when password is changed")
        void doesNotOverrideUsernameWhenPasswordIsChanged() {
            User user = new User("username", "password");

            user.accountBuilder().setPassword("newPassword").apply();

            assertEquals("username", user.getUsername());
        }

        @Test
        @DisplayName("does not override password when username is changed")
        void doesNotOverridePasswordWhenUsernameIsChanged() {
            User user = new User("username", "password");

            user.accountBuilder().setUsername("newUsername").apply();

            assertEquals("password", user.getPassword());
        }
    }

    @Nested
    @DisplayName("profileBuilder method")
    class ProfileBuilderMethodTest {

        @Test
        @DisplayName("allows to change firstname only")
        void allowsToChangeFirstname() {
            User user = new User("username", "password");
            user.profileBuilder().setFirstname("Firstname").setLastname("Lastname").apply();

            user.profileBuilder().setFirstname("NewFirstname").apply();

            assertEquals("NewFirstname Lastname", user.getName());
        }

        @Test
        @DisplayName("allows to change lastname only")
        void allowsToChangeLastname() {
            User user = new User("username", "password");
            user.profileBuilder().setFirstname("Firstname").setLastname("Lastname").apply();

            user.profileBuilder().setLastname("NewLastname").apply();

            assertEquals("Firstname NewLastname", user.getName());
        }

        @Test
        @DisplayName("allows to change image")
        void allowsToChangeImage() {
            User user = new User("username", "password");
            Image image = (Image) Media.of(DataSourceTestUtil.IN_JPEG_SOURCE);
            user.profileBuilder().setFirstname("Firstname").setLastname("Lastname").apply();

            user.profileBuilder().setImage(image).apply();

            assertEquals(image, user.getImage());
        }

        @Test
        @DisplayName("throws exception when firstname is null or blank")
        void throwsExceptionWhenFirstnameIsNullOrBlank() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class, () -> user.profileBuilder().setFirstname(null).apply());
                assertThrows(InitializationException.class, () -> user.profileBuilder().setFirstname("  \n  ").apply());
            });
        }

        @Test
        @DisplayName("throws exception when lastname is null or blank")
        void throwsExceptionWhenLastnameIsNullOrBlank() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class, () -> user.profileBuilder().setLastname(null).apply());
                assertThrows(InitializationException.class, () -> user.profileBuilder().setLastname("  \n  ").apply());
            });
        }

        @Test
        @DisplayName("throws exception when either firstname or lastname is null or blank")
        void throwsExceptionWhenEitherFirstnameOrLastnameIsNullOrBlank() {
            User user = new User("username", "password");

            assertAll(() -> {
                assertThrows(InitializationException.class,
                        () -> user.profileBuilder().setFirstname(null).setLastname("Lastname").apply());
                assertThrows(InitializationException.class,
                        () -> user.profileBuilder().setFirstname("Firstname").setLastname(null).apply());
                assertThrows(InitializationException.class,
                        () -> user.profileBuilder().setFirstname("  \n  ").setLastname("Lastname").apply());
                assertThrows(InitializationException.class,
                        () -> user.profileBuilder().setFirstname("Firstname").setLastname("  \n  ").apply());
            });
        }

        @Test
        @DisplayName("does not override current properties when not specified")
        void doesNotOverrideCurrentPropertiesWhenNotSpecified() {
            User user = new User("username", "password");
            Image image = (Image) Media.of(DataSourceTestUtil.IN_JPEG_SOURCE);

            user.profileBuilder()
                    .setFirstname("Firstname")
                    .setLastname("Lastname")
                    .setImage(image)
                    .apply();

            user.profileBuilder().apply();

            assertAll(() -> {
                assertEquals("Firstname Lastname", user.getName());
                assertEquals(image, user.getImage());
            });
        }
    }
}

package pl.mwasyluk.ouroom_server.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptUtilsTest {
    @Nested
    @DisplayName("isBcryptWithPrefix method")
    class IsBcryptWithPrefixTest {
        @Test
        @DisplayName("returns true when hash starts with {bcrypt} and is a valid bcrypt hash")
        void returnsTrueWhenHashStartsWithBcryptAndIsValidBcryptHash() {
            var hash = "{bcrypt}" + new BCryptPasswordEncoder().encode("password");

            assertTrue(BcryptUtils.isBcryptWithPrefix(hash));
        }

        @Test
        @DisplayName("returns false when hash does not start with {bcrypt}")
        void returnsFalseWhenHashDoesNotStartWithBcrypt() {
            var hash = new BCryptPasswordEncoder().encode("password");

            assertFalse(BcryptUtils.isBcryptWithPrefix(hash));
        }

        @Test
        @DisplayName("returns false when hash starts with {bcrypt} but is not a valid bcrypt hash")
        void returnsFalseWhenHashStartsWithBcryptButIsNotValidBcryptHash() {
            var hash = "{bcrypt}not-a-valid-bcrypt-hash";

            assertFalse(BcryptUtils.isBcryptWithPrefix(hash));
        }
    }

    @Nested
    @DisplayName("isBcrypt method")
    class IsBcryptTest {
        @Test
        @DisplayName("returns true when hash is a valid bcrypt hash")
        void returnsTrueWhenHashIsValidBcryptHash() {
            var hash = new BCryptPasswordEncoder().encode("password");

            assertTrue(BcryptUtils.isBcrypt(hash));
        }

        @Test
        @DisplayName("returns false when hash is not a valid bcrypt hash")
        void returnsFalseWhenHashIsNotValidBcryptHash() {
            var hash = "not-a-valid-bcrypt-hash";

            assertFalse(BcryptUtils.isBcrypt(hash));
        }

        @Test
        @DisplayName("returns false when hash only starts like bcrypt")
        void returnsFalseWhenHashOnlyStartsLikeBcrypt() {
            var hash = "$2a$10$not-a-valid-bcrypt-hash";

            assertFalse(BcryptUtils.isBcrypt(hash));
        }

        @Test
        @DisplayName("returns false when hash is a valid bcrypt hash with prefix")
        void returnsFalseWhenHashIsValidBcryptHashWithPrefix() {
            var hash = "{bcrypt}" + new BCryptPasswordEncoder().encode("password");

            assertFalse(BcryptUtils.isBcrypt(hash));
        }
    }
}

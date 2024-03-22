package pl.mwasyluk.ouroom_server.utils;

import java.util.stream.Stream;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BcryptUtilsTest {
    static class InvalidBcryptValuesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("$2g$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2y$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9E=PYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2b$a0$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$12$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2a$10$G/6>0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2x$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2a$10$G/6n0NAYTURJRtsWrWsihOl0j!MeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("#2a$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$3a$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS")
            );
        }
    }

    static class ValidBcryptValuesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("$2a$08$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO"),
                    Arguments.of("$2a$10$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2a$12$G/6n0NAYTURJRtsWrWsihOl0jyMeMV5S9EePYQ8ZHowQf/QfrL9zS"),
                    Arguments.of("$2a$08$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO"),
                    Arguments.of("$2a$14$Pa9z8oR30QjeMpSOzdJ.HO/XdZUkUZaRfo1rM8ktUA227FJUR6m02"),
                    Arguments.of("$2b$10$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO"),
                    Arguments.of("$2b$12$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO"),
                    Arguments.of("$2y$10$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO"),
                    Arguments.of("$2y$12$UxmWRzjaDf79QaStIOH4LOwqWKDT7mYNfa0nPDEfYZ24SrwbeQWwO")
            );
        }
    }

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
        @ParameterizedTest
        @DisplayName("returns true when hash is a valid bcrypt hash")
        @ArgumentsSource(ValidBcryptValuesProvider.class)
        void returnsTrueWhenHashIsValidBcryptHash(String hash) {
            assertTrue(BcryptUtils.isBcrypt(hash));
        }

        @ParameterizedTest
        @DisplayName("returns false when hash is not a valid bcrypt hash")
        @ArgumentsSource(InvalidBcryptValuesProvider.class)
        void returnsFalseWhenHashIsNotValidBcryptHash(String hash) {
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

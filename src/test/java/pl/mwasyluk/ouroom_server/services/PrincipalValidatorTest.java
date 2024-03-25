package pl.mwasyluk.ouroom_server.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;
import pl.mwasyluk.ouroom_server.mocks.WithMockCustomUser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static pl.mwasyluk.ouroom_server.mocks.MockUserDetailsService.ADMIN;
import static pl.mwasyluk.ouroom_server.mocks.MockUserDetailsService.ADMIN_USER;
import static pl.mwasyluk.ouroom_server.mocks.MockUserDetailsService.USER;
import static pl.mwasyluk.ouroom_server.mocks.WithUserDetailsSecurityContextFactory.pullPrincipalUser;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig
class PrincipalValidatorTest {

    @Nested
    @DisplayName("validatePrincipal method")
    class ValidatePrincipalMethodTest {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, PrincipalValidator::validatePrincipal);
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser
        @DisplayName("returns principal when user is authenticated")
        void returnsPrincipalWhenUserIsAuthenticated() {
            User principal = pullPrincipalUser();

            User user = assertDoesNotThrow(PrincipalValidator::validatePrincipal);
            assertEquals(principal, user);
        }

        @Test
        @DisplayName("returns principal when security context contains user principal")
        void returnsPrincipalWhenSecurityContextContainsUserPrincipal() {
            User principal = new User("test", "pass");
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal, principal.getPassword(), principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = assertDoesNotThrow(PrincipalValidator::validatePrincipal);
            assertEquals(principal, user);
        }

        @Test
        @DisplayName("throws UNAUTHORIZED when security context contains non-user principal")
        void throwsUnauthorizedWhenSecurityContextContainsNonUserPrincipal() {
            Authentication auth = new UsernamePasswordAuthenticationToken("test", "pass");
            SecurityContextHolder.getContext().setAuthentication(auth);

            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, PrincipalValidator::validatePrincipal);
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }
    }

    @Nested
    @DisplayName("validateAdminPrincipal method")
    class ValidateAdminPrincipalMethodTest {
        @Test
        @DisplayName("throws UNAUTHORIZED when user is not authenticated")
        void throwsUnauthorizedWhenUserIsNotAuthenticated() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, PrincipalValidator::validateAdminPrincipal);
            assertEquals(UNAUTHORIZED, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser(USER)
        @DisplayName("throws FORBIDDEN when user is not an admin")
        void throwsForbiddenWhenUserIsNotAnAdmin() {
            ServiceException serviceException =
                    assertThrowsExactly(ServiceException.class, PrincipalValidator::validateAdminPrincipal);
            assertEquals(FORBIDDEN, serviceException.getStatusCode());
        }

        @Test
        @WithMockCustomUser(ADMIN)
        @DisplayName("returns principal when user is an admin")
        void returnsPrincipalWhenUserIsAnAdmin() {
            User principal = pullPrincipalUser();

            User user = assertDoesNotThrow(PrincipalValidator::validateAdminPrincipal);
            assertEquals(principal, user);
        }

        @Test
        @WithMockCustomUser(ADMIN_USER)
        @DisplayName("returns principal when user is an admin with additional authorities")
        void returnsPrincipalWhenUserIsAnAdminWithAdditionalAuthorities() {
            User principal = pullPrincipalUser();

            User user = assertDoesNotThrow(PrincipalValidator::validateAdminPrincipal);
            assertEquals(principal, user);
        }
    }
}

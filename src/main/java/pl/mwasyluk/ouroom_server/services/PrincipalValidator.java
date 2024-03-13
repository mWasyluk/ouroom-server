package pl.mwasyluk.ouroom_server.services;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.NonNull;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;
import pl.mwasyluk.ouroom_server.exceptions.ServiceException;

public class PrincipalValidator {
    private static User getPrincipalUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof User user ? user : null;
    }

    public static @NonNull User validatePrincipal() {
        User principal = getPrincipalUser();
        if (principal == null) {
            throw new ServiceException(HttpStatus.UNAUTHORIZED, "This operation requires authentication.");
        }
        return principal;
    }

    public static @NonNull User validateAdminPrincipal() {
        User principal = validatePrincipal();
        if (!principal.getAuthorities().contains(UserAuthority.ADMIN)) {
            throw new ServiceException(HttpStatus.FORBIDDEN, "This operation requires admin authority.");
        }
        return principal;
    }
}

package pl.mwasyluk.ouroom_server.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import pl.mwasyluk.ouroom_server.domain.user.User;

public class WithUserDetailsSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    public static User pullPrincipalUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private final UserDetailsService userDetailsService;

    public WithUserDetailsSecurityContextFactory() {
        this.userDetailsService = new MockUserDetailsService();
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User principal = (User) userDetailsService.loadUserByUsername(customUser.name());

        Authentication auth =
                UsernamePasswordAuthenticationToken.authenticated(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}

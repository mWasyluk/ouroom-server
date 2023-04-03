package pl.mwasyluk.ouroom_server.domain.userdetails;

import org.springframework.security.core.GrantedAuthority;

public enum UserAuthority implements GrantedAuthority {
    ADMIN("ADMIN"), USER("USER");

    private final String authority;

    UserAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }
}

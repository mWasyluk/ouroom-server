package pl.mwasyluk.ouroom_server.domain.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import pl.mwasyluk.ouroom_server.converters.AuthoritySetConverter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Embeddable
public class UserAccount {

    @NonNull
    private String email;

    @NonNull
    private String password;

    @NonNull
    private AuthProvider provider;

    @NonNull
    @Column(length = 16)
    @Convert(converter = AuthoritySetConverter.class)
    private EnumSet<UserAuthority> authorities;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    public UserAccount(@NonNull String email, @NonNull String password) {
        this(email, password, Collections.singleton(UserAuthority.USER));
    }

    public UserAccount(@NonNull String email, @NonNull String password,
            @NonNull Set<UserAuthority> authorities) {
        this.email = email;
        this.password = password;
        this.setAuthorities(authorities);
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = true;
        this.provider = AuthProvider.LOCAL;
    }

    public @NonNull Set<UserAuthority> getAuthorities() {
        return Collections.unmodifiableSet(authorities);
    }

    public void setAuthorities(Set<UserAuthority> authorities) {
        this.authorities = EnumSet.copyOf(authorities);
    }
}

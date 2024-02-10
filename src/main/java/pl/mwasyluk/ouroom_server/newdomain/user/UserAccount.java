package pl.mwasyluk.ouroom_server.newdomain.user;

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
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;
import pl.mwasyluk.ouroom_server.newdomain.converter.AuthoritySetConverter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "user_accounts")
public class UserAccount extends Identifiable {

    @NonNull
    private String email;

    @NonNull
    private String password;

    // TODO: provide enum type
    @NonNull
    private String provider;

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
        this.provider = "LOCAL";
    }

    @Override
    public @NonNull Set<UserAuthority> getAuthorities() {
        return Collections.unmodifiableSet(authorities);
    }

    public void setAuthorities(Set<UserAuthority> authorities) {
        this.authorities = EnumSet.copyOf(authorities);
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}

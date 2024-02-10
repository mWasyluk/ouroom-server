package pl.mwasyluk.ouroom_server.newdomain.user;

import java.util.Collection;

import org.springframework.security.core.userdetails.UserDetails;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.newdomain.Identifiable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor

@Entity
@Table(name = "users")
public class User extends Identifiable implements UserDetails {
    @NonNull
    @Setter(AccessLevel.PROTECTED)
    @Embedded
    protected UserAccount account;

    @Embedded
    protected UserProfile profile;

    public String getProvider() {
        return account.getProvider();
    }

    @Override
    public Collection<UserAuthority> getAuthorities() {
        return account.getAuthorities();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return account.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return account.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return account.isEnabled();
    }
}

package pl.mwasyluk.ouroom_server.domain.user;

import java.util.Collection;
import java.util.UUID;

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

import pl.mwasyluk.ouroom_server.domain.Identifiable;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor

@Entity
@Table(name = "users")
public class User extends Identifiable implements UserDetails {

    /**
     @param id
     id to be set as the mock User's id;

     @return Mock User instance which should only be used after verifying that User with the given id exists in the
     database.
     */
    public static User mockOf(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

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

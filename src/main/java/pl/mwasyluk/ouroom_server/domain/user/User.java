package pl.mwasyluk.ouroom_server.domain.user;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import pl.mwasyluk.ouroom_server.domain.Identifiable;
import pl.mwasyluk.ouroom_server.domain.Presentable;
import pl.mwasyluk.ouroom_server.domain.media.Image;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "users")
public class User extends Identifiable implements UserDetails, Presentable {
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
    @Setter(AccessLevel.PRIVATE)
    @Embedded
    protected UserAccount account;

    @Setter(AccessLevel.PRIVATE)
    @Embedded
    protected UserProfile profile;

    public User(@NonNull String username, @NonNull String password) {
        this(username, password, Collections.singleton(UserAuthority.USER));
    }

    public User(@NonNull String username, @NonNull String password,
            @NonNull Set<UserAuthority> authorities) {
        this.accountBuilder()
                .setUsername(username)
                .setPassword(password)
                .setAuthorities(authorities)
                .setProvider(AuthProvider.LOCAL)
                .setAccountNonExpired(true)
                .setAccountNonLocked(true)
                .setCredentialsNonExpired(true)
                .setEnabled(false)
                .apply();
    }

    public UserAccount.AccountBuilder accountBuilder() {
        return new UserAccount.AccountBuilder(this);
    }

    public UserProfile.ProfileBuilder profileBuilder() {
        return new UserProfile.ProfileBuilder(this);
    }

    public AuthProvider getProvider() {
        return account.getProvider();
    }

    @Override
    public String getName() {
        return profile == null ? null : profile.getName();
    }

    @Override
    public Image getImage() {
        return profile == null ? null : profile.getImage();
    }

    @Override
    public EnumSet<UserAuthority> getAuthorities() {
        return account.getAuthorities();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
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

    protected static abstract class UserPropertiesBuilder {
        protected final User target;

        protected UserPropertiesBuilder(@NonNull User target) {
            this.target = target;
        }

        protected abstract void validate();

        public User apply() {
            validate();
            return target;
        }
    }
}

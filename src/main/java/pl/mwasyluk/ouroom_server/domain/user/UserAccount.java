package pl.mwasyluk.ouroom_server.domain.user;

import java.util.EnumSet;
import java.util.Set;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import pl.mwasyluk.ouroom_server.converters.AuthoritySetConverter;
import pl.mwasyluk.ouroom_server.exceptions.InitializationException;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Embeddable
public class UserAccount {
    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private AuthProvider provider;

    @Column(length = 16)
    @Convert(converter = AuthoritySetConverter.class)
    private EnumSet<UserAuthority> authorities;

    private boolean accountNonExpired;
    /**
     When Account is locked it means some problems with the user.
     <br> Only non-locked users can log in, but the property does not affect their visibility.
     */
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    /**
     When Account is enabled it means it has been verified.
     <br> Only enabled users can log in, appear in search results and their presentable is visible above messages.
     <br> Deleted user becomes disabled and eventually effectively removed from the application.
     */
    private boolean enabled;

    private UserAccount(AccountBuilder accountBuilder) {
        this.username = accountBuilder.username;
        this.password = accountBuilder.password;
        this.authorities = accountBuilder.authorities;
        this.accountNonExpired = accountBuilder.accountNonExpired;
        this.accountNonLocked = accountBuilder.accountNonLocked;
        this.credentialsNonExpired = accountBuilder.credentialsNonExpired;
        this.enabled = accountBuilder.enabled;
        this.provider = accountBuilder.provider;
    }

    public @NonNull EnumSet<UserAuthority> getAuthorities() {
        return EnumSet.copyOf(authorities);
    }

    public static class AccountBuilder extends User.UserPropertiesBuilder {
        private String username;
        private String password;
        private AuthProvider provider;
        private EnumSet<UserAuthority> authorities;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        private boolean enabled;

        protected AccountBuilder(@NonNull User user) {
            super(user);
            if (target.account != null) {
                username = target.account.username;
                password = target.account.password;
                authorities = target.account.authorities;
                accountNonExpired = target.account.accountNonExpired;
                accountNonLocked = target.account.accountNonLocked;
                credentialsNonExpired = target.account.credentialsNonExpired;
                enabled = target.account.enabled;
                provider = target.account.provider;
            }
        }

        public AccountBuilder setUsername(String username) {
            this.username = username.trim();
            return this;
        }

        public AccountBuilder setPassword(String password) {
            this.password = password.trim();
            return this;
        }

        public AccountBuilder setProvider(AuthProvider provider) {
            this.provider = provider;
            return this;
        }

        public AccountBuilder setAuthorities(Set<UserAuthority> authorities) {
            this.authorities = EnumSet.copyOf(authorities);
            return this;
        }

        public AccountBuilder setAccountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }

        public AccountBuilder setAccountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }

        public AccountBuilder setCredentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }

        public AccountBuilder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        protected void validate() {
            if (username == null || username.isBlank()) {
                throw new InitializationException("Username cannot be empty");
            }
            if ((password == null || password.isBlank()) && provider == AuthProvider.LOCAL) {
                throw new InitializationException("Password cannot be empty for a local user.");
            }
            target.account = new UserAccount(this);
        }
    }
}

package pl.mwasyluk.ouroom_server.dto.user;

import java.util.Collection;
import java.util.UUID;

import lombok.NonNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import pl.mwasyluk.ouroom_server.domain.user.User;
import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;

public record UserDetailsView(
        UUID userId,
        AccountDetailsView accountDetails,
        @JsonIgnoreProperties({"id"}) UserPresentableView view
) {
    public UserDetailsView(User user) {
        this(user.getId(),
                new AccountDetailsView(user),
                new UserPresentableView(user));
    }

    record AccountDetailsView(
            String email,
            Collection<UserAuthority> authorities,
            String provider,
            boolean nonExpired,
            boolean nonLocked,
            boolean credentialsNonExpired,
            boolean enabled
    ) {
        public AccountDetailsView(@NonNull User user) {
            this(user.getUsername(),
                    user.getAuthorities(),
                    user.getProvider().toString(),
                    user.isAccountNonExpired(),
                    user.isAccountNonLocked(),
                    user.isCredentialsNonExpired(),
                    user.isEnabled());
        }
    }
}

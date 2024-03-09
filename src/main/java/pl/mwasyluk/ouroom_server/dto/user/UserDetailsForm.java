package pl.mwasyluk.ouroom_server.dto.user;

import java.util.EnumSet;

import lombok.Data;

import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;

@Data
public class UserDetailsForm {
    private String email;
    private String password;
    private EnumSet<UserAuthority> authorities;

    public String getEmail() {
        return email == null || email.isBlank() ? null : email.trim();
    }

    public String getPassword() {
        return password == null || password.isBlank() ? null : password.trim();
    }
}

package pl.mwasyluk.ouroom_server.newdomain.converter;

import jakarta.persistence.Converter;

import pl.mwasyluk.ouroom_server.newdomain.user.UserAuthority;

@Converter
public class AuthoritySetConverter extends EnumSetConverter<UserAuthority> {

    @Override
    Class<UserAuthority> clazz() {
        return UserAuthority.class;
    }
}

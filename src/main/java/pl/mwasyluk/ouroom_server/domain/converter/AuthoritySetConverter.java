package pl.mwasyluk.ouroom_server.domain.converter;

import jakarta.persistence.Converter;

import pl.mwasyluk.ouroom_server.domain.user.UserAuthority;

@Converter
public class AuthoritySetConverter extends EnumSetConverter<UserAuthority> {

    @Override
    Class<UserAuthority> clazz() {
        return UserAuthority.class;
    }
}

package pl.mwasyluk.ouroom_server.domain.converter;

import jakarta.persistence.Converter;

import pl.mwasyluk.ouroom_server.domain.member.MemberPrivilege;

@Converter
public class MemberPrivilegeSetConverter extends EnumSetConverter<MemberPrivilege> {

    @Override
    Class<MemberPrivilege> clazz() {
        return MemberPrivilege.class;
    }
}

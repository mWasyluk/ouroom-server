package pl.mwasyluk.ouroom_server.newdomain.converter;

import jakarta.persistence.Converter;

import pl.mwasyluk.ouroom_server.newdomain.member.MemberPrivilege;

@Converter
public class MemberPrivilegeSetConverter extends EnumSetConverter<MemberPrivilege> {

    @Override
    Class<MemberPrivilege> clazz() {
        return MemberPrivilege.class;
    }
}

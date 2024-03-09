package pl.mwasyluk.ouroom_server.utils;

import java.util.regex.Pattern;

public class BcryptUtils {
    public static final String BCRYPT_PREFIX = "{bcrypt}";
    private static final String BCRYPT_PREFIX_REGEX = "\\" + BCRYPT_PREFIX;
    public static final Pattern BCRYPT_PATTERN =
            Pattern.compile("\\A" + BCRYPT_PREFIX_REGEX + "\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");
}

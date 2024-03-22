package pl.mwasyluk.ouroom_server.utils;

import java.util.regex.Pattern;

public class BcryptUtils {
    public static final String BCRYPT_REGEX = "^\\$2[aby]?\\$\\d{2}\\$[./0-9A-Za-z]{53}$";
    public static final String BCRYPT_PREFIX = "{bcrypt}";

    public static boolean isBcryptWithPrefix(String hash) {
        return hash.startsWith(BCRYPT_PREFIX) && isBcrypt(hash.substring(BCRYPT_PREFIX.length()));
    }
    
    public static boolean isBcrypt(String hash) {
        return Pattern.compile(BCRYPT_REGEX).matcher(hash).matches();
    }
}

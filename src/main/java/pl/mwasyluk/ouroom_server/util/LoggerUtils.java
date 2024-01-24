package pl.mwasyluk.ouroom_server.util;

public class LoggerUtils {
    public String somethingUnexpectedAndInvolved(Object... involved) {
        var sb = new StringBuilder("Something unexpected happened. Involved instances:");

        for (Object i : involved) {
            sb.append("\n");
            sb.append(i);
        }

        return sb.toString();
    }
}

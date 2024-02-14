package pl.mwasyluk.ouroom_server.util;

public class LoggerUtils {
    public static String involved(Object... involved) {
        var sb = new StringBuilder("Involved instances:");

        for (Object i : involved) {
            sb.append("\n");
            sb.append(i);
        }

        return sb.toString();
    }

    public static String operationFailedDueTo(String operation, String reason, Object... involved) {
        return "The " + operation + " operation failed due to " + reason + ". " + involved(involved);
    }
}

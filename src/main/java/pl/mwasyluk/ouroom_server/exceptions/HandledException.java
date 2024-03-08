package pl.mwasyluk.ouroom_server.exceptions;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public abstract class HandledException extends ResponseStatusException {
    public HandledException(HttpStatusCode status, String reason) {
        super(status, reason);
    }

    public HandledException(HttpStatusCode status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getStackTrace().length > 1) {
            StackTraceElement element = getStackTrace()[0];
            sb.append(element.getFileName())
                    .append(":")
                    .append(element.getLineNumber())
                    .append(" > ");
        }
        sb.append(getClass().getSimpleName())
                .append(" > ")
                .append(getStatusCode());

        String reason = getReason();
        if (reason != null) {
            sb.append(" > \"")
                    .append(reason)
                    .append("\"");
        }
        return sb.toString();
    }
}

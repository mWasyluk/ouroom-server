package pl.mwasyluk.ouroom_server.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketInterceptorException extends RuntimeException {
    public WebSocketInterceptorException(String message) {
        super(message);
        log.debug(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getStackTrace().length > 1) {
            StackTraceElement element = getStackTrace()[0];
            sb.append(element.getFileName())
                    .append(":")
                    .append(element.getLineNumber());
        }

        sb.append(" > \"")
                .append(getMessage())
                .append("\"");
        return sb.toString();
    }
}

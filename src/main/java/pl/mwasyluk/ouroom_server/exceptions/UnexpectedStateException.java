package pl.mwasyluk.ouroom_server.exceptions;

import org.springframework.http.HttpStatus;

public class UnexpectedStateException extends HandledException {
    public UnexpectedStateException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }
}

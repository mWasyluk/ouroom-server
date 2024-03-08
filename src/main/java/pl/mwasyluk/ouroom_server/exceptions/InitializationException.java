package pl.mwasyluk.ouroom_server.exceptions;

import org.springframework.http.HttpStatus;

public class InitializationException extends HandledException {
    public InitializationException(String reason) {
        super(HttpStatus.PRECONDITION_FAILED, reason, null);
    }
}

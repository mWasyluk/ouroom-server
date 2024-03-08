package pl.mwasyluk.ouroom_server.exceptions;

import org.springframework.http.HttpStatus;

public class ConversionException extends HandledException {
    public ConversionException(String reason) {
        super(HttpStatus.PRECONDITION_FAILED, reason, null);
    }
}

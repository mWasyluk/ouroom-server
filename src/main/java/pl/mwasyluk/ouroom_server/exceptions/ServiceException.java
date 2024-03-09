package pl.mwasyluk.ouroom_server.exceptions;

import org.springframework.http.HttpStatusCode;

public class ServiceException extends HandledException {
    public ServiceException(HttpStatusCode status, String reason) {
        super(status, reason);
    }
}

package pl.mwasyluk.ouroom_server.web.http.exception;

import static pl.mwasyluk.ouroom_server.data.service.support.ServiceResponseMessages.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import pl.mwasyluk.ouroom_server.data.service.support.ServiceResponse;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(value = { HttpMessageNotReadableException.class, NullPointerException.class })
    public ResponseEntity<?> handleHttpMessageNotReadableException() {
        return new ServiceResponse<>(INCORRECT_OBJECT_PROVIDED, HttpStatus.BAD_REQUEST).getResponseEntity();
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupportedException() {
        return new ServiceResponse<>(UNSUPPORTED_IMAGE_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .getResponseEntity();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException() {
        return new ServiceResponse<>(PAYLOAD_TOO_LARGE, HttpStatus.PAYLOAD_TOO_LARGE).getResponseEntity();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException() {
        return new ServiceResponse<>(UNAUTHORIZED, HttpStatus.UNAUTHORIZED).getResponseEntity();
    }
}

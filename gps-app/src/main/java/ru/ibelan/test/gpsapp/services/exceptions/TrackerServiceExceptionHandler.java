package ru.ibelan.test.gpsapp.services.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class TrackerServiceExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ReceiveGpsException.class)
    public ResponseEntity<String> handleReceiveGpsException(ReceiveGpsException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

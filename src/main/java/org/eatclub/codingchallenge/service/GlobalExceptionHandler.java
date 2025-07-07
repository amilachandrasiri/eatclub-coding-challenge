package org.eatclub.codingchallenge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException validationEx) {
        log.error("Validation exception: {}", validationEx.getMessage());

        return new ResponseEntity<>("Invalid parameter", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<String> handleValidationExceptions(ServerWebInputException exception) {
        log.error("Validation exception: {}", exception.getDetailMessageArguments());

        return new ResponseEntity<>("Invalid parameter", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllOtherExceptions(Exception exception) {
        log.error("Exception occurred", exception);

        return new ResponseEntity<>("Internal Server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}


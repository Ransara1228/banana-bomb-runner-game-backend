package com.bbr.exception;

import com.bbr.dto.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Turns errors into {"message": "..."} so the frontend can show them. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MessageResponse> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new MessageResponse(e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> invalid(MethodArgumentNotValidException e) {
        String field = e.getBindingResult().getFieldErrors().isEmpty()
                ? "input" : e.getBindingResult().getFieldErrors().get(0).getField();
        return ResponseEntity.badRequest().body(new MessageResponse("Please check the " + field + " field"));
    }
}
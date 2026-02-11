package com.backend.givr.shared;

import com.backend.givr.shared.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.ParseException;

@RestControllerAdvice
public class ControllerAdviser {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String>  handleEntityNotFoundException(EntityNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Entity does not exist %s", e.getLocalizedMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String>  handleUsernameNotFoundException(UsernameNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Entity does not exist %s", e.getLocalizedMessage()));
    }

    @ExceptionHandler(CredentialsChangedException.class)
    public ResponseEntity<String> handleCredentialsChangedException(CredentialsChangedException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getLocalizedMessage());
    }

    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<String> handleIllegalOperation(IllegalOperationException e){
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public  ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(String.format("Cannot pass null argument %s", e.getLocalizedMessage()));
    }
    @ExceptionHandler(MaxApplicantsReachedException.class)
    public ResponseEntity<String> handleMaxApplicationException(MaxApplicantsReachedException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE.value()).body(String.format("Max number of applicants for project reached %s", e.getLocalizedMessage()));
    }

    @ExceptionHandler(ProjectDeadlinePastException.class)
    public ResponseEntity<String> handleProjectDeadlinePastException(ProjectDeadlinePastException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE.value()).body(String.format("Deadline for this project as passed, cannot apply: %s", e.getLocalizedMessage()));
    }
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<String> handleDuplicateAccountException(DuplicateAccountException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format("Failed to create account: %s", e.getLocalizedMessage()));
    }
    @ExceptionHandler(InconsistentProjectDatesException.class)
    public ResponseEntity<String> handleInconsistentProjectDateException(InconsistentProjectDatesException e){
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }

    @ExceptionHandler(FailedToSendOTPException.class)
    public ResponseEntity<String> handleFailedToSendOtpException(FailedToSendOTPException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getLocalizedMessage());
    }
    @ExceptionHandler(value = ParseException.class)
    public ResponseEntity<String> handleParseException(ParseException e){
        return ResponseEntity.unprocessableEntity().body(String.format("Incompatible date format, expected format yyyy-MM-dd. %s", e.getLocalizedMessage()));
    }

    @ExceptionHandler(value = InvalidOtpException.class)
    public ResponseEntity<String> handleInvalidOtpException(InvalidOtpException e){
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }
}

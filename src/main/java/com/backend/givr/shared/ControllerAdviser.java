package com.backend.givr.shared;

import com.backend.givr.shared.dtos.ErrorMessage;
import com.backend.givr.shared.exceptions.*;
import com.resend.core.exception.ResendException;
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
    public ResponseEntity<ErrorMessage>  handleEntityNotFoundException(EntityNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(String.format("Entity does not exist %s", e.getLocalizedMessage())));
    }

    @ExceptionHandler(BroadcastFailedException.class)
    public ResponseEntity<ErrorMessage> handleResendException(BroadcastFailedException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(String.format("Failed to broadcast email %s", e.getLocalizedMessage())));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorMessage>  handleUsernameNotFoundException(UsernameNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(String.format("Entity does not exist %s", e.getLocalizedMessage())));
    }

    @ExceptionHandler(CredentialsChangedException.class)
    public ResponseEntity<ErrorMessage> handleCredentialsChangedException(CredentialsChangedException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessage(e.getLocalizedMessage()));
    }

    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<ErrorMessage> handleIllegalOperation(IllegalOperationException e){
        return ResponseEntity.badRequest().body(new ErrorMessage(e.getLocalizedMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public  ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(new ErrorMessage(String.format("Cannot pass null argument %s", e.getLocalizedMessage())));
    }
    @ExceptionHandler(MaxApplicantsReachedException.class)
    public ResponseEntity<ErrorMessage> handleMaxApplicationException(MaxApplicantsReachedException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE.value()).body(new ErrorMessage(String.format("Max number of applicants for project reached %s", e.getLocalizedMessage())));
    }

    @ExceptionHandler(ProjectDeadlinePastException.class)
    public ResponseEntity<ErrorMessage> handleProjectDeadlinePastException(ProjectDeadlinePastException e){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE.value()).body(new ErrorMessage(String.format("Deadline for this project as passed, cannot apply: %s", e.getLocalizedMessage())));
    }
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<ErrorMessage> handleDuplicateAccountException(DuplicateAccountException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessage(String.format("Failed to create account: %s", e.getLocalizedMessage())));
    }
    @ExceptionHandler(InconsistentProjectDatesException.class)
    public ResponseEntity<ErrorMessage> handleInconsistentProjectDateException(InconsistentProjectDatesException e){
        return ResponseEntity.badRequest().body(new ErrorMessage(e.getLocalizedMessage()));
    }

    @ExceptionHandler(FailedToSendOTPException.class)
    public ResponseEntity<ErrorMessage> handleFailedToSendOtpException(FailedToSendOTPException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(e.getLocalizedMessage()));
    }
    @ExceptionHandler(value = ParseException.class)
    public ResponseEntity<ErrorMessage> handleParseException(ParseException e){
        return ResponseEntity.unprocessableEntity().body(new ErrorMessage(String.format("Incompatible date format, expected format yyyy-MM-dd. %s", e.getLocalizedMessage())));
    }

    @ExceptionHandler(value = InvalidOtpException.class)
    public ResponseEntity<ErrorMessage> handleInvalidOtpException(InvalidOtpException e){
        return ResponseEntity.badRequest().body(new ErrorMessage(e.getLocalizedMessage()));
    }
}

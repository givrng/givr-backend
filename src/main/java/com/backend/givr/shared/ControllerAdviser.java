package com.backend.givr.shared;

import com.backend.givr.shared.exceptions.DuplicateAccountException;
import com.backend.givr.shared.exceptions.InconsistentProjectDatesException;
import com.backend.givr.shared.exceptions.MaxApplicantsReachedException;
import com.backend.givr.shared.exceptions.ProjectDeadlinePastException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdviser {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String>  handleEntityNotFoundException(EntityNotFoundException e){
        return ResponseEntity.badRequest().body(String.format("Entity does not exist %s", e.getLocalizedMessage()));
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
        return ResponseEntity.badRequest().body(String.format("Failed to create account: %s", e.getLocalizedMessage()));
    }
    @ExceptionHandler(InconsistentProjectDatesException.class)
    public ResponseEntity<String> handleInconsistentProjectDateException(InconsistentProjectDatesException e){
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }
}

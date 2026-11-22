package com.cultswap.backend.exception;

import com.cultswap.backend.dto.ApiErrorResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception) {

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEmail(
            InvalidCredentialsException exception) {

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleListingNotFound(
            ListingNotFoundException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                Collections.emptyMap()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(TransferRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferRequestException(
            TransferRequestException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                Collections.emptyMap()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex
    ) {
        String message = "This operation conflicts with existing data";

        Throwable cause = ex;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException constraintEx) {

                String constraintName = constraintEx.getConstraintName();

                if ("uq_pending_request_per_buyer_listing".equals(constraintName)) {
                    message = "You already have a pending request for this listing";
                } else if ("uq_accepted_request_per_listing".equals(constraintName)) {
                    message = "Another transfer request has already been accepted for this listing";
                }

                break;
            }

            cause = cause.getCause();
        }

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                message,
                Map.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ListingOwnershipException.class)
    public ResponseEntity<ApiErrorResponse> handleListingOwnershipException(
            ListingOwnershipException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                403,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(ListingStateException.class)
    public ResponseEntity<ApiErrorResponse> handleListingStateException(
            ListingStateException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                409,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                400,
                "Invalid request parameters",
                null
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                409,
                "This listing was modified by another request. Please refresh and try again.",
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(TransferRequestStateException.class)
    public ResponseEntity<ApiErrorResponse> handleTransferRequestStateException(
            TransferRequestStateException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleListingOwnershipException(
            UserNotFoundException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                404,
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
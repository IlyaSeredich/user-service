package com.innowise.userservice.exception.handler;

import com.innowise.userservice.dto.ErrorResponseDto;
import com.innowise.userservice.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailNotFoundException(
            EmailNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExistException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyActiveException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyActiveException(
            UserAlreadyActiveException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserAlreadyNonActiveException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyNonActiveException(
            UserAlreadyNonActiveException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentCardAlreadyActiveException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentCardAlreadyActiveException(
            PaymentCardAlreadyActiveException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentCardAlreadyNonActiveException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentCardAlreadyNonActiveException(
            PaymentCardAlreadyNonActiveException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentCardNumberAlreadyExistException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNumberAlreadyExistException(
            PaymentCardNumberAlreadyExistException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentCardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotFoundException(
            PaymentCardNotFoundException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaymentCardLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleCardLimitExceededException(
            PaymentCardLimitExceededException ex,
            HttpServletRequest request
    ) {
        ErrorResponseDto responseDto = new ErrorResponseDto(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI()
        );

        return new ResponseEntity<>(responseDto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponseDto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Invalid request");


        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                message,
                HttpStatus.BAD_REQUEST,
                request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponseDto);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponseDto> handleException(
//            HttpServletRequest request
//    ) {
//        ErrorResponseDto responseDto = new ErrorResponseDto(
//                "An unexpected error occurred",
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                request.getRequestURI()
//        );
//
//        return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

}


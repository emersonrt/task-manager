package com.emersonrt.taskmanager.config;

import com.emersonrt.taskmanager.exception.TaskBaseException;
import com.emersonrt.taskmanager.util.dto.ErrorMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageDTO> handleBaseRequestException(Exception ex) {
        ErrorMessageDTO errorMessageDTO = new ErrorMessageDTO(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                "Sorry, an unexpected error occurred",
                Collections.emptyList()
        );
        return new ResponseEntity<>(errorMessageDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TaskBaseException.class)
    public ResponseEntity<ErrorMessageDTO> handleBaseRequestException(TaskBaseException ex) {
        ErrorMessageDTO errorMessageDTO = ex.getErrorDTO();
        return new ResponseEntity<>(errorMessageDTO, HttpStatusCode.valueOf(Integer.parseInt(errorMessageDTO.code())));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessageDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorMessageDTO errorMessageDTO = new ErrorMessageDTO(
                String.valueOf(HttpStatus.BAD_REQUEST.value()),
                ex.getMessage(),
                Collections.emptyList()
        );
        return new ResponseEntity<>(errorMessageDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

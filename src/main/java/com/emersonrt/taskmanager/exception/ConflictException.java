package com.emersonrt.taskmanager.exception;

import com.emersonrt.taskmanager.util.dto.ErrorMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends TaskBaseException {

    public ConflictException(ErrorMessageDTO errorMessageDTO) {
        super(errorMessageDTO);
    }

    public ConflictException(String message) {
        super(new ErrorMessageDTO(String.valueOf(HttpStatus.CONFLICT.value()), message));
    }
}
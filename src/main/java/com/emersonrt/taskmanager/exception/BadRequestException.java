package com.emersonrt.taskmanager.exception;

import com.emersonrt.taskmanager.util.dto.ErrorMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends TaskBaseException {
    public BadRequestException(ErrorMessageDTO errorMessageDTO) {
        super(errorMessageDTO);
    }

    public BadRequestException(String message) {
        super(new ErrorMessageDTO(String.valueOf(HttpStatus.BAD_REQUEST.value()), message));
    }
}

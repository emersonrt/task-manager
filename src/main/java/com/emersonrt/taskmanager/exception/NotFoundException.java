package com.emersonrt.taskmanager.exception;

import com.emersonrt.taskmanager.util.dto.ErrorMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends TaskBaseException {
    public NotFoundException(ErrorMessageDTO errorMessageDTO) {
        super(errorMessageDTO);
    }

    public NotFoundException(String message) {
        super(new ErrorMessageDTO(String.valueOf(HttpStatus.NOT_FOUND.value()), message));
    }
}

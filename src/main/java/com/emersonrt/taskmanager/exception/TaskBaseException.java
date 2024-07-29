package com.emersonrt.taskmanager.exception;

import com.emersonrt.taskmanager.util.dto.ErrorMessageDTO;

public class TaskBaseException extends RuntimeException {

    private final ErrorMessageDTO errorMessageDTO;

    public TaskBaseException(ErrorMessageDTO errorMessageDTO) {
        super(errorMessageDTO.message());
        this.errorMessageDTO = errorMessageDTO;
    }

    public ErrorMessageDTO getErrorDTO() {
        return errorMessageDTO;
    }

}

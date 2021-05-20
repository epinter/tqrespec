/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public class InvalidVariableException extends RuntimeException {
    public InvalidVariableException() {
        super();
    }

    public InvalidVariableException(String message) {
        super(message);
    }

    public InvalidVariableException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVariableException(Throwable cause) {
        super(cause);
    }

    protected InvalidVariableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

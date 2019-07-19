/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

public class UnhandledRuntimeException extends RuntimeException {
    public UnhandledRuntimeException() {
    }

    public UnhandledRuntimeException(String message) {
        super(message);
    }

    public UnhandledRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnhandledRuntimeException(Throwable cause) {
        super(cause);
    }

    public UnhandledRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

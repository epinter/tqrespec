/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public class IncompatibleSavegameException extends Exception {
    public IncompatibleSavegameException() {
        super();
    }

    public IncompatibleSavegameException(String message) {
        super(message);
    }

    public IncompatibleSavegameException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleSavegameException(Throwable cause) {
        super(cause);
    }

}

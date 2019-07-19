/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

import javafx.event.Event;
import javafx.event.EventHandler;

@FunctionalInterface
public interface MyEventHandler<T extends Event> extends EventHandler<T> {
    @Override
    default void handle(T t) {
        try {
            handleEvent(t);
        } catch (Exception e) {
            throw new UnhandledRuntimeException(e);
        }
    }

    void handleEvent(T t);
}

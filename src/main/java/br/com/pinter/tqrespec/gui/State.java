/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

public class State {
    private static State instance = null;
    private Boolean saveInProgress = null;


    public static State get() {
        if (instance == null) {
            synchronized (State.class) {
                if (instance == null) {
                    instance = new State();
                }
            }
        }
        return instance;
    }

    public Boolean getSaveInProgress() {
        return saveInProgress;
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress = saveInProgress;
    }

}

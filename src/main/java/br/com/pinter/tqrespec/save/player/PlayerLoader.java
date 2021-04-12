/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

public class PlayerLoader extends Player {
    private CurrentPlayerData saveDataPrivate;

    @Override
    public void prepareSaveData() {
        saveDataPrivate = new CurrentPlayerData();
    }

    @Override
    public CurrentPlayerData getSaveData() {
        return saveDataPrivate;
    }
}
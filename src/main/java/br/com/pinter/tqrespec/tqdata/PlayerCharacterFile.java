/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.save.SaveLocation;

public class PlayerCharacterFile {
    private final String playerName;
    private final SaveLocation location;

    public PlayerCharacterFile(String playerName, SaveLocation location) {
        this.playerName = playerName;
        this.location = location;
    }

    public String getPlayerName() {
        return playerName;
    }

    public SaveLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return playerName;
    }
}

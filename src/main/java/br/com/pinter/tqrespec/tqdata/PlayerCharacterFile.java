/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

public class PlayerCharacterFile {
    private final String playerName;
    private final boolean external;

    public PlayerCharacterFile(String playerName, boolean external) {
        this.playerName = playerName;
        this.external = external;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public String toString() {
        return playerName;
    }
}

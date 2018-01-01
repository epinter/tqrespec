/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

public class HeaderInfo {
    private int headerVersion = -1;
    private String playerCharacterClass = null;
    private String playerClassTag = null;
    private int playerLevel = -1;
    private int playerVersion = -1;

    public int getHeaderVersion() {
        return headerVersion;
    }

    public void setHeaderVersion(int headerVersion) {
        this.headerVersion = headerVersion;
    }

    public String getPlayerCharacterClass() {
        return playerCharacterClass;
    }

    public void setPlayerCharacterClass(String playerCharacterClass) {
        this.playerCharacterClass = playerCharacterClass;
    }

    public String getPlayerClassTag() {
        return playerClassTag;
    }

    public void setPlayerClassTag(String playerClassTag) {
        this.playerClassTag = playerClassTag;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int getPlayerVersion() {
        return playerVersion;
    }

    public void setPlayerVersion(int playerVersion) {
        this.playerVersion = playerVersion;
    }
}

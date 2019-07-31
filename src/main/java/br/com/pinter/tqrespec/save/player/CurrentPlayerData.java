/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.ChangesTable;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class CurrentPlayerData implements Serializable {
    @Inject
    private GameInfo gameInfo;

    private String playerName = null;
    private boolean customQuest = false;
    private final LinkedHashMap<String, PlayerSkill> playerSkills = new LinkedHashMap<>();
    private ChangesTable changes = new ChangesTable();
    private HeaderInfo headerInfo = new HeaderInfo();
    private ByteBuffer buffer = null;

    String getPlayerName() {
        return playerName;
    }

    void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    boolean isCustomQuest() {
        return customQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        this.customQuest = customQuest;
    }

    String getPlayerClassTag() {
        if (getHeaderInfo() != null) {
            return getHeaderInfo().getPlayerClassTag();
        }
        return null;
    }

    Map<String, PlayerSkill> getPlayerSkills() {
        return playerSkills;
    }

    Path getPlayerChr() {
        return gameInfo.playerChr(playerName, customQuest);
    }

    HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    ChangesTable getChanges() {
        return changes;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }

    void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    void reset() {
        this.buffer = null;
        this.headerInfo = new HeaderInfo();
        this.changes = new ChangesTable();
        this.playerName = null;
        this.customQuest = false;
        this.playerSkills.clear();
    }
}

/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Respec is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Respec.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqdatabase.Skill;
import br.com.pinter.tqrespec.Data;
import br.com.pinter.tqrespec.Util;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerData {
    private static PlayerData instance = null;
    private String playerName = null;
    private Path playerChr = null;
    private ByteBuffer buffer = null;
    private Hashtable<Integer, BlockInfo> blockInfo = null;
    private HeaderInfo headerInfo = null;
    private Hashtable<String, ArrayList<Integer>> variableLocation = null;
    private ChangesTable changes = null;
    private Boolean saveInProgress = null;
    private boolean isCustomQuest = false;

    public static PlayerData getInstance() {
        if (instance == null) {
            synchronized (PlayerData.class) {
                if (instance == null) {
                    instance = new PlayerData();
                    instance.changes = new ChangesTable();
                    instance.variableLocation = new Hashtable<String, ArrayList<Integer>>();
                    instance.blockInfo = new Hashtable<Integer, BlockInfo>();
                }
            }
        }
        return instance;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Hashtable<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Hashtable<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    public Hashtable<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Hashtable<String, ArrayList<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    public ChangesTable getChanges() {
        return changes;
    }

    public Path getPlayerChr() {
        return playerChr;
    }

    public void setPlayerChr(Path playerChr) {
        this.playerChr = playerChr;
    }

    public Boolean getSaveInProgress() {
        return saveInProgress;
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress = saveInProgress;
    }

    public boolean isCustomQuest() {
        return isCustomQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        isCustomQuest = customQuest;
    }

    public boolean loadPlayerData(String playerName) throws Exception {
        if (PlayerData.getInstance().getSaveInProgress() != null && PlayerData.getInstance().getSaveInProgress()) {
            return false;
        }
        try {
            new PlayerParser().player(playerName).parse();
            return true;
        } catch (Exception e) {
            throw e;
        }


    }

    public void reset() {
        this.buffer = null;
        this.headerInfo = null;
        this.blockInfo = new Hashtable<Integer, BlockInfo>();
        this.playerName = null;
        this.variableLocation = variableLocation = new Hashtable<String, ArrayList<Integer>>();
        this.changes = new ChangesTable();
        this.playerChr = null;
        this.saveInProgress = null;
    }
}

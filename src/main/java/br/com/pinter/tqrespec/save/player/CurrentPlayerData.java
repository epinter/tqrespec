/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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

/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec.save.stash;

import br.com.pinter.tqrespec.save.FileDataHolder;
import br.com.pinter.tqrespec.save.FileDataMap;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class StashData implements FileDataHolder {
    private String playerName = null;
    private Path playerPath = null;
    private boolean customQuest = false;
    private FileDataMap dataMap = new FileDataMap();
    private ByteBuffer buffer = null;

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public Path getPlayerPath() {
        return playerPath;
    }

    @Override
    public void setPlayerPath(Path playerPath) {
        this.playerPath = playerPath;
    }

    public boolean isCustomQuest() {
        return customQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        this.customQuest = customQuest;
    }

    @Override
    public FileDataMap getDataMap() {
        return dataMap;
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    void reset() {
        dataMap.clear();
        this.buffer = null;
        this.dataMap = new FileDataMap();
        this.playerName = null;
        this.customQuest = false;
    }
}

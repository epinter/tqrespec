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

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.FileDataHolder;
import br.com.pinter.tqrespec.save.FileDataMap;
import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.SaveLocation;
import com.google.inject.Singleton;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class CurrentPlayerData implements FileDataHolder {
    private final Map<String, PlayerSkill> playerSkills = Collections.synchronizedMap(new LinkedHashMap<>());
    private final AtomicBoolean missingSkills = new AtomicBoolean(false);
    private String playerName = null;
    private Path playerChr = null;
    private FileDataMap dataMap = new FileDataMap();
    private HeaderInfo headerInfo = new HeaderInfo();
    private ByteBuffer buffer = null;
    private SaveLocation location;

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setLocation(SaveLocation location) {
        this.location = location;
    }

    public SaveLocation getLocation() {
        return location;
    }

    boolean isCustomQuest() {
        return SaveLocation.USER.equals(location);
    }

    String getPlayerClassTag() {
        if (getHeaderInfo() != null) {
            return getHeaderInfo().getPlayerClassTag();
        }
        return null;
    }

    String getPlayerCharacterClass() {
        return getHeaderInfo().getPlayerCharacterClass();
    }

    Map<String, PlayerSkill> getPlayerSkills() {
        return playerSkills;
    }

    Path getPlayerChr() {
        return playerChr;
    }

    public void setPlayerChr(Path playerChr) {
        this.playerChr = playerChr;
    }

    HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    @Override
    public Path getPlayerPath() {
        return playerChr.getParent();
    }

    @Override
    public void setPlayerPath(Path playerPath) {
        //not implemented
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public FileDataMap getDataMap() {
        return dataMap;
    }

    public boolean isMissingSkills() {
        return missingSkills.get();
    }

    public void setMissingSkills(boolean newValue) {
        missingSkills.set(newValue);
    }

    public Platform getPlatform() {
        return getDataMap().getPlatform();
    }

    public void setPlatform(Platform platform) {
        getDataMap().setPlatform(platform);
    }

    void reset() {
        dataMap.clear();
        this.buffer = null;
        this.headerInfo = new HeaderInfo();
        this.dataMap = new FileDataMap();
        this.playerName = null;
        this.location = SaveLocation.MAIN;
        this.playerSkills.clear();
        this.missingSkills.set(false);
    }


}

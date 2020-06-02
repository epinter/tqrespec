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

import java.io.Serializable;

@SuppressWarnings("unused")
public class HeaderInfo implements Serializable {
    private int headerVersion = -1;
    private String playerCharacterClass = null;
    private String playerClassTag = null;
    private int playerLevel = -1;
    private int playerVersion = -1;

    int getHeaderVersion() {
        return headerVersion;
    }

    void setHeaderVersion(int headerVersion) {
        this.headerVersion = headerVersion;
    }

    public String getPlayerCharacterClass() {
        return playerCharacterClass;
    }

    void setPlayerCharacterClass(String playerCharacterClass) {
        this.playerCharacterClass = playerCharacterClass;
    }

    String getPlayerClassTag() {
        return playerClassTag;
    }

    void setPlayerClassTag(String playerClassTag) {
        this.playerClassTag = playerClassTag;
    }

    int getPlayerLevel() {
        return playerLevel;
    }

    void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    int getPlayerVersion() {
        return playerVersion;
    }

    void setPlayerVersion(int playerVersion) {
        this.playerVersion = playerVersion;
    }
}

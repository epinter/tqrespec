/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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

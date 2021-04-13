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

import br.com.pinter.tqrespec.save.VariableInfo;

import java.util.List;

public class TeleportDifficulty {
    private int difficulty;
    private int size;
    private int offset;
    private List<VariableInfo> teleportList;

    public TeleportDifficulty(int difficulty, int size, int offset, List<VariableInfo> teleportList) {
        this.difficulty = difficulty;
        this.size = size;
        this.offset = offset;
        this.teleportList = teleportList;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getSize() {
        return size;
    }

    public int getOffset() {
        return offset;
    }

    public List<VariableInfo> getTeleportList() {
        return teleportList;
    }

    @Override
    public String toString() {
        return "TeleportDifficulty{" +
                "difficulty=" + difficulty +
                ", size=" + size +
                ", offset=" + offset +
                ", teleportList=" + teleportList +
                '}';
    }
}

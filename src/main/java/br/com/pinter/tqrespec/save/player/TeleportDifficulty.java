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

import br.com.pinter.tqrespec.save.BlockInfo;
import br.com.pinter.tqrespec.save.UID;
import br.com.pinter.tqrespec.save.VariableInfo;

import java.util.ArrayList;
import java.util.List;

public class TeleportDifficulty {
    private final int difficulty;
    private final int size;
    private final int offset;
    private final BlockInfo blockInfo;
    private final List<VariableInfo> variables;
    private final List<UID> teleports = new ArrayList<>();

    public TeleportDifficulty(int difficulty, int size, int offset, List<VariableInfo> variables, BlockInfo blockInfo) {
        this.difficulty = difficulty;
        this.size = size;
        this.offset = offset;
        this.variables = variables;
        this.blockInfo = blockInfo;
        for (VariableInfo v : variables) {
            if (v.isUid()) {
                teleports.add(new UID((byte[]) v.getValue()));
            }
        }

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

    public List<VariableInfo> getVariables() {
        return variables;
    }

    public List<UID> getTeleports() {
        return teleports;
    }

    public BlockInfo getBlockInfo() {
        return blockInfo;
    }

    @Override
    public String toString() {
        return "TeleportDifficulty{" +
                "difficulty=" + difficulty +
                ", size=" + size +
                ", offset=" + offset +
                ", teleportList=" + variables +
                '}';
    }
}

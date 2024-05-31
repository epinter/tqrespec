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

import br.com.pinter.tqrespec.save.BlockType;
import br.com.pinter.tqrespec.save.FileVariable;
import br.com.pinter.tqrespec.save.VariableType;

import java.util.HashMap;

public class StashFileVariable implements FileVariable {
    private static final HashMap<String, StashFileVariable> map = new HashMap<>();

    static {
        map.put("stashVersion", new StashFileVariable("stashVersion", VariableType.INTEGER, StashBlockType.STASH_MAIN));
        map.put("fName", new StashFileVariable("fName", VariableType.STRING, StashBlockType.STASH_MAIN));
        map.put("sackWidth", new StashFileVariable("sackWidth", VariableType.INTEGER, StashBlockType.STASH_MAIN));
        map.put("sackHeight", new StashFileVariable("sackHeight", VariableType.INTEGER, StashBlockType.STASH_MAIN));
        map.put("numItems", new StashFileVariable("numItems", VariableType.INTEGER, StashBlockType.STASH_MAIN));

        map.put("stackCount", new StashFileVariable("stackCount", VariableType.INTEGER, StashBlockType.STASH_MAIN));
        map.put("baseName", new StashFileVariable("baseName", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("prefixName", new StashFileVariable("prefixName", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("suffixName", new StashFileVariable("suffixName", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("relicName", new StashFileVariable("relicName", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("relicBonus", new StashFileVariable("relicBonus", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("seed", new StashFileVariable("seed", VariableType.INTEGER, StashBlockType.STASH_ITEM));
        map.put("var1", new StashFileVariable("var1", VariableType.INTEGER, StashBlockType.STASH_ITEM));
        map.put("relicName2", new StashFileVariable("relicName2", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("relicBonus2", new StashFileVariable("relicBonus2", VariableType.STRING, StashBlockType.STASH_ITEM));
        map.put("var2", new StashFileVariable("var2", VariableType.INTEGER, StashBlockType.STASH_ITEM));
        map.put("xOffset", new StashFileVariable("xOffset", VariableType.INTEGER, StashBlockType.STASH_MAIN));
        map.put("yOffset", new StashFileVariable("yOffset", VariableType.INTEGER, StashBlockType.STASH_MAIN));
    }

    private final BlockType location;
    private final String variable;
    private final VariableType type;

    private StashFileVariable(String variable, VariableType type, BlockType location) {
        this.variable = variable;
        this.type = type;
        this.location = location;
    }

    static StashFileVariable valueOf(String variable) {
        return map.get(variable);
    }

    @Override
    public String var() {
        return variable;
    }

    @Override
    public VariableType type() {
        return type;
    }

    @Override
    public BlockType location() {
        return location;
    }
}

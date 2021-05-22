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

package br.com.pinter.tqrespec.save;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.io.Serializable;
import java.util.ArrayList;

public class BlockInfo implements Serializable {
    private int start = -1;
    private int end = -1;
    private int size = -1;
    @JsonIgnore
    private ImmutableListMultimap<String, VariableInfo> variables = ImmutableListMultimap.of();
    @JsonIgnore
    private transient Multimap<String, VariableInfo> stagingVariables = MultimapBuilder.hashKeys().arrayListValues().build();
    @JsonIgnore
    private int parentOffset = -1;
    private BlockType blockType = FileBlockType.UNKNOWN;

    @JsonProperty("blockType")
    public String jsonBlockType() {
        return blockType.toString();
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getParentOffset() {
        return parentOffset;
    }

    public void setParentOffset(int parentOffset) {
        this.parentOffset = parentOffset;
    }

    public ImmutableListMultimap<String, VariableInfo> getVariables() {
        return variables;
    }

    public void setVariables(ImmutableListMultimap<String, VariableInfo> variables) {
        this.variables = variables;
    }

    public ImmutableList<VariableInfo> getVariableByAlias(String alias) {
        ArrayList<VariableInfo> ret = new ArrayList<>();
        for (VariableInfo v : variables.values()) {
            if (v.getAlias().equals(alias)) {
                ret.add(v);
            }
        }
        return ImmutableList.copyOf(ret);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public Multimap<String, VariableInfo> getStagingVariables() {
        if (stagingVariables == null) {
            // stagingVariables content is lost during deepClone
            stagingVariables = MultimapBuilder.hashKeys().arrayListValues().build();
        }
        return stagingVariables;
    }

    @Override
    public String toString() {
        return "BlockInfo{" +
                "start=" + start +
                ", end=" + end +
                ", size=" + size +
                ", variables=" + variables +
                ", stagingVariables=" + stagingVariables +
                ", parentOffset=" + parentOffset +
                ", blockType=" + blockType +
                '}';
    }
}

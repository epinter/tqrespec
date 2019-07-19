/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

import com.google.inject.Singleton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SaveData implements Serializable {
    private Map<Integer, BlockInfo> blockInfo = null;
    private HeaderInfo headerInfo = null;
    private Map<String, ArrayList<Integer>> variableLocation = null;

    public SaveData() {
        variableLocation = new ConcurrentHashMap<>();
        blockInfo = new ConcurrentHashMap<>();
    }

    public Map<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Map<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    public Map<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Map<String, ArrayList<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    public void reset() {
        this.headerInfo = null;
        this.blockInfo = new ConcurrentHashMap<>();
        this.variableLocation = new ConcurrentHashMap<>();
    }
}

/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class PlayerData {
    private static PlayerData instance = null;
    private String playerName = null;
    private Path playerChr = null;
    private ByteBuffer buffer = null;
    private Hashtable<Integer, BlockInfo> blockInfo = null;
    private HeaderInfo headerInfo = null;
    private Hashtable<String, ArrayList<Integer>> variableLocation = null;
    private Hashtable<Integer, byte[]> changes = null;
    private Hashtable<Integer, Integer> valuesLengthIndex = null;
    private Boolean saveInProgress = null;
    private boolean isCustomQuest = false;

    public static PlayerData getInstance() {
        if (instance == null) {
            synchronized (PlayerData.class) {
                if (instance == null)
                    instance = new PlayerData();
            }
        }
        return instance;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public Hashtable<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Hashtable<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    public Hashtable<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Hashtable<String, ArrayList<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    public Hashtable<Integer, byte[]> getChanges() {
        return changes;
    }

    public void setChanges(Hashtable<Integer, byte[]> changes) {
        this.changes = changes;
    }

    public Hashtable<Integer, Integer> getValuesLengthIndex() {
        return valuesLengthIndex;
    }

    public void setValuesLengthIndex(Hashtable<Integer, Integer> valuesLengthIndex) {
        this.valuesLengthIndex = valuesLengthIndex;
    }

    public Path getPlayerChr() {
        return playerChr;
    }

    public void setPlayerChr(Path playerChr) {
        this.playerChr = playerChr;
    }

    public Boolean getSaveInProgress() {
        return saveInProgress;
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress = saveInProgress;
    }

    public boolean isCustomQuest() {
        return isCustomQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        isCustomQuest = customQuest;
    }

    public boolean loadPlayerData(String playerName) throws Exception {
        if (PlayerData.getInstance().getSaveInProgress() != null && PlayerData.getInstance().getSaveInProgress()) {
            return false;
        }
        try {
            new PlayerParser(playerName);
            return true;
        } catch (Exception e) {
            throw e;
        }


    }

    public String getString(String variable) {
        if (variableLocation.get(variable) != null) {
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.String) {
                    return (String) blockInfo.get(block).getVariables().get(variable).getValue();
                }
            }
        }
        return null;
    }

    public void setString(String variable, String value) throws Exception {
        this.setString(variable, value, false);
    }

    public void setString(String variable, String value, boolean utf16le) throws Exception {
        if (variableLocation.get(variable) != null) {
            if (variableLocation.get(variable).size() > 1) {
                throw new Exception("Variable is defined on multiple blocks, aborting");
            }
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.String) {
                    VariableInfo variableInfo = blockInfo.get(block).getVariables().get(variable);
                    byte str[];
                    if (utf16le) {
                        str = value.getBytes(Charset.forName("UTF-16LE"));
                    } else {
                        str = value.getBytes();
                    }
                    byte len[] = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value.length()).array();
                    byte[] data = new byte[4 + str.length];
                    System.arraycopy(len, 0, data, 0, len.length);
                    System.arraycopy(str, 0, data, len.length, str.length);

                    changes.put(variableInfo.getValOffset(), data);
                    this.valuesLengthIndex.put(variableInfo.getValOffset(), 4 + variableInfo.getValSize());
                    System.err.println(Arrays.toString(str) + " --- v" + variableInfo.toString());
                }
            }
        }
    }

    public void setFloat(String variable, float value) throws Exception {
        if (variableLocation.get(variable) != null) {
            if (variableLocation.get(variable).size() > 1) {
                throw new Exception("Variable is defined on multiple blocks, aborting");
            }
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Float) {
                    VariableInfo variableInfo = blockInfo.get(block).getVariables().get(variable);
                    if (variableInfo.getValSize() == 4) {
                        this.getBuffer().putFloat(variableInfo.getValOffset(), value);
                    }
                } else {
                    throw new Exception(String.format("Variable '%s' is not a float", variable));
                }
            }
        }
    }

    public float getFloat(String variable) {
        if (variableLocation.get(variable) != null) {
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Float) {
                    return (Float) blockInfo.get(block).getVariables().get(variable).getValue();
                }

            }
        }
        return -1;
    }

    public Float[] getFloatList(String variable) {
        ArrayList<Float> ret = new ArrayList<>();
        if (variableLocation.get(variable) != null) {
            ArrayList<Integer> blocksList = variableLocation.get(variable);
            for (int block : blocksList) {
                BlockInfo current = blockInfo.get(block);
                if (current.getVariables().get(variable).getVariableType() == VariableInfo.VariableType.Float) {
                    float v = (Float) current.getVariables().get(variable).getValue();
                    ret.add(v);
                }
            }
        }
        return (Float[]) ret.toArray();
    }

    public void setInt(String variable, int value) throws Exception {
        if (variableLocation.get(variable) != null) {
            if (variableLocation.get(variable).size() > 1) {
                throw new Exception("Variable is defined on multiple blocks, aborting");
            }
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Integer) {
                    VariableInfo variableInfo = blockInfo.get(block).getVariables().get(variable);
                    if (variableInfo.getValSize() == 4) {
                        this.getBuffer().putInt(variableInfo.getValOffset(), value);
                    }
                } else {
                    throw new Exception(String.format("Variable '%s' is not an int", variable));
                }
            }
        }
    }

    public Integer getInt(String variable) {
        if (variableLocation.get(variable) != null) {
            int block = variableLocation.get(variable).get(0);
            if (blockInfo.get(block) != null) {
                if (blockInfo.get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Integer) {
                    return (Integer) blockInfo.get(block).getVariables().get(variable).getValue();
                }

            }
        }
        return -1;
    }

    public Integer[] getIntList(String variable) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (variableLocation.get(variable) != null) {
            ArrayList<Integer> blocksList = variableLocation.get(variable);
            for (int block : blocksList) {
                BlockInfo current = blockInfo.get(block);
                if (current.getVariables().get(variable).getVariableType() == VariableInfo.VariableType.Integer) {
                    int v = (Integer) current.getVariables().get(variable).getValue();
                    ret.add(v);
                }
            }
        }
        return (Integer[]) ret.toArray();
    }

    public void reset() {
        this.buffer = null;
        this.headerInfo = null;
        this.blockInfo = null;
        this.playerName = null;
        this.variableLocation = null;
        this.changes = null;
        this.valuesLengthIndex = null;
        this.playerChr = null;
        this.saveInProgress = null;
    }
}
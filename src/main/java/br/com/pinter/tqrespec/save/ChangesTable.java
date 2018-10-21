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

import br.com.pinter.tqrespec.DeepCloneable;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Hashtable;

public class ChangesTable extends Hashtable<Integer, byte[]> implements DeepCloneable {
    private Hashtable<Integer, Integer> valuesLengthIndex = null;

    public ChangesTable() {
        super();
        this.valuesLengthIndex = new Hashtable<Integer, Integer>();
    }

    public Hashtable<Integer, Integer> getValuesLengthIndex() {
        return valuesLengthIndex;
    }

    public void setValuesLengthIndex(Hashtable<Integer, Integer> valuesLengthIndex) {
        this.valuesLengthIndex = valuesLengthIndex;
    }

    public String getString(String variable) {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.String) {
                    return (String) PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getValue();
                }
            }
        }
        return null;
    }

    public void setString(String variable, String value) throws Exception {
        this.setString(variable, value, false);
    }

    public void setString(String variable, String value, boolean utf16le) {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            if (PlayerData.getInstance().getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException("Variable is defined on multiple blocks, aborting");
            }
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.String) {
                    VariableInfo variableInfo = PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable);
                    byte str[];
                    if (utf16le) {
                        //encode string to the format the game uses, a wide character with second byte always 0
                        str = encodeString(value, true);
                    } else {
                        str = encodeString(value, false);
                    }
                    byte len[] = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value.length()).array();
                    byte[] data = new byte[4 + str.length];
                    System.arraycopy(len, 0, data, 0, len.length);
                    System.arraycopy(str, 0, data, len.length, str.length);

                    this.put(variableInfo.getValOffset(), data);
                    this.valuesLengthIndex.put(variableInfo.getValOffset(), 4 + (variableInfo.getValSize() * (utf16le ? 2 : 1)));
                }
            }
        }
    }

    private byte[] encodeString(String str, boolean wide) {
        //allocate the number of characters * 2 so the buffer can hold the '0'
        ByteBuffer buffer = ByteBuffer.allocate(str.length() * (wide ? 2 : 1));

        for (char c : str.toCharArray()) {
            byte n;

            //all characters above 0xFF needs to have accents stripped
            if (c > 0xFF) {
                n = (byte) StringUtils.stripAccents(Character.toString(c)).toCharArray()[0];
            } else {
                n = (byte) c;
            }
            if (wide) {
                buffer.put(new byte[]{n, 0});
            } else {
                buffer.put(new byte[]{n});
            }
        }
        return buffer.array();
    }

    public void setFloat(String variable, float value) throws Exception {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            if (PlayerData.getInstance().getVariableLocation().get(variable).size() > 1) {
                throw new Exception("Variable is defined on multiple blocks, aborting");
            }
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Float) {
                    VariableInfo variableInfo = PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable);
                    if (variableInfo.getValSize() == 4) {
                        byte data[] = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new Exception(String.format("Variable '%s' is not a float", variable));
                }
            }
        }
    }

    public float getFloat(String variable) {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Float) {
                    return (Float) PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getValue();
                }

            }
        }
        return -1;
    }

    public Float[] getFloatList(String variable) {
        ArrayList<Float> ret = new ArrayList<>();
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            ArrayList<Integer> blocksList = PlayerData.getInstance().getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = PlayerData.getInstance().getBlockInfo().get(block);
                if (current.getVariables().get(variable).getVariableType() == VariableInfo.VariableType.Float) {
                    float v = (Float) current.getVariables().get(variable).getValue();
                    ret.add(v);
                }
            }
        }
        return (Float[]) ret.toArray();
    }

    public void setInt(String variable, int value) throws Exception {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            if (PlayerData.getInstance().getVariableLocation().get(variable).size() > 1) {
                throw new Exception("Variable is defined on multiple blocks, aborting");
            }
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Integer) {
                    VariableInfo variableInfo = PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable);
                    if (variableInfo.getValSize() == 4) {
                        byte data[] = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new Exception(String.format("Variable '%s' is not an int", variable));
                }
            }
        }
    }

    public Integer getInt(String variable) {
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            int block = PlayerData.getInstance().getVariableLocation().get(variable).get(0);
            if (PlayerData.getInstance().getBlockInfo().get(block) != null) {
                if (PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getVariableType()
                        == VariableInfo.VariableType.Integer) {
                    return (Integer) PlayerData.getInstance().getBlockInfo().get(block).getVariables().get(variable).getValue();
                }

            }
        }
        return -1;
    }

    public Integer[] getIntList(String variable) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (PlayerData.getInstance().getVariableLocation().get(variable) != null) {
            ArrayList<Integer> blocksList = PlayerData.getInstance().getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = PlayerData.getInstance().getBlockInfo().get(block);
                if (current.getVariables().get(variable).getVariableType() == VariableInfo.VariableType.Integer) {
                    int v = (Integer) current.getVariables().get(variable).getValue();
                    ret.add(v);
                }
            }
        }
        return (Integer[]) ret.toArray();
    }


}

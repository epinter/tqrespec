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

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ChangesTable extends ConcurrentHashMap<Integer, byte[]> implements DeepCloneable {
    private Map<Integer, BlockInfo> blockInfo = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> variableLocation = new ConcurrentHashMap<>();

    private static final String ALERT_INVALIDDATA = "alert.changesinvaliddata";
    private static final String MULTIPLE_DEFINITIONS_ERROR = "Variable is defined on multiple locations, aborting";
    private static final String INVALID_DATA_TYPE = "Variable '%s' has an unexpected data type";
    private Map<Integer, Integer> valuesLengthIndex = new ConcurrentHashMap<>();

    public ChangesTable() {
        super();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChangesTable that = (ChangesTable) o;
        return Objects.equals(valuesLengthIndex, that.valuesLengthIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valuesLengthIndex);
    }

    public Map<Integer, Integer> getValuesLengthIndex() {
        return valuesLengthIndex;
    }

    public Map<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Map<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public Map<String, List<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Map<String, List<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    String getString(String variable) {
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null
                    && (getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING
                    || getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING_UTF_16_LE)) {
                return (String) getBlockInfo().get(block).getVariables().get(variable).get(0).getValue();
            }
        }
        return null;
    }

    void setString(String variable, String value) {
        this.setString(variable, value, false);
    }

    public void setString(String variable, String value, boolean utf16le) {
        if (getVariableLocation().get(variable) != null) {
            if (getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null
                    && (getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING
                    || getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING_UTF_16_LE)) {
                VariableInfo variableInfo = getBlockInfo().get(block).getVariables().get(variable).get(0);
                byte[] str;
                if (utf16le) {
                    //encode string to the format the game uses, a wide character with second byte always 0
                    str = encodeString(value, true);
                } else {
                    str = encodeString(value, false);
                }
                byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value.length()).array();
                byte[] data = new byte[4 + str.length];
                System.arraycopy(len, 0, data, 0, len.length);
                System.arraycopy(str, 0, data, len.length, str.length);

                this.put(variableInfo.getValOffset(), data);
                this.valuesLengthIndex.put(variableInfo.getValOffset(), 4 + (variableInfo.getValSize() * (utf16le ? 2 : 1)));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
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

    void setFloat(String variable, float value) {
        if (getVariableLocation().get(variable) != null) {
            if (getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                if (getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                        == VariableType.FLOAT) {
                    VariableInfo variableInfo = getBlockInfo().get(block).getVariables().get(variable).get(0);
                    if (variableInfo.getValSize() == 4) {
                        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
                }
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public void setFloat(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.getVariableType() == VariableType.FLOAT && variable.getValSize() == 4) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
                this.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    float getFloat(String variable) {
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null
                    && getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.FLOAT) {
                VariableInfo v = getBlockInfo().get(block).getVariables().get(variable).get(0);
                if (this.get(v.getValOffset()) != null) {
                    return ByteBuffer.wrap(this.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                } else {
                    return (Float) v.getValue();
                }
            }
        }
        return -1;
    }

    Float[] getFloatList(String variable) {
        ArrayList<Float> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            List<Integer> blocksList = getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = getBlockInfo().get(block);
                if (current.getVariables().get(variable).get(0).getVariableType() == VariableType.FLOAT) {
                    float v = (Float) current.getVariables().get(variable).get(0).getValue();
                    ret.add(v);
                }
            }
        }
        return ret.toArray(new Float[0]);
    }

    public void setInt(int blockStart, String variable, int value) {
        if (getBlockInfo().get(blockStart) != null) {
            if (getBlockInfo().get(blockStart).getVariables().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }

            if (getBlockInfo().get(blockStart).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.INTEGER) {
                VariableInfo variableInfo = getBlockInfo().get(blockStart).getVariables().get(variable).get(0);
                if (variableInfo.getValSize() == 4) {
                    byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                    this.put(variableInfo.getValOffset(), data);
                    this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                }
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public void setInt(String variable, int value) {
        if (getVariableLocation().get(variable) != null) {
            if (getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                if (getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                        == VariableType.INTEGER) {
                    VariableInfo variableInfo = getBlockInfo().get(block).getVariables().get(variable).get(0);
                    if (variableInfo.getValSize() == 4) {
                        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
                }
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public void setInt(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.getVariableType() == VariableType.INTEGER && variable.getValSize() == 4) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                this.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public Integer getInt(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.INTEGER && this.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(this.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return (Integer) variable.getValue();
        }
    }

    public Float getFloat(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.FLOAT && this.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(this.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return (Float) variable.getValue();
        }
    }

    public Integer getInt(int blockStart, String variable) {
        if (getBlockInfo().get(blockStart) != null
                && getBlockInfo().get(blockStart).getVariables().get(variable).get(0).getVariableType()
                == VariableType.INTEGER) {
            VariableInfo v = getBlockInfo().get(blockStart).getVariables().get(variable).get(0);
            if (this.get(v.getValOffset()) != null) {
                return ByteBuffer.wrap(this.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                return (Integer) v.getValue();
            }
        }
        return -1;
    }

    public Integer getInt(String variable) {
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null
                    && getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.INTEGER) {
                VariableInfo v = getBlockInfo().get(block).getVariables().get(variable).get(0);
                if (this.get(v.getValOffset()) != null) {
                    return ByteBuffer.wrap(this.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
                } else {
                    return (Integer) v.getValue();
                }
            }
        }
        return -1;
    }

    Integer[] getIntList(String variable) {
        ArrayList<Integer> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            List<Integer> blocksList = getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = getBlockInfo().get(block);
                if (current.getVariables().get(variable).get(0).getVariableType() == VariableType.INTEGER) {
                    int v = (Integer) current.getVariables().get(variable).get(0).getValue();
                    ret.add(v);
                }
            }
        }
        return ret.toArray(new Integer[0]);
    }

    public void removeBlock(int offset) {
        BlockInfo current = getBlockInfo().get(offset);
        //we shouldnt leave var changes in the list, the block will disappear
        // and nothing should be changed
        for (VariableInfo v : current.getVariables().values()) {
            if (this.get(v.getValOffset()) != null) {
                this.remove(v.getValOffset());
            }
        }
        this.put(current.getStart(), new byte[0]);
        this.valuesLengthIndex.put(current.getStart(), current.getSize());
    }

    void removeVariable(VariableInfo variable) {
        put(variable.getKeyOffset(), new byte[0]);
        valuesLengthIndex.put(variable.getKeyOffset(), variable.getVariableBytesLength());
    }

    void insertVariable(int offset, VariableInfo variable) {
        ByteBuffer v = ByteBuffer.wrap(new byte[variable.getVariableBytesLength()]).order(ByteOrder.LITTLE_ENDIAN);

        v.putInt(variable.getName().length());
        v.put(variable.getName().getBytes());
        v.put((byte[]) variable.getValue());
        this.put(offset, v.array());
        valuesLengthIndex.put(offset, 0);
    }
}

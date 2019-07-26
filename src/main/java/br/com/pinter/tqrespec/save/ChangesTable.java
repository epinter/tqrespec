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

import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
class ChangesTable extends ConcurrentHashMap<Integer, byte[]> implements DeepCloneable {
    @Inject
    private SaveData saveData;

    private static final String ALERT_INVALIDDATA = "alert.changesinvaliddata";
    private static final String MULTIPLE_DEFINITIONS_ERROR = "Variable is defined on multiple locations, aborting";
    private Map<Integer, Integer> valuesLengthIndex = new ConcurrentHashMap<>();

    ChangesTable() {
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

    Map<Integer, Integer> getValuesLengthIndex() {
        return valuesLengthIndex;
    }

    String getString(String variable) {
        if (saveData.getVariableLocation().get(variable) != null) {
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null
                    && saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING) {
                return (String) saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getValue();
            }
        }
        return null;
    }

    void setString(String variable, String value) {
        this.setString(variable, value, false);
    }

    void setString(String variable, String value, boolean utf16le) {
        if (saveData.getVariableLocation().get(variable) != null) {
            if (saveData.getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null
                    && saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.STRING) {
                VariableInfo variableInfo = saveData.getBlockInfo().get(block).getVariables().get(variable).get(0);
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
        if (saveData.getVariableLocation().get(variable) != null) {
            if (saveData.getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null) {
                if (saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                        == VariableType.FLOAT) {
                    VariableInfo variableInfo = saveData.getBlockInfo().get(block).getVariables().get(variable).get(0);
                    if (variableInfo.getValSize() == 4) {
                        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new NumberFormatException(String.format("Variable '%s' is not a float", variable));
                }
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    float getFloat(String variable) {
        if (saveData.getVariableLocation().get(variable) != null) {
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null
                    && saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.FLOAT) {
                VariableInfo v = saveData.getBlockInfo().get(block).getVariables().get(variable).get(0);
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
        if (saveData.getVariableLocation().get(variable) != null) {
            ArrayList<Integer> blocksList = saveData.getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = saveData.getBlockInfo().get(block);
                if (current.getVariables().get(variable).get(0).getVariableType() == VariableType.FLOAT) {
                    float v = (Float) current.getVariables().get(variable).get(0).getValue();
                    ret.add(v);
                }
            }
        }
        return ret.toArray(new Float[0]);
    }

    void setInt(int blockStart, String variable, int value) {
        if (saveData.getBlockInfo().get(blockStart) != null) {
            if (saveData.getBlockInfo().get(blockStart).getVariables().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }

            if (saveData.getBlockInfo().get(blockStart).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.INTEGER) {
                VariableInfo variableInfo = saveData.getBlockInfo().get(blockStart).getVariables().get(variable).get(0);
                if (variableInfo.getValSize() == 4) {
                    byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                    this.put(variableInfo.getValOffset(), data);
                    this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                }
            } else {
                throw new NumberFormatException(String.format("Variable '%s' is not an int", variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    void setInt(String variable, int value) {
        if (saveData.getVariableLocation().get(variable) != null) {
            if (saveData.getVariableLocation().get(variable).size() > 1) {
                throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
            }
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null) {
                if (saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                        == VariableType.INTEGER) {
                    VariableInfo variableInfo = saveData.getBlockInfo().get(block).getVariables().get(variable).get(0);
                    if (variableInfo.getValSize() == 4) {
                        byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                        this.put(variableInfo.getValOffset(), data);
                        this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
                    }
                } else {
                    throw new NumberFormatException(String.format("Variable '%s' is not an int", variable));
                }
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    void setInt(VariableInfo variable, int value) {
        if (saveData.getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.getVariableType() == VariableType.INTEGER && variable.getValSize() == 4) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                this.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format("Variable '%s' is not an int", variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    Integer getInt(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.INTEGER && this.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(this.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return (Integer) variable.getValue();
        }
    }

    Float getFloat(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.FLOAT && this.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(this.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return (Float) variable.getValue();
        }
    }

    Integer getInt(int blockStart, String variable) {
        if (saveData.getBlockInfo().get(blockStart) != null
                && saveData.getBlockInfo().get(blockStart).getVariables().get(variable).get(0).getVariableType()
                == VariableType.INTEGER) {
            VariableInfo v = saveData.getBlockInfo().get(blockStart).getVariables().get(variable).get(0);
            if (this.get(v.getValOffset()) != null) {
                return ByteBuffer.wrap(this.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                return (Integer) v.getValue();
            }
        }
        return -1;
    }

    Integer getInt(String variable) {
        if (saveData.getVariableLocation().get(variable) != null) {
            int block = saveData.getVariableLocation().get(variable).get(0);
            if (saveData.getBlockInfo().get(block) != null
                    && saveData.getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.INTEGER) {
                VariableInfo v = saveData.getBlockInfo().get(block).getVariables().get(variable).get(0);
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
        if (saveData.getVariableLocation().get(variable) != null) {
            ArrayList<Integer> blocksList = saveData.getVariableLocation().get(variable);
            for (int block : blocksList) {
                BlockInfo current = saveData.getBlockInfo().get(block);
                if (current.getVariables().get(variable).get(0).getVariableType() == VariableType.INTEGER) {
                    int v = (Integer) current.getVariables().get(variable).get(0).getValue();
                    ret.add(v);
                }
            }
        }
        return ret.toArray(new Integer[0]);
    }

    void removeBlock(int offset) {
        BlockInfo current = saveData.getBlockInfo().get(offset);
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

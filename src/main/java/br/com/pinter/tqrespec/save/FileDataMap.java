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

import br.com.pinter.tqrespec.util.Util;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class FileDataMap implements DeepCloneable {
    private Map<Integer, BlockInfo> blockInfo = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> variableLocation = new ConcurrentHashMap<>();
    private final Map<Integer, byte[]> changes = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> valuesLengthIndex = new ConcurrentHashMap<>();
    private Platform platform = Platform.WINDOWS;

    private static final String ALERT_INVALIDDATA = "alert.changesinvaliddata";
    private static final String MULTIPLE_DEFINITIONS_ERROR = "Variable is defined on multiple locations, aborting";
    private static final String INVALID_DATA_TYPE = "Variable '%s' has an unexpected data type";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileDataMap that = (FileDataMap) o;
        return Objects.equals(valuesLengthIndex, that.valuesLengthIndex) && Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), changes, valuesLengthIndex);
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

    public byte[] getBytes(Integer offset) {
        return changes.get(offset);
    }

    public Set<Integer> changesKeySet() {
        return changes.keySet();
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public void clear() {
        blockInfo.clear();
        changes.clear();
        valuesLengthIndex.clear();
        variableLocation.clear();
    }

    public String getString(String variable) {
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

    public void convertTo(Platform target, String saveId) {
        Platform currentPlatform = platform;
        platform = target;
        if(currentPlatform.equals(target)) {
            throw new IllegalStateException("can't convert to same platform");
        }

        if(currentPlatform.equals(Platform.WINDOWS) && target.equals(Platform.MOBILE)) {
            BlockInfo myPlayerNameBlock = this.blockInfo.get(variableLocation.get("myPlayerName").get(0));
            int myPlayerNameKeyOffset = myPlayerNameBlock.getVariables().get("myPlayerName").get(0).getKeyOffset();
            VariableInfo variableInfo = VariableInfo.builder().name("mySaveId")
                    .blockOffset(myPlayerNameBlock.getStart())
                    .keyOffset(myPlayerNameKeyOffset)
                    .variableType(VariableType.STRING)
                    .value(saveId).build();

            insertVariable(myPlayerNameKeyOffset ,variableInfo);
            for(BlockInfo b: getBlockInfo().values()) {
                for (VariableInfo v : b.getVariables().values()) {
                    if(v.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                        String oldValue;
                        if(changes.get(v.getValOffset()) != null) {
                            oldValue = readStringFromMap(v.getValOffset(), Platform.WINDOWS,true);
                        } else {
                            oldValue = (String) v.getValue();
                        }
                        byte[] newValue = encodeString(oldValue,true);
                        byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(oldValue.length()).array();
                        byte[] data = new byte[4 + newValue.length];
                        System.arraycopy(len, 0, data, 0, len.length);
                        System.arraycopy(newValue, 0, data, len.length, newValue.length);

                        changes.put(v.getValOffset(), data);
                        valuesLengthIndex.put(v.getValOffset(), 4 + (v.getValSize() * 2));
                    }
                }
            }
        } else if(currentPlatform.equals(Platform.MOBILE) && target.equals(Platform.WINDOWS)) {
            for (BlockInfo b : getBlockInfo().values()) {
                for (VariableInfo v : b.getVariables().values()) {
                    if (v.getName().equals("mySaveId")) {
                        removeVariable(v);
                    } else {
                        if(v.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                            String oldValue;
                            if(changes.get(v.getValOffset()) != null) {
                                oldValue = readStringFromMap(v.getValOffset(), Platform.MOBILE,true);
                            } else {
                                oldValue = new String(((String) v.getValue()).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_16LE);
                            }
                            byte[] newValue = encodeString(oldValue,true);
                            byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(oldValue.length()).array();
                            byte[] data = new byte[4 + newValue.length];
                            System.arraycopy(len, 0, data, 0, len.length);
                            System.arraycopy(newValue, 0, data, len.length, newValue.length);

                            changes.put(v.getValOffset(), data);
                            valuesLengthIndex.put(v.getValOffset(), 4 + (v.getValSize() * 4));
                        }
                    }
                }
            }
        }
    }

    public String getCharacterName() {
        if(platform.equals(Platform.MOBILE)) {
            return new String((getString("myPlayerName")).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_16LE);
        } else {
            return getString("myPlayerName");
        }
    }

    private String readStringFromMap(int offset, Platform fromPlatform, boolean wide) {
        byte[] data = Arrays.copyOfRange(changes.get(offset), 4, changes.get(offset).length-1);

        if(wide && fromPlatform.equals(Platform.MOBILE)) {
            return decodeString(data, wide, 4);
        } else if(wide && fromPlatform.equals(Platform.WINDOWS)) {
            return decodeString(data, wide, 2);
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public List<String> getStringValuesFromBlock(String variable) {
        List<String> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for(VariableInfo vi: getBlockInfo().get(block).getVariables().values()) {
                    if(vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if(vi.getVariableType().equals(VariableType.STRING) || vi.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                        ret.add(vi.getValueString());
                    }
                }
            }
        }
        return ret;
    }

    public void setString(String variable, String value) {
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

                changes.put(variableInfo.getValOffset(), data);
                this.valuesLengthIndex.put(variableInfo.getValOffset(), 4 + (variableInfo.getValSize() * getUtfSize(utf16le)));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    private int getUtfSize(boolean utf16le) {
        int utfSize = 1;
        if(utf16le && platform.equals(Platform.WINDOWS)) {
            utfSize = 2;
        } else if(utf16le && platform.equals(Platform.MOBILE)) {
            utfSize = 4;
        }
        return utfSize;
    }

    private String decodeString(byte[] str, boolean wide, int bytesPerChar) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < str.length; i=i+bytesPerChar) {
            ret.append((char) str[i]);
        }
        return ret.toString();
    }

    private byte[] encodeString(String str, boolean wide) {
        //allocate the number of characters * 2 so the buffer can hold the '0'
        ByteBuffer buffer = ByteBuffer.allocate(str.length() * getUtfSize(wide));

        for (char c : str.toCharArray()) {
            byte n;

            //all characters above 0xFF needs to have accents stripped
            if (c > 0xFF) {
                n = (byte) StringUtils.stripAccents(Character.toString(c)).toCharArray()[0];
            } else {
                n = (byte) c;
            }
            if (wide) {
                if(platform.equals(Platform.WINDOWS)) {
                    buffer.put(new byte[]{n, 0});
                } else if(platform.equals(Platform.MOBILE)) {
                    buffer.put(new byte[]{n, 0, 0, 0});
                }
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
                        changes.put(variableInfo.getValOffset(), data);
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
                changes.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public float getFloat(String variable) {
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null
                    && getBlockInfo().get(block).getVariables().get(variable).get(0).getVariableType()
                    == VariableType.FLOAT) {
                VariableInfo v = getBlockInfo().get(block).getVariables().get(variable).get(0);
                if (changes.get(v.getValOffset()) != null) {
                    return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
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
                    changes.put(variableInfo.getValOffset(), data);
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
                        changes.put(variableInfo.getValOffset(), data);
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

    public void incrementInt(VariableInfo variable) {
        int value = (int) variable.getValue();
        if(changes.get(variable.getValOffset())!=null) {
            byte[] currentData = changes.get(variable.getValOffset());
            int currentValue = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(currentData).rewind().getInt();
            value = currentValue + 1;
        } else {
            value++;
        }
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.getVariableType() == VariableType.INTEGER && variable.getValSize() == 4) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                changes.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public void setInt(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.getVariableType() == VariableType.INTEGER && variable.getValSize() == 4) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                changes.put(variable.getValOffset(), data);
                this.valuesLengthIndex.put(variable.getValOffset(), variable.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public Integer getInt(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.INTEGER && changes.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(changes.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return (Integer) variable.getValue();
        }
    }

    public Float getFloat(VariableInfo variable) {
        if (variable.getVariableType() == VariableType.FLOAT && changes.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(changes.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return (Float) variable.getValue();
        }
    }

    public Integer getInt(int blockStart, String variable) {
        if (getBlockInfo().get(blockStart) != null
                && getBlockInfo().get(blockStart).getVariables().get(variable).get(0).getVariableType()
                == VariableType.INTEGER) {
            VariableInfo v = getBlockInfo().get(blockStart).getVariables().get(variable).get(0);
            if (changes.get(v.getValOffset()) != null) {
                return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
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
                if (changes.get(v.getValOffset()) != null) {
                    return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
                } else {
                    return (Integer) v.getValue();
                }
            }
        }
        return -1;
    }

    public List<Integer> getIntValuesFromBlock(String variable) {
        List<Integer> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for(VariableInfo vi: getBlockInfo().get(block).getVariables().values()) {
                    if(vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if(vi.getVariableType().equals(VariableType.INTEGER)) {
                        ret.add((Integer) vi.getValue());
                    }
                }
            }
        }
        return ret;
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

    public List<UID> getUIDValuesFromBlock(String variable) {
        List<UID> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for(VariableInfo vi: getBlockInfo().get(block).getVariables().values()) {
                    if(vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if(vi.getVariableType().equals(VariableType.UID)) {
                        ret.add(new UID((byte[]) vi.getValue()));
                    }
                }
            }
        }
        return ret;
    }

    public void removeBlock(int offset) {
        BlockInfo current = getBlockInfo().get(offset);
        //we shouldnt leave var changes in the list, the block will disappear
        // and nothing should be changed
        for (VariableInfo v : current.getVariables().values()) {
            if (changes.get(v.getValOffset()) != null) {
                changes.remove(v.getValOffset());
            }
        }
        changes.put(current.getStart(), new byte[0]);
        this.valuesLengthIndex.put(current.getStart(), current.getSize());
    }

    void removeVariable(VariableInfo variable) {
        changes.put(variable.getKeyOffset(), new byte[0]);
        valuesLengthIndex.put(variable.getKeyOffset(), variable.getVariableBytesLength());
    }

    public void insertVariable(int offset, VariableInfo variable) {
        insertVariable(offset, variable,false);
    }

    public void insertVariable(int offset, VariableInfo variable, boolean overwrite) {
        int bufSize = variable.getVariableBytesLength();

        if(changes.get(offset) != null && !overwrite) {
            bufSize += changes.get(offset).length;
        }

        ByteBuffer v = ByteBuffer.allocate(bufSize).order(ByteOrder.LITTLE_ENDIAN);

        if(changes.get(offset) != null && !overwrite) {
            v.put(changes.get(offset));
        }

        v.putInt(variable.getName().length());
        v.put(variable.getName().getBytes());
        if(variable.getVariableType().equals(VariableType.UID)) {
            v.put((byte[]) variable.getValue());
        } else if(variable.getVariableType().equals(VariableType.STRING)) {
            String value = (String) variable.getValue();
            v.putInt(value.length());
            v.put(value.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalStateException("not implemented");
        }

        changes.put(offset, v.array());
        valuesLengthIndex.put(offset, 0);

        BlockInfo block = this.blockInfo.get(variable.getBlockOffset());

        block.getStagingVariables().put(variable.getName(),variable);
    }

    public List<VariableInfo> getTempVariableInfo(String var) {
        List<Integer> temp = variableLocation.get("temp");
        for (Integer blockStart : temp) {
            BlockInfo b = blockInfo.get(blockStart);
            if (!b.getVariableByAlias(var).isEmpty()) {
                return b.getVariableByAlias(var);
            }
        }
        return List.of();
    }

    public int getTempAttr(String attr) {
        int ret = -1;
        List<VariableInfo> varList = getTempVariableInfo(attr);
        if (varList.size() == 1 && varList.get(0) != null) {
            VariableInfo attrVar = varList.get(0);
            if (attrVar.getVariableType() == VariableType.FLOAT) {
                ret = Math.round(getFloat(attrVar));
            } else if (attrVar.getVariableType() == VariableType.INTEGER) {
                ret = getInt(attrVar);
            }
        }
        if (ret < 0) {
            throw new IllegalArgumentException(String.format("attribute not found %s", attr));
        }
        return ret;
    }

    public void setTempAttr(String attr, Integer val) {
        List<VariableInfo> varList = getTempVariableInfo(attr);
        if (!varList.isEmpty() && varList.get(0) != null) {
            VariableInfo attrVar = varList.get(0);
            if (attrVar.getVariableType() == VariableType.FLOAT) {
                setFloat(attrVar, val);
            } else if (attrVar.getVariableType() == VariableType.INTEGER) {
                setInt(attrVar, val);
            }
        } else {
            throw new IllegalArgumentException(String.format("attribute not found %s", attr));
        }
    }
}

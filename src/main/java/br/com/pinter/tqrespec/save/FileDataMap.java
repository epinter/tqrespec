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

import br.com.pinter.tqrespec.save.player.PlayerFileVariable;
import br.com.pinter.tqrespec.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private int searchFirstVariable(String variable) {
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                return block;
            }
        }
        return -1;
    }

    private void assertMultipleDefinitions(int block, String variable) {
        if (getBlockInfo().get(block).getVariables().get(variable).size() > 1) {
            throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
        }
    }

    private void assertMultipleDefinitions(String variable) {
        if (getVariableLocation().get(variable).size() > 1) {
            throw new IllegalStateException(MULTIPLE_DEFINITIONS_ERROR);
        }
    }

    private VariableInfo getFirst(int block, String variable) {
        if (block >= 0) {
            return getBlockInfo().get(block).getVariables().get(variable).get(0);
        }
        return null;
    }

    private VariableInfo getFirst(String variable) {
        int block = searchFirstVariable(variable);
        if (block >= 0) {
            return getBlockInfo().get(block).getVariables().get(variable).get(0);
        }
        return null;
    }

    public String getString(String variable) {
        VariableInfo v = getFirst(variable);
        if (v!=null && v.isString()) {
            return v.getValueString();
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

            insertVariable(myPlayerNameKeyOffset, variableInfo);
            for(BlockInfo b: getBlockInfo().values()) {
                for (VariableInfo v : b.getVariables().values()) {
                    if(v.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                        if(v.getValSize() == 0)
                            continue;

                        String oldValue;
                        if(changes.get(v.getValOffset()) != null) {
                            oldValue = readStringFromMap(v.getValOffset(), Platform.WINDOWS, true);
                        } else {
                            oldValue = v.getValueString();
                        }
                        byte[] newValue = encodeString(oldValue, VariableType.STRING_UTF_32_LE);
                        byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(oldValue.length()).array();
                        byte[] data = new byte[4 + newValue.length];
                        System.arraycopy(len, 0, data, 0, len.length);
                        System.arraycopy(newValue, 0, data, len.length, newValue.length);

                        changes.put(v.getValOffset(), data);
                        valuesLengthIndex.put(v.getValOffset(), 4 + v.getValBytesLength());
                    }
                }
            }
        } else if(currentPlatform.equals(Platform.MOBILE) && target.equals(Platform.WINDOWS)) {
            for (BlockInfo b : getBlockInfo().values()) {
                for (VariableInfo v : b.getVariables().values()) {
                    if (v.getName().equals("mySaveId")) {
                        removeVariable(v);
                    } else {
                        if(v.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
                            if(v.getValSize() == 0)
                                continue;

                            String oldValue;
                            if(changes.get(v.getValOffset()) != null) {
                                oldValue = readStringFromMap(v.getValOffset(), Platform.MOBILE, true);
                            } else {
                                oldValue = v.getValueString();
                            }
                            byte[] newValue = encodeString(oldValue, VariableType.STRING_UTF_16_LE);
                            byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(oldValue.length()).array();
                            byte[] data = new byte[4 + newValue.length];
                            System.arraycopy(len, 0, data, 0, len.length);
                            System.arraycopy(newValue, 0, data, len.length, newValue.length);

                            changes.put(v.getValOffset(), data);
                            valuesLengthIndex.put(v.getValOffset(), 4 + v.getValBytesLength());
                        }
                    }
                }
            }
        }
    }

    public String getCharacterName() {
            return getString("myPlayerName");
    }

    private String readStringFromMap(int offset, Platform fromPlatform, boolean wide) {
        byte[] data = Arrays.copyOfRange(changes.get(offset), 4, changes.get(offset).length);

        if(wide && fromPlatform.equals(Platform.MOBILE)) {
            return new String(new String(data, Charset.forName("UTF-32LE")).getBytes());
        } else if(wide && fromPlatform.equals(Platform.WINDOWS)) {
            return new String(new String(data, StandardCharsets.UTF_16LE).getBytes());
        }
        return new String(new String(data, StandardCharsets.UTF_8).getBytes());
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
                    if(vi.getVariableType().equals(VariableType.STRING) || vi.getVariableType().equals(VariableType.STRING_UTF_16_LE) || vi.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
                        ret.add(vi.getValueString());
                    }
                }
            }
        }
        return ret;
    }

    public void setString(String variable, String value) {
        assertMultipleDefinitions(variable);

        VariableInfo variableInfo = getFirst(variable);
        if(variableInfo != null && variableInfo.isString()) {
            byte[] str = encodeString(value, variableInfo.getVariableType());
            byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value.length()).array();
            byte[] data = new byte[4 + str.length];
            System.arraycopy(len, 0, data, 0, len.length);
            System.arraycopy(str, 0, data, len.length, str.length);

            changes.put(variableInfo.getValOffset(), data);
            valuesLengthIndex.put(variableInfo.getValOffset(), 4 + variableInfo.getValBytesLength());
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    private byte[] encodeString(String str, VariableType stringType) {
        //allocate the number of characters * 2 so the buffer can hold the '0'
        ByteBuffer buffer = ByteBuffer.allocate(str.length() * stringType.dataTypeSize());

        for (char o : str.toCharArray()) {
            char c = StringUtils.stripAccents(Character.toString(o)).toCharArray()[0];

            if (! stringType.equals(VariableType.STRING)) {
                byte n1 = (byte) (c & 0xFF);
                byte n2 = (byte) (c >> 8);

                if(stringType.equals(VariableType.STRING_UTF_16_LE)) {
                    buffer.put(new byte[]{n1, n2});
                } else if(stringType.equals(VariableType.STRING_UTF_32_LE)) {
                    buffer.put(new byte[]{n1, n2, 0, 0});
                }
            } else {
                buffer.put(new byte[]{(byte) c});
            }
        }
        return buffer.array();
    }

    public void setFloat(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.isFloat()) {
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
        VariableInfo v = getFirst(variable);
        if(v != null && v.isFloat()) {
            if (changes.get(v.getValOffset()) != null) {
                return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            } else {
                return (Float) v.getValue();
            }
        }

        return -1;
    }

    public void setInt(int blockStart, String variable, int value) {
        if (getBlockInfo().get(blockStart) != null) {
            assertMultipleDefinitions(blockStart, variable);

            VariableInfo variableInfo = getFirst(blockStart, variable);

            if(variableInfo == null) {
                throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
            }

            if(variableInfo.isInt()) {
                byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
                changes.put(variableInfo.getValOffset(), data);
                this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }

        }
    }

    public void setInt(String variable, int value) {
        assertMultipleDefinitions(variable);
        VariableInfo variableInfo = getFirst(variable);

        if(variableInfo == null) {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }

        if(variableInfo.isInt()) {
            byte[] data = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
            changes.put(variableInfo.getValOffset(), data);
            this.valuesLengthIndex.put(variableInfo.getValOffset(), variableInfo.getValSize());
        } else {
            throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
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
            if (variable.isInt()) {
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
        if (variable.isInt() && changes.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(changes.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
        } else {
            return (Integer) variable.getValue();
        }
    }

    public Float getFloat(VariableInfo variable) {
        if (variable.isFloat() && changes.get(variable.getValOffset()) != null) {
            return ByteBuffer.wrap(changes.get(variable.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else {
            return (Float) variable.getValue();
        }
    }

    public Integer getInt(int blockStart, String variable) {
        if (getBlockInfo().get(blockStart) != null) {
            VariableInfo v = getFirst(blockStart, variable);
            if(v != null && v.isInt()) {
                if (changes.get(v.getValOffset()) != null) {
                    return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
                } else {
                    return (Integer) v.getValue();
                }
            }
        }

        return -1;
    }

    public Integer getInt(String variable) {
        VariableInfo v = getFirst(variable);
        if(v != null && v.isInt()) {
            if (changes.get(v.getValOffset()) != null) {
                return ByteBuffer.wrap(changes.get(v.getValOffset())).order(ByteOrder.LITTLE_ENDIAN).getInt();
            } else {
                return (Integer) v.getValue();
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

    @SuppressWarnings("unused")
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
        insertVariable(offset, variable, false);
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

        block.getStagingVariables().put(variable.getName(), variable);
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

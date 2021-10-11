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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileDataMap implements DeepCloneable {
    private static final String ALERT_INVALIDDATA = "alert.changesinvaliddata";
    private static final String MULTIPLE_DEFINITIONS_ERROR = "Variable is defined on multiple locations, aborting";
    private static final String INVALID_DATA_TYPE = "Variable '%s' has an unexpected data type";
    private final Map<Integer, DataChange> changes = new ConcurrentHashMap<>();
    private Map<Integer, BlockInfo> blockInfo = new ConcurrentHashMap<>();
    private Map<String, List<Integer>> variableLocation = new ConcurrentHashMap<>();
    private Platform platform = Platform.WINDOWS;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FileDataMap that = (FileDataMap) o;
        return Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), changes);
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

    byte[] getBytes(Integer offset) {
        if (changes.get(offset) != null) {
            return changes.get(offset).data();
        }
        return new byte[0];
    }

    byte[] getBytes(VariableInfo variable) {
        if (changes.get(variable.getValOffset()) != null) {
            return changes.get(variable.getValOffset()).data();
        }
        return new byte[0];
    }

    int getPreviousValueLength(int offset) {
        if (changes.get(offset) != null) {
            return changes.get(offset).previousValueLength();
        }
        throw new IllegalArgumentException("invalid offset");
    }

    Set<Integer> changesKeySet() {
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

    private void storeChange(VariableInfo oldVar, VariableInfo newVar) {
        storeChange(oldVar, newVar, 0);
    }

    /**
     * Store changes in memory, this will be used by PlayerWriter
     *
     * @param oldVar   old variable
     * @param newVar   new data
     * @param position where to place the data if already exists a change in memory
     *                 -1 = before
     *                 0 = replace
     *                 1 = after
     */
    private void storeChange(VariableInfo oldVar, VariableInfo newVar, int position) {
        int offset;
        boolean creating = false;

        if (oldVar == null) {
            // used to insert new variable
            oldVar = VariableInfo.builder().variableType(VariableType.UNKNOWN)
                    .name(newVar.getName())
                    .blockOffset(newVar.getBlockOffset())
                    .keyOffset(newVar.getKeyOffset())
                    .valOffset(-1)
                    .valSize(0) //must be zero for previousValueLength calculation
                    .build();
            offset = newVar.getKeyOffset();
            creating = true;
        } else {
            offset = oldVar.getValOffset();
        }


        if (changes.get(offset) == null) {
            DataChangeVariable changeVariable = new DataChangeVariable(oldVar, newVar);
            if (creating) {
                changeVariable.getAddVars().add(newVar.getName());
            }
            changes.put(offset, changeVariable);
        } else {
            if (changes.get(offset).isVariable()) {
                DataChangeVariable current = (DataChangeVariable) changes.get(offset);
                if (creating) {
                    current.getAddVars().add(newVar.getName());
                }

                if (position == -1) {
                    current.getVariables().add(0, newVar);
                } else if (position == 0) {
                    current.getVariables().clear();
                    current.getVariables().add(newVar);
                } else if (position == 1) {
                    current.getVariables().add(newVar);
                }
            } else {
                DataChangeVariable changeVariable = new DataChangeVariable(oldVar, newVar);
                if (creating) {
                    changeVariable.getAddVars().add(newVar.getName());
                }

                changeVariable.insertPadding(changes.get(offset).data(), position != -1);
                changes.put(offset, changeVariable);
            }
        }

    }

    private void storeChange(int offset, byte[] newData, int previousLength) {
        storeChange(offset, newData, previousLength, -1);
    }

    /**
     * Store changes in memory, this will be used by PlayerWriter
     *
     * @param offset   offset of the data, can be a valOffset, keyOffset or blockStartOffset
     * @param newData  new data
     * @param position where to place the data if already exists a change in memory
     *                 -1 = before
     *                 0 = replace
     *                 1 = after
     */
    private void storeChange(int offset, byte[] newData, int previousLength, int position) {
        if (changes.get(offset) != null && newData.length == 0) {
            int previous = changes.get(offset).previousValueLength();
            changes.put(offset, new DataChangeRaw(offset, newData, previous));
            return;
        }

        if (changes.get(offset) == null) {
            changes.put(offset, new DataChangeRaw(offset, newData, previousLength));
        } else {
            changes.get(offset).insertPadding(newData, position != -1);
        }
    }

    public boolean isRemoved(int offset) {
        return changes.get(offset) != null && changes.get(offset).isEmpty();
    }

    private boolean hasChange(VariableInfo variable) {
        return changes.get(variable.getValOffset()) != null;
    }

    private VariableInfo getFirstChange(VariableInfo variable) {
        DataChangeVariable dataChange = (DataChangeVariable) changes.get(variable.getValOffset());
        return dataChange.getVariable(variable);
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
        if (v != null && v.isString()) {
            return v.getValueString();
        }
        return null;
    }

    private void convertWindowsToMobile(String saveId) {
        BlockInfo myPlayerNameBlock = this.blockInfo.get(variableLocation.get("myPlayerName").get(0));
        int myPlayerNameKeyOffset = myPlayerNameBlock.getVariables().get("myPlayerName").get(0).getKeyOffset();
        VariableInfo variableInfo = VariableInfo.builder().name("mySaveId")
                .blockOffset(myPlayerNameBlock.getStart())
                .keyOffset(myPlayerNameKeyOffset)
                .variableType(VariableType.STRING)
                .value(saveId).build();

        insertVariable(variableInfo);
        for (VariableInfo v :
            getBlockInfo().values().stream().flatMap(b -> b.getVariables().values().stream()).collect(Collectors.toList())) {
            if (v.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                if (v.getValSize() == 0)
                    continue;

                VariableInfo newVar = (VariableInfo) v.deepClone();
                if (hasChange(v)) {
                    newVar = getFirstChange(v);
                }
                newVar.setVariableType(VariableType.STRING_UTF_32_LE);
                storeChange(v, newVar);
            }
        }
    }

    private void convertMobileToWindows() {
        for (VariableInfo v :
                getBlockInfo().values().stream().flatMap(b -> b.getVariables().values().stream()).collect(Collectors.toList())) {
            if (v.getName().equals("mySaveId") || v.getName().equals("currentDifficulty")) {
                removeVariable(v);
            } else if(v.getName().equals("headerVersion")) {
                VariableInfo newVar = (VariableInfo) v.deepClone();
                if (hasChange(v)) {
                    newVar = getFirstChange(v);
                }
                newVar.setValue(3);
                storeChange(v, newVar);
            } else {
                if (v.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
                    if (v.getValSize() == 0)
                        continue;

                    VariableInfo newVar = (VariableInfo) v.deepClone();
                    if (hasChange(v)) {
                        newVar = getFirstChange(v);
                    }
                    newVar.setVariableType(VariableType.STRING_UTF_16_LE);
                    storeChange(v, newVar);
                }
            }
        }
    }

    public void convertTo(Platform target, String saveId) {
        Platform currentPlatform = platform;
        platform = target;
        if (currentPlatform.equals(target)) {
            throw new IllegalStateException("can't convert to same platform");
        }

        if (currentPlatform.equals(Platform.WINDOWS) && target.equals(Platform.MOBILE)) {
            convertWindowsToMobile(saveId);
        } else if (currentPlatform.equals(Platform.MOBILE) && target.equals(Platform.WINDOWS)) {
            convertMobileToWindows();
        }
    }

    public String getCharacterName() {
        return getString("myPlayerName");
    }

    public List<String> getStringValuesFromBlock(String variable) {
        List<String> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for (VariableInfo vi : getBlockInfo().get(block).getVariables().values()) {
                    if (vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if (vi.getVariableType().equals(VariableType.STRING) || vi.getVariableType().equals(VariableType.STRING_UTF_16_LE) || vi.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
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
        if (variableInfo != null && variableInfo.isString()) {
            VariableInfo newVar = (VariableInfo) variableInfo.deepClone();
            newVar.setValue(value);
            storeChange(variableInfo, newVar);
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    private void setFloat(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.isFloat()) {
                VariableInfo newVar = (VariableInfo) variable.deepClone();
                newVar.setValue((float) value);
                storeChange(variable, newVar);
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    public Float getFloat(String variable) {
        VariableInfo v = getFirst(variable);
        if (v != null && v.isFloat()) {
            if (hasChange(v)) {
                VariableInfo c = getFirstChange(v);
                return (Float) c.getValue();
            } else {
                return (Float) v.getValue();
            }
        }

        throw new IllegalStateException("invalid variable: " + variable);
    }

    public void setInt(int blockStart, String variable, int value) {
        if (getBlockInfo().get(blockStart) != null) {
            assertMultipleDefinitions(blockStart, variable);

            VariableInfo variableInfo = getFirst(blockStart, variable);

            if (variableInfo == null) {
                throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
            }

            if (variableInfo.isInt()) {
                VariableInfo newVar = (VariableInfo) variableInfo.deepClone();
                newVar.setValue(value);
                storeChange(variableInfo, newVar);
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }

        }
    }

    public void setInt(String variable, int value) {
        assertMultipleDefinitions(variable);
        VariableInfo variableInfo = getFirst(variable);

        if (variableInfo == null) {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }

        if (variableInfo.isInt()) {
            VariableInfo newVar = (VariableInfo) variableInfo.deepClone();
            newVar.setValue(value);
            storeChange(variableInfo, newVar);
        } else {
            throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
        }
    }

    public void incrementInt(VariableInfo variable) {
        int value = (int) variable.getValue();
        if (hasChange(variable)) {
            byte[] currentData = getBytes(variable);
            int currentValue = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(currentData).rewind().getInt();
            value = currentValue + 1;
        } else {
            value++;
        }

        setInt(variable, value);
    }

    public void decrementInt(VariableInfo variable) {
        int value = (int) variable.getValue();
        if (hasChange(variable)) {
            byte[] currentData = getBytes(variable);
            int currentValue = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).put(currentData).rewind().getInt();
            value = currentValue - 1;
        } else {
            value++;
        }

        setInt(variable, value);
    }

    void setInt(VariableInfo variable, int value) {
        if (getBlockInfo().get(variable.getBlockOffset()) != null) {
            if (variable.isInt()) {
                VariableInfo newVar = (VariableInfo) variable.deepClone();
                newVar.setValue(value);
                storeChange(variable, newVar);
            } else {
                throw new NumberFormatException(String.format(INVALID_DATA_TYPE, variable));
            }
        } else {
            throw new IllegalArgumentException(Util.getUIMessage(ALERT_INVALIDDATA, variable));
        }
    }

    Integer getInt(VariableInfo variable) {
        if (variable != null && variable.isInt()) {
            if (hasChange(variable)) {
                VariableInfo c = getFirstChange(variable);
                return (Integer) c.getValue();
            } else {
                return (Integer) variable.getValue();
            }
        }

        throw new IllegalStateException("invalid variable: " + variable);
    }

    Float getFloat(VariableInfo variable) {
        if (variable != null && variable.isFloat()) {
            if (hasChange(variable)) {
                VariableInfo c = getFirstChange(variable);
                return (Float) c.getValue();
            } else {
                return (Float) variable.getValue();
            }
        }

        throw new IllegalStateException("invalid variable: " + variable);
    }

    public Integer getInt(int blockStart, String variable) {
        if (getBlockInfo().get(blockStart) != null) {
            VariableInfo v = getFirst(blockStart, variable);
            if (v != null && v.isInt()) {
                if (hasChange(v)) {
                    VariableInfo c = getFirstChange(v);
                    return (Integer) c.getValue();
                } else {
                    return (Integer) v.getValue();
                }
            }
        }

        throw new IllegalStateException(String.format("invalid variable='%s'; blockStart='%s'", variable, blockStart));
    }

    public Integer getInt(String variable) {
        VariableInfo v = getFirst(variable);
        if (v != null && v.isInt()) {
            if (hasChange(v)) {
                VariableInfo c = getFirstChange(v);
                return (Integer) c.getValue();
            } else {
                return (Integer) v.getValue();
            }
        }

        throw new IllegalStateException("invalid variable: " + variable);
    }

    public List<Integer> getIntValuesFromBlock(String variable) {
        List<Integer> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for (VariableInfo vi : getBlockInfo().get(block).getVariables().values()) {
                    if (vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if (vi.getVariableType().equals(VariableType.INTEGER)) {
                        ret.add((Integer) vi.getValue());
                    }
                }
            }
        }
        return ret;
    }

    @SuppressWarnings("unused")
    List<UID> getUIDValuesFromBlock(String variable) {
        List<UID> ret = new ArrayList<>();
        if (getVariableLocation().get(variable) != null) {
            int block = getVariableLocation().get(variable).get(0);
            if (getBlockInfo().get(block) != null) {
                for (VariableInfo vi : getBlockInfo().get(block).getVariables().values()) {
                    if (vi.getValue() == null || !vi.getName().equals(variable)) {
                        continue;
                    }
                    if (vi.getVariableType().equals(VariableType.UID)) {
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
            if (hasChange(v)) {
                changes.remove(v.getValOffset());
            }
        }
        storeChange(current.getStart(), new byte[0], current.getSize());
    }

    public void removeVariable(VariableInfo variable) {
        removeVariable(variable.getValOffset(), variable);
    }

    public void removeVariable(int offset, VariableInfo variable) {
        if (changes.get(offset) != null && changes.get(offset).isVariable()) {
            DataChangeVariable dataChange = (DataChangeVariable) changes.get(offset);
            List<VariableInfo> toRemove = new ArrayList<>();
            for (VariableInfo v : dataChange.getVariables()) {
                if (v.isUid() && v.getName().equals(variable.getName()) && v.getValue().equals(variable.getValue())) {
                    toRemove.add(v);
                }
            }

            BlockInfo block = getBlockInfo().get(variable.getBlockOffset());
            block.getStagingVariables().remove(variable.getName(), variable);

            for (VariableInfo v : toRemove) {
                dataChange.getVariables().remove(v);
            }

            if (dataChange.getVariables().isEmpty()) {
                storeChange(variable.getKeyOffset(), new byte[0], variable.getVariableBytesLength());
            }
        } else {
            storeChange(variable.getKeyOffset(), new byte[0], variable.getVariableBytesLength());
        }
    }

    public void insertVariable(VariableInfo variable) {
        insertVariable(variable, false);
    }

    void insertVariable(VariableInfo variable, boolean overwrite) {
        storeChange(null, variable, overwrite ? 0 : -1);
        BlockInfo block = getBlockInfo().get(variable.getBlockOffset());
        block.getStagingVariables().put(variable.getName(), variable);
    }

    List<VariableInfo> getTempVariableInfo(String var) {
        List<Integer> temp = variableLocation.get("temp") != null ? variableLocation.get("temp") : List.of();

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

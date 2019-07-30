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

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@SuppressWarnings("unused")
public class VariableInfo implements Serializable {
    private String name = null;
    private String alias = null;
    private int keyOffset = -1;
    private int valOffset = -1;
    private int valSize = -1;
    private String valueString = null;
    private Integer valueInteger = null;
    private Float valueFloat = null;
    private byte[] valueByteArray = null;
    private VariableType variableType;
    private int blockOffset = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        if (StringUtils.isNotBlank(alias)) {
            return alias;
        }
        return name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getKeyOffset() {
        return keyOffset;
    }

    public void setKeyOffset(int keyOffset) {
        this.keyOffset = keyOffset;
    }

    public int getValOffset() {
        return valOffset;
    }

    public void setValOffset(int valOffset) {
        this.valOffset = valOffset;
    }

    /**
     * Returns the value size. Strings in UTF8 and UTF16 encodings will return the same size.
     */
    public int getValSize() {
        return valSize;
    }

    public void setValSize(int valSize) {
        this.valSize = valSize;
    }

    public Object getValue() {
        if (variableType == VariableType.INTEGER)
            return valueInteger;
        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE)
            return valueString;
        if (variableType == VariableType.FLOAT)
            return valueFloat;
        if (variableType == VariableType.UID || variableType == VariableType.STREAM)
            return valueByteArray;
        return null;
    }

    /**
     * Returns value as a string. Bytes are converted to hex-string.
     */
    public String getValueString() {
        if (variableType == VariableType.INTEGER)
            return String.valueOf(valueInteger);
        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE)
            return valueString;
        if (variableType == VariableType.FLOAT)
            return String.valueOf(valueFloat);
        if (variableType == VariableType.UID || variableType == VariableType.STREAM)
            return BaseEncoding.base16().encode(valueByteArray);
        return null;
    }

    /**
     * Returns value length in bytes
     */
    public int getValBytesLength() {
        int sz = valSize;
        if (variableType == VariableType.STRING_UTF_16_LE) {
            sz *= 2;
        }
        return sz;
    }

    /**
     * Returns total variable length in bytes. Are considered first bytes specifying variable name length,
     * variable name, first bytes specifying value length if present, value (double if utf16)
     */
    public int getVariableBytesLength() {
        if (valOffset == -1) {
            return name.length() + 4;
        }

        int valSizePrefix = 0;
        int sz = valSize;

        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE
                || variableType == VariableType.STREAM) {
            valSizePrefix = 4;
        }

        if (variableType == VariableType.STRING_UTF_16_LE) {
            sz *= 2;
        }

        return valOffset - keyOffset + sz + valSizePrefix;
    }

    public void setValue(String value) {
        this.valueString = value;
    }

    public void setValue(int value) {
        this.valueInteger = value;
    }

    public void setValue(float value) {
        this.valueFloat = value;
    }

    public void setValue(byte[] value) {
        this.valueByteArray = value;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    public int getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(int blockOffset) {
        this.blockOffset = blockOffset;
    }

    @Override
    public String toString() {
        return String.format("name={%s}; alias={%s}; value={%s}; keyOffset={%d}, valOffset={%d}; valSize={%d}; variableType: {%s}", this.name, alias, this.getValue(), this.keyOffset, this.valOffset, this.valSize, variableType);
    }
}

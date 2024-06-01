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

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@SuppressWarnings("unused")
public class VariableInfo implements DeepCloneable, Serializable {
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
    private static final String INVALID_VALUE_TYPE_MSG = "invalid value type";

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableInfo that = (VariableInfo) o;
        return valSize == that.valSize && blockOffset == that.blockOffset && name.equals(that.name) && Objects.equals(alias, that.alias) && Objects.equals(valueString, that.valueString) && Objects.equals(valueInteger, that.valueInteger) && Objects.equals(valueFloat, that.valueFloat) && Arrays.equals(valueByteArray, that.valueByteArray) && variableType == that.variableType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, alias, valSize, valueString, valueInteger, valueFloat, variableType, blockOffset);
        result = 31 * result + Arrays.hashCode(valueByteArray);
        return result;
    }

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

    public String getValuePlatformString() {
        if (variableType == VariableType.STRING)
            return valueString;
        if (variableType == VariableType.STRING_UTF_16_LE)
            return new String(valueString.getBytes(StandardCharsets.UTF_16LE));
        if (variableType == VariableType.STRING_UTF_32_LE)
            return new String(valueString.getBytes(Charset.forName("UTF-32LE")));
        return null;
    }

    public Object getValue() {
        if (variableType == VariableType.INTEGER)
            return valueInteger;
        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE || variableType == VariableType.STRING_UTF_32_LE)
            return valueString;
        if (variableType == VariableType.FLOAT)
            return valueFloat;
        if (variableType == VariableType.UID || variableType == VariableType.STREAM)
            return valueByteArray;
        return null;
    }

    public void setValue(String value) {
        if (!isString()) {
            throw new IllegalArgumentException(INVALID_VALUE_TYPE_MSG);
        }
        this.valueString = value;
        valSize = valueString.length();
    }

    public void setValue(int value) {
        if (!isInt()) {
            throw new IllegalArgumentException(INVALID_VALUE_TYPE_MSG);
        }
        this.valueInteger = value;
        valSize = variableType.dataTypeSize();
    }

    public void setValue(float value) {
        if (!isFloat()) {
            throw new IllegalArgumentException(INVALID_VALUE_TYPE_MSG);
        }
        this.valueFloat = value;
        valSize = variableType.dataTypeSize();
    }

    public void setValue(byte[] value) {
        if (!isUid() && !isStream()) {
            throw new IllegalArgumentException(INVALID_VALUE_TYPE_MSG);
        }
        this.valueByteArray = value;
        valSize = valueByteArray.length;
    }

    /**
     * Returns value as a string. Bytes are converted to hex-string.
     */
    public String getValueString() {
        if (variableType == VariableType.INTEGER)
            return String.valueOf(valueInteger);
        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE || variableType == VariableType.STRING_UTF_32_LE)
            return valueString;
        if (variableType == VariableType.FLOAT)
            return String.valueOf(valueFloat);
        if (variableType == VariableType.UID)
            return UID.convertUidByteToString(valueByteArray);
        if (variableType == VariableType.STREAM)
            return BaseEncoding.base16().encode(valueByteArray);
        return null;
    }

    /**
     * Returns value length in bytes
     */
    public int getValBytesLength() {
        int sz = valSize;
        if (isString()) {
            sz *= variableType.dataTypeSize();
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

        return valOffset - keyOffset + getValBytesLength() + getValuePrefix();
    }

    int getValuePrefix() {
        int valSizePrefix = 0;

        if (variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE || variableType == VariableType.STRING_UTF_32_LE
                || variableType == VariableType.STREAM) {
            valSizePrefix = 4;
        }

        return valSizePrefix;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        if (this.valSize == -1) {
            if (variableType.equals(VariableType.FLOAT) || variableType.equals(VariableType.INTEGER) || variableType.equals(VariableType.UID)) {
                valSize = variableType.dataTypeSize();
            } else if (variableType.equals(VariableType.STREAM) && valueByteArray != null) {
                valSize = valueByteArray.length;
            } else if (isString() && valueString != null) {
                valSize = valueString.length();
            }
        }

        this.variableType = variableType;
    }

    public int getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(int blockOffset) {
        this.blockOffset = blockOffset;
    }

    public boolean isString() {
        return variableType == VariableType.STRING || variableType == VariableType.STRING_UTF_16_LE || variableType == VariableType.STRING_UTF_32_LE;
    }

    public boolean isFloat() {
        return variableType == VariableType.FLOAT;
    }

    public boolean isInt() {
        return variableType == VariableType.INTEGER;
    }

    public boolean isUid() {
        return variableType == VariableType.UID;
    }

    public boolean isStream() {
        return variableType == VariableType.STREAM;
    }

    @Override
    public String toString() {
        return String.format("name={%s}; alias={%s}; value={%s}; keyOffset={%d}, valOffset={%d}; valSize={%d}; variableType: {%s}", this.name, alias, this.getValue(), this.keyOffset, this.valOffset, this.valSize, variableType);
    }

    private byte[] encodeString() {
        //allocate the number of characters * 2 so the buffer can hold the '0'
        ByteBuffer buffer = ByteBuffer.allocate(valueString.length() * variableType.dataTypeSize());

        for (char o : valueString.toCharArray()) {
            char c = StringUtils.stripAccents(Character.toString(o)).toCharArray()[0];

            if (!variableType.equals(VariableType.STRING)) {
                byte n1 = (byte) (c & 0xFF);
                byte n2 = (byte) (c >> 8);

                if (variableType.equals(VariableType.STRING_UTF_16_LE)) {
                    buffer.put(new byte[]{n1, n2});
                } else if (variableType.equals(VariableType.STRING_UTF_32_LE)) {
                    buffer.put(new byte[]{n1, n2, 0, 0});
                }
            } else {
                buffer.put(new byte[]{(byte) c});
            }
        }
        return buffer.array();
    }

    public byte[] bytes() {
        if (variableType.equals(VariableType.INTEGER)) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(valueInteger).array();
        } else if (variableType.equals(VariableType.FLOAT)) {
            return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(valueFloat).array();
        } else if (variableType.equals(VariableType.STRING) || variableType.equals(VariableType.STRING_UTF_16_LE) || variableType.equals(VariableType.STRING_UTF_32_LE)) {
            byte[] str = encodeString();
            byte[] len = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(valueString.length()).array();
            byte[] data = new byte[4 + str.length];
            System.arraycopy(len, 0, data, 0, len.length);
            System.arraycopy(str, 0, data, len.length, str.length);
            return data;
        } else if (variableType.equals(VariableType.UID) || variableType.equals(VariableType.STREAM)) {
            return valueByteArray;
        }
        return new byte[0];
    }

    public static class Builder {
        private String builderName = null;
        private String builderAlias = null;
        private int builderKeyOffset = -1;
        private int builderValOffset = -1;
        private int builderValSize = -1;
        private String builderValueString = null;
        private Integer builderValueInteger = null;
        private Float builderValueFloat = null;
        private byte[] builderValueByteArray = null;
        private VariableType builderVariableType;
        private int builderBlockOffset = -1;

        public Builder name(String builderName) {
            this.builderName = builderName;
            return this;
        }

        public Builder alias(String builderAlias) {
            this.builderAlias = builderAlias;
            return this;
        }

        public Builder keyOffset(int builderKeyOffset) {
            this.builderKeyOffset = builderKeyOffset;
            return this;
        }

        public Builder valOffset(int builderValOffset) {
            this.builderValOffset = builderValOffset;
            return this;
        }

        public Builder valSize(int builderValSize) {
            this.builderValSize = builderValSize;
            return this;
        }

        public Builder value(String value) {
            this.builderValueString = value;
            return this;
        }

        public Builder value(int value) {
            this.builderValueInteger = value;
            return this;
        }

        public Builder value(float value) {
            this.builderValueFloat = value;
            return this;
        }

        public Builder value(byte[] value) {
            this.builderValueByteArray = value;
            return this;
        }

        public Builder variableType(VariableType builderVariableType) {
            this.builderVariableType = builderVariableType;
            return this;
        }

        public Builder blockOffset(int builderBlockOffset) {
            this.builderBlockOffset = builderBlockOffset;
            return this;
        }


        public VariableInfo build() {
            VariableInfo v = new VariableInfo();
            v.name = builderName;
            v.blockOffset = builderBlockOffset;
            v.alias = builderAlias;
            v.keyOffset = builderKeyOffset;
            v.valOffset = builderValOffset;
            v.valSize = builderValSize;
            v.valueString = builderValueString;
            v.valueInteger = builderValueInteger;
            v.valueFloat = builderValueFloat;
            v.valueByteArray = builderValueByteArray;
            v.variableType = builderVariableType;

            if (v.valSize == -1) {
                if (v.variableType.equals(VariableType.FLOAT) || v.variableType.equals(VariableType.INTEGER) || v.variableType.equals(VariableType.UID)) {
                    v.valSize = v.variableType.dataTypeSize();
                } else if (v.variableType.equals(VariableType.STREAM) && v.valueByteArray != null) {
                    v.valSize = v.valueByteArray.length;
                } else if (v.isString() && v.valueString != null) {
                    v.valSize = v.valueString.length();
                }
            }

            if (v.valOffset == -1) {
                v.valOffset = builderKeyOffset + 4 + v.name.length();
            }
            return v;
        }
    }
}

/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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

public class VariableInfo {
    private String name = null;
    private int keyOffset = -1;
    private int valOffset = -1;
    private int valSize = -1;
    private String valueString = null;
    private Integer valueInteger = null;
    private Float valueFloat = null;

    public enum VariableType {
        Unknown,
        String,
        Integer,
        Float,
        UID
    }

    private VariableType variableType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getValSize() {
        return valSize;
    }

    public void setValSize(int valSize) {
        this.valSize = valSize;
    }

    public Object getValue() {
        if (variableType == VariableType.Integer)
            return valueInteger;
        if (variableType == VariableType.String)
            return valueString;
        if (variableType == VariableType.Float)
            return valueFloat;
        return null;
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

    public VariableType getVariableType() {
        return variableType;
    }

    public void setVariableType(VariableType variableType) {
        this.variableType = variableType;
    }

    @Override
    public String toString() {
        return String.format("name={%s}; value={%s}; keyOffset={%d}, valOffset={%d}; valSize={%d}; variableType: {%s}", this.name, this.getValue(), this.keyOffset, this.valOffset, this.valSize, variableType);
    }
}

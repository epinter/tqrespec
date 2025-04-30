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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class DataChangeVariable extends DataChange implements Serializable {
    private VariableInfo oldVariable;
    private final List<VariableInfo> variables = new ArrayList<>();
    private final List<String> addVars = new ArrayList<>();
    private boolean remove = false;

    public DataChangeVariable(VariableInfo oldVariable, VariableInfo variable) {
        this.oldVariable = oldVariable;
        if(variable == null) {
            this.remove = true;
        } else {
            this.variables.add(variable);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataChangeVariable that = (DataChangeVariable) o;
        return Objects.equals(oldVariable, that.oldVariable) && variables.equals(that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldVariable, variables);
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    public byte[] data() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (!isPaddingAfter())
                bos.write(getPadding());

            for (VariableInfo v : variables) {
                if (v.getValOffset() != oldVariable.getValOffset()) {
                    throw new IllegalArgumentException("invalid offset " + v);
                }
                if (addVars.contains(v.getName())) {
                    ByteBuffer varKey = ByteBuffer.allocate(v.getName().getBytes().length + 4).order(ByteOrder.LITTLE_ENDIAN);

                    varKey.putInt(v.getName().length());
                    varKey.put(v.getName().getBytes());
                    bos.write(varKey.array());
                }
                bos.write(v.bytes());
            }

            if (isPaddingAfter())
                bos.write(getPadding());

            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error writing to buffer");
        }
    }

    public List<String> getAddVars() {
        return addVars;
    }

    @Override
    public int previousValueLength() {
        if(remove) {
            return oldVariable.getVariableBytesLength();
        }else {
            return oldVariable.getValuePrefix() + oldVariable.getValBytesLength();
        }
    }

    @Override
    public int offset() {
        return oldVariable.getValOffset();
    }

    public VariableInfo getVariable(VariableInfo variable) {
        for (VariableInfo v : variables) {
            if (v.getName().equals(variable.getName()) && v.getBlockOffset() == variable.getBlockOffset()
                    && v.getVariableType().equals(variable.getVariableType())) {
                return v;
            }
        }

        return null;
    }

    public int getSize() {
        return variables.size();
    }

    public List<VariableInfo> getVariables() {
        return variables;
    }

    public VariableInfo getOldVariable() {
        return oldVariable;
    }

    public void setOldVariable(VariableInfo oldVariable) {
        this.oldVariable = oldVariable;
    }

    @Override
    public boolean isEmpty() {
        return variables.isEmpty() && getPadding().length == 0;
    }

    public void clear() {
        addVars.clear();
        variables.clear();
        setPadding(new byte[0]);
        setPaddingAfter(true);
    }

    @Override
    public boolean isRemove() {
        return remove;
    }

    @Override
    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    @Override
    public String toString() {
        return "DataChangeVariable{" +
                "oldVariable=" + oldVariable +
                ", variables=" + variables +
                ", addVars=" + addVars +
                "} " + super.toString();
    }
}

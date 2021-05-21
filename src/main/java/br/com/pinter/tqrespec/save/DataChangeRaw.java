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
import java.util.Arrays;
import java.util.Objects;

class DataChangeRaw extends DataChange implements Serializable {
    private final byte[] data;
    private final int previouslength;
    private final int offset;

    public DataChangeRaw(int offset, byte[] data, int previousLength) {
        this.offset = offset;
        this.data = data;
        this.previouslength = previousLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataChangeRaw changeRaw = (DataChangeRaw) o;
        return previouslength == changeRaw.previouslength && offset == changeRaw.offset && Arrays.equals(data, changeRaw.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(previouslength, offset);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return data.length == 0 && getPadding().length == 0;
    }

    @Override
    public byte[] data() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (!isPaddingAfter())
                bos.write(getPadding());

            bos.write(data);

            if (isPaddingAfter())
                bos.write(getPadding());

            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error writing to buffer");
        }
    }

    @Override
    public int previousValueLength() {
        return previouslength;
    }

    @Override
    public int offset() {
        return offset;
    }
}

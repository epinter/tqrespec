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

abstract class DataChange implements Serializable {
    private byte[] padding = new byte[0];
    private boolean paddingAfter = true;

    public boolean isRaw() {
        return !isVariable();
    }

    public abstract boolean isVariable();

    public abstract byte[] data();

    public abstract int previousValueLength();

    public abstract int offset();

    public void insertPadding(byte[] data) {
        insertPadding(data, true);
    }

    public abstract boolean isEmpty();

    public byte[] getPadding() {
        return padding;
    }

    public void setPadding(byte[] padding) {
        this.padding = padding;
    }

    public boolean isPaddingAfter() {
        return paddingAfter;
    }

    public void setPaddingAfter(boolean paddingAfter) {
        this.paddingAfter = paddingAfter;
    }

    public void insertPadding(byte[] data, boolean before) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (before) {
                bos.write(data);
                bos.write(padding);
            } else {
                bos.write(padding);
                bos.write(data);
            }
            padding = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error writing to buffer");
        }
    }
}

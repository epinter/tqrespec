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

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;

public class UID {
    private String value;

    public UID(String value) {
        this.value = value;
    }

    public UID(byte[] value) {
        this.value = convertUidByteToString(value);
    }

    public static String convertUidByteToString(byte[] uid) {
        ByteBuffer uidP1 = ByteBuffer.wrap(Arrays.copyOfRange(uid, 0, 4)).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP2 = ByteBuffer.wrap(Arrays.copyOfRange(uid, 4, 8)).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP3 = ByteBuffer.wrap(Arrays.copyOfRange(uid, 8, 12)).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP4 = ByteBuffer.wrap(Arrays.copyOfRange(uid, 12, 16)).order(ByteOrder.LITTLE_ENDIAN);

        int p4 = uidP4.getInt();
        int p3 = uidP3.getInt();
        int p2 = uidP2.getInt();
        int p1 = uidP1.getInt();

        String uidStr = String.format("%d-%d-%d-%d",
                p4 & 0xFFFFFFFFL,
                p3 & 0xFFFFFFFFL,
                p2 & 0xFFFFFFFFL,
                p1 & 0xFFFFFFFFL);

        if("0-0-0-0".equals(uidStr) || StringUtils.isBlank(uidStr)) {
            return null;
        }
        return uidStr;
    }

    public static byte[] convertUidStringToByte(String uid) {
        if(StringUtils.isBlank(uid)) {
            return null;
        }

        String[] p = StringUtils.split(uid, "-");
        long p4 = Long.parseLong(p[3]);
        long p3 = Long.parseLong(p[2]);
        long p2 = Long.parseLong(p[1]);
        long p1 = Long.parseLong(p[0]);

        ByteBuffer uidP4 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP3 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP2 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer uidP1 = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

        uidP4.putInt((int) (p4));
        uidP3.putInt((int) p3);
        uidP2.putInt((int) p2);
        uidP1.putInt((int) p1);

        byte[] ret = new byte[16];
        System.arraycopy(uidP4.array(), 0, ret, 0, 4);
        System.arraycopy(uidP3.array(), 0, ret, 4, 4);
        System.arraycopy(uidP2.array(), 0, ret, 8, 4);
        System.arraycopy(uidP1.array(), 0, ret, 12, 4);

        return ret;
    }

    public byte[] getBytes() {
        return convertUidStringToByte(value);
    }

    public String getUid() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UID uid = (UID) o;
        return value.equals(uid.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UID{" +
                "value='" + value + '\'' +
                '}';
    }
}

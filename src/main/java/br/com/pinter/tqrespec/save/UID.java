/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class UID {
    private String value;

    public UID(String value) {
        this.value = value;
    }

    public UID(byte[] value) {
        this.value = convertUidByteToString(value);
    }

    public byte[] getBytes() {
        return convertUidStringToByte(value);
    }

    public String getUid() {
        return value;
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

        return String.format("%d-%d-%d-%d",
                p4 & 0xFFFFFFFFL,
                p3 & 0xFFFFFFFFL,
                p2 & 0xFFFFFFFFL,
                p1 & 0xFFFFFFFFL);
    }

    public static byte[] convertUidStringToByte(String uid) {
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
}

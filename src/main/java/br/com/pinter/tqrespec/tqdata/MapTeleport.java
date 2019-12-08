/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.save.UID;

public abstract class MapTeleport {
    private final int order;
    private final UID uid;
    private final String recordId;

    public MapTeleport(int order, UID uid, String recordId) {
        this.order = order;
        this.uid = uid;
        this.recordId = recordId;
    }

    public int getOrder() {
        return order;
    }

    public UID getUid() {
        return uid;
    }

    public String getRecordId() {
        return recordId;
    }

    @Override
    public String toString() {
        return "MapTeleport{" +
                "order=" + order +
                ", uid=" + uid +
                ", recordId='" + recordId + '\'' +
                '}';
    }
}

/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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

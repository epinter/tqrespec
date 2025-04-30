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

package br.com.pinter.tqrespec.tqdata;

import java.util.Arrays;
import java.util.Optional;

public enum DefaultAct {
    GREECE(1),
    EGYPT(2),
    ORIENT(3),
    HADES(4),
    NORTH(5),
    ATLANTIS(6),
    EAST(7);

    private int value;

    DefaultAct(int value) {
        this.value = value;
    }

    public static int get(DefaultAct v) {
        Optional<DefaultAct> first = Arrays.stream(DefaultAct.values()).filter((f -> f.value == v.getValue())).findFirst();
        if (first.isEmpty()) {
            throw new EnumConstantNotPresentException(DefaultAct.class, String.valueOf(v));
        }
        return first.get().getValue();
    }

    public String getTag() {
        switch (value) {
            case 1 -> {
                return "tagMGreece";
            }
            case 2 -> {
                return "tagMEgypt";
            }
            case 3 -> {
                return "tagMOrient";
            }
            case 4 -> {
                return "xtagMHades";
            }
            case 5 -> {
                return "x2tagMNorth";
            }
            case 6 -> {
                return "x3tagQAct06";
            }
            case 7 -> {
                return "x4tagMChina";
            }
            default -> {
                return "";
            }
        }
    }

    public int getValue() {
        return value;
    }
}

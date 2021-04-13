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

public enum GameVersion {
    UNKNOWN(0),
    TQ(1),
    TQIT(2),
    TQAE(3);

    private final int value;

    GameVersion(int value) {
        this.value = value;
    }

    public static GameVersion fromValue(int version) {
        Optional<GameVersion> first = Arrays.stream(GameVersion.values()).filter(f -> f.value() == version).findFirst();
        if(first.isEmpty()) {
            throw new EnumConstantNotPresentException(GameVersion.class,String.valueOf(version));
        }
        return first.get();
    }

    public int value() {
        return value;
    }
}

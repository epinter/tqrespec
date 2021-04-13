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

public enum InstallType {
    UNKNOWN(0),
    WINDOWS(1),
    STEAM(2),
    GOG(3),
    MICROSOFT_STORE(4),
    LEGACY_DISC(5),
    ALTERNATIVE_STEAM_API(6),
    MANUAL(100)
    ;

    private int value;

    InstallType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static InstallType fromValue(int version) {
        Optional<InstallType> first = Arrays.stream(InstallType.values()).filter(f -> f.value() == version).findFirst();
        if(first.isEmpty()) {
            throw new EnumConstantNotPresentException(InstallType.class,String.valueOf(version));
        }
        return first.get();
    }

}

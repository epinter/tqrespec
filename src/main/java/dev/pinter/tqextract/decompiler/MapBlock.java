/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Extract.

    TQ Extract is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TQ Extract is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TQ Extract.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.pinter.tqextract.decompiler;

import java.util.Arrays;

record MapBlock(int id, int size, int start) {
    public enum Type {
        UNKNOWN(0xFF),
        LEVELS(0x01),
        LEVELDATA(0x02),
        IMAGES(0x1A),
        QUESTS(0x1B),
        INSTANCEDATA(0x11),
        SECTOR(0x18),
        MINIMAP(0x19);


        private final int value;

        Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Type of(int value) {
            return Arrays.stream(values()).filter(f -> f.value == value).findFirst().orElse(UNKNOWN);
        }
    }
}

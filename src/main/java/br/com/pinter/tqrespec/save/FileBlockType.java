/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

public enum FileBlockType {
    //generic types
    UNKNOWN,
    BODY,
    MULTIPLE,
    //player file types
    PLAYER_HEADER,
    PLAYER_MAIN,
    PLAYER_ATTRIBUTES,
    PLAYER_STATS,
    PLAYER_INVENTORY,
    PLAYER_HOT_SLOT,
    PLAYER_SKILLS,
    PLAYER_UI_SKILL,
    PLAYER_LEVEL_POINTS
}

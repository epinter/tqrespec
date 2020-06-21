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

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.FileBlockType;

public class PlayerBlockType extends FileBlockType {
    static final FileBlockType PLAYER_HEADER = new FileBlockType(0, "PLAYER_HEADER");
    static final FileBlockType PLAYER_MAIN = new FileBlockType(1, "PLAYER_MAIN");
    static final FileBlockType PLAYER_ATTRIBUTES = new FileBlockType(2, "PLAYER_ATTRIBUTES");
    static final FileBlockType PLAYER_STATS = new FileBlockType(3, "PLAYER_STATS");
    static final FileBlockType PLAYER_INVENTORY = new FileBlockType(5, "PLAYER_INVENTORY");
    static final FileBlockType PLAYER_ITEM = new FileBlockType(6, "PLAYER_ITEM");
    static final FileBlockType PLAYER_EQUIPMENT = new FileBlockType(7, "PLAYER_EQUIPMENT");
    static final FileBlockType PLAYER_HOT_SLOT = new FileBlockType(8, "PLAYER_HOT_SLOT");
    static final FileBlockType PLAYER_SKILLS = new FileBlockType(9, "PLAYER_SKILLS");
    static final FileBlockType PLAYER_UI_SKILL = new FileBlockType(10, "PLAYER_UI_SKILL");
    static final FileBlockType PLAYER_LEVEL_POINTS = new FileBlockType(11, "PLAYER_LEVEL_POINTS");

    public PlayerBlockType(int value, String name) {
        super(value, name);
    }
}

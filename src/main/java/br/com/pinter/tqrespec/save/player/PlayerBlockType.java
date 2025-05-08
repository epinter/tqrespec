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

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.FileBlockType;

public class PlayerBlockType extends FileBlockType {
    public static final FileBlockType PLAYER_HEADER = new FileBlockType(0, "PLAYER_HEADER");
    public static final FileBlockType PLAYER_MAIN = new FileBlockType(1, "PLAYER_MAIN");
    public static final FileBlockType PLAYER_ATTRIBUTES = new FileBlockType(2, "PLAYER_ATTRIBUTES");
    public static final FileBlockType PLAYER_STATS = new FileBlockType(3, "PLAYER_STATS");
    public static final FileBlockType PLAYER_SKILLSLIST = new FileBlockType(4, "PLAYER_SKILLSLIST");
    public static final FileBlockType PLAYER_INVENTORY = new FileBlockType(5, "PLAYER_INVENTORY");
    public static final FileBlockType PLAYER_EQUIPMENT = new FileBlockType(7, "PLAYER_EQUIPMENT");
    public static final FileBlockType PLAYER_HOT_SLOT = new FileBlockType(8, "PLAYER_HOT_SLOT");
    public static final FileBlockType PLAYER_UI_SKILL = new FileBlockType(10, "PLAYER_UI_SKILL");
    public static final FileBlockType PLAYER_LEVEL_POINTS = new FileBlockType(11, "PLAYER_LEVEL_POINTS");
    public static final FileBlockType PLAYER_SKILL = new FileBlockType(9, "PLAYER_SKILL", PlayerBlockType.PLAYER_SKILLSLIST);
    public static final FileBlockType PLAYER_ITEM = new FileBlockType(6, "PLAYER_ITEM", PlayerBlockType.PLAYER_INVENTORY_ITEMCONTAINER);
    public static final FileBlockType PLAYER_INVENTORY_SACK = new FileBlockType(13, "PLAYER_INVENTORY_SACK", PlayerBlockType.PLAYER_INVENTORY);
    public static final FileBlockType PLAYER_INVENTORY_ITEMCONTAINER = new FileBlockType(14, "PLAYER_INVENTORY_ITEMCONTAINER", PlayerBlockType.PLAYER_INVENTORY_SACK);

    public PlayerBlockType(int value, String name) {
        super(value, name);
    }
}

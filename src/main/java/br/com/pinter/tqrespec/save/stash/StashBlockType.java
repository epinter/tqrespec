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

package br.com.pinter.tqrespec.save.stash;

import br.com.pinter.tqrespec.save.FileBlockType;

class StashBlockType extends FileBlockType {
    static final FileBlockType STASH_MAIN = new FileBlockType(0, "STASH_MAIN");
    static final FileBlockType STASH_ITEM = new FileBlockType(1, "STASH_ITEM");
    static final FileBlockType STASH_ITEM_PREFIX = new FileBlockType(2, "STASH_ITEM_PREFIX");
    static final FileBlockType STASH_ITEM_SUFFIX = new FileBlockType(3, "STASH_ITEM_SUFFIX");

    public StashBlockType(int value, String name) {
        super(value, name);
    }
}

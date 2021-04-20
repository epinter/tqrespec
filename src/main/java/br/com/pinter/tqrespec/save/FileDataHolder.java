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

package br.com.pinter.tqrespec.save;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface FileDataHolder {
    ByteBuffer getBuffer();

    void setBuffer(ByteBuffer buffer);

    FileDataMap getDataMap();

    String getPlayerName();

    void setPlayerName(String playerName);

    Path getPlayerPath();

    void setPlayerPath(Path playerPath);
}

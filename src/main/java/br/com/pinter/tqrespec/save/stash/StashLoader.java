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

import br.com.pinter.tqrespec.util.Constants;

import java.nio.file.Files;
import java.nio.file.Path;

public class StashLoader {
    private StashData stashData;

    public StashData getSaveData() {
        return stashData;
    }

    public boolean loadStash(Path playerPath, String playerName) {
        if(!Files.exists(playerPath.resolve(Constants.STASH_FILE))) {
            return false;
        }
        StashParser stashParser = new StashParser(playerPath.toString());
        stashData = new StashData();
        stashData.setPlayerPath(playerPath);
        stashData.setBuffer(stashParser.load());
        stashData.setPlayerName(playerName);
        stashData.setCustomQuest(false);
        stashData.getDataMap().setBlockInfo(stashParser.getBlockInfo());
        stashData.getDataMap().setVariableLocation(stashParser.getVariableLocation());
        return true;
    }
}

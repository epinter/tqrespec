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

import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.FileDataHolder;
import br.com.pinter.tqrespec.save.FileWriter;
import br.com.pinter.tqrespec.util.Constants;

import java.io.IOException;
import java.nio.file.Paths;

public class StashWriter extends FileWriter {
    private static final System.Logger logger = Log.getLogger(StashWriter.class.getName());

    private final StashData saveData;
    private final int crcOffset;
    private final boolean createCrc;

    public StashWriter(StashData saveData) {
        this.saveData = saveData;
        createCrc = true;
        crcOffset = 0;
    }

    public boolean save() {
        try {
            String fName = String.format("%s/winsys.dxb", saveData.getPlayerPath());
            getSaveData().getDataMap().setString("fName", fName);
            writeBuffer(saveData.getPlayerPath().toString(), Constants.STASH_FILE);
            getSaveData().getDataMap().setString("fName", fName.replaceAll("\\.dxb$", ".dxg"));
            writeBuffer(saveData.getPlayerPath().toString(), Constants.STASH_FILE_BACKUP);
            return true;
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return false;
    }

    public int getCrcOffset() {
        return crcOffset;
    }

    public boolean isCreateCrc() {
        return createCrc;
    }

    @Override
    protected FileDataHolder getSaveData() {
        return saveData;
    }

}

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
import br.com.pinter.tqrespec.save.*;
import br.com.pinter.tqrespec.util.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.System.Logger.Level.DEBUG;

public class StashParser extends FileParser {
    private static final System.Logger logger = Log.getLogger(StashParser.class);
    private final String playerPath;

    public StashParser(String playerPath) {
        this.playerPath = playerPath;
    }

    private String getStashFileName() {
        return Paths.get(playerPath, Constants.STASH_FILE).toString();
    }

    @Override
    protected boolean readFile() throws IOException {
        if (!Files.exists(Paths.get(getStashFileName()))) {
            return false;
        }
        try (FileInputStream chr = new FileInputStream(getStashFileName())) {
            try (FileChannel in = chr.getChannel()) {
                setBuffer(ByteBuffer.allocate((int) in.size()));
                this.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

                while (true) {
                    if (in.read(this.getBuffer()) <= 0) break;
                }
            }
        }

        logger.log(DEBUG, "File ''{0}'' read to buffer: ''{1}''", getStashFileName(), this.getBuffer());
        return this.getBuffer() != null;
    }

    @Override
    protected void preprocessVariable(String name, int keyOffset, BlockType block) {
        //not implemented
    }

    @Override
    protected void prepareForParse() throws IOException {
        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read stash from" + playerPath);
        }
        logger.log(DEBUG, "Stash ''{0}'' loaded, size=''{1}''", playerPath, this.getBuffer().capacity());
    }

    @Override
    protected void prepareBlockSpecialVariable(VariableInfo variableInfo, String name) {
        //not implemented
    }

    @Override
    protected void processBlockSpecialVariable(BlockInfo block) {
        //not implemented
    }

    @Override
    protected FileVariable getFileVariable(String variable) {
        return StashFileVariable.valueOf(variable);
    }

    @Override
    protected FileVariable getPlatformFileVariable(Platform platform, String variable) {
        return getFileVariable(variable);
    }

}

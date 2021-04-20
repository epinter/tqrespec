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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class StashParser extends FileParser {
    private static final System.Logger logger = Log.getLogger(StashParser.class.getName());
    private final String playerPath;

    public StashParser(String playerPath) {
        this.playerPath = playerPath;
    }

    private String getStashFileName() {
        return Paths.get(playerPath, Constants.STASH_FILE).toString();
    }

    public ByteBuffer loadStash() {
        parse();
        return getBuffer();
    }

    private void readStash() throws IOException {
        try (FileChannel in = new FileInputStream(getStashFileName()).getChannel()) {
            setBuffer(ByteBuffer.allocate((int) in.size()));
            this.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

            while (true) {
                if (in.read(this.getBuffer()) <= 0) break;
            }
        }

        logger.log(System.Logger.Level.DEBUG, "File ''{0}'' read to buffer: ''{1}''", getStashFileName(), this.getBuffer());
    }

    @Override
    protected void prepareForParse() throws IOException {
        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read stash from" + playerPath);
        }
        logger.log(System.Logger.Level.DEBUG, "Stash ''{0}'' loaded, size=''{1}''", playerPath, this.getBuffer().capacity());
    }

    @Override
    protected void fillBuffer() throws IOException {
        readStash();
        prepareBufferForRead();
    }

    @Override
    protected ImmutableListMultimap<String, VariableInfo> parseBlock(BlockInfo block) {
        ArrayListMultimap<String, VariableInfo> ret = ArrayListMultimap.create();
        IBlockType fileBlock = StashBlockType.BODY;
        this.getBuffer().position(block.getStart() + BEGIN_BLOCK_SIZE);

        while (this.getBuffer().position() < block.getEnd() - END_BLOCK_SIZE) {
            int keyOffset = getBuffer().position();
            String name = readString();

            //ignore all child blocks, will be parsed by main loop in parseAllBlocks
            if (BEGIN_BLOCK.equals(name)) {
                BlockInfo subBlock = getBlockInfo().get(keyOffset);
                getBuffer().position(subBlock.getEnd() + 1);
            }

            if (StringUtils.isEmpty(name) || name.equals(BEGIN_BLOCK) || name.equals(END_BLOCK)) {
                continue;
            }

            IFileVariable fileVariable;
            try {
                fileVariable = StashFileVariable.valueOf(filterFileVariableName(name));
                if (!fileVariable.location().equals(StashBlockType.BODY)
                        && !fileVariable.location().equals(StashBlockType.UNKNOWN)
                        && !fileVariable.location().equals(StashBlockType.MULTIPLE)) {
                    fileBlock = fileVariable.location();
                }
            } catch (Exception e) {
                throw new IllegalStateException(String.format("An invalid variable (%s) was found in block %s (%s), aborting."
                        , name, block.getStart(), fileBlock), e.getCause());
            }

            VariableInfo variableInfo = readVar(name, fileBlock);
            variableInfo.setBlockOffset(block.getStart());
            variableInfo.setName(name);
            variableInfo.setKeyOffset(keyOffset);

            if (variableInfo.getBlockOffset() == -1) {
                throw new IllegalStateException("Illegal block offset");
            }
            ret.put(variableInfo.getName(), variableInfo);
            putVarIndex(variableInfo.getName(), block.getStart());
        }

        block.setBlockType(fileBlock);
        return ImmutableListMultimap.copyOf(ret);
    }

    @Override
    protected IFileVariable getFileVariable(String var) {
        return StashFileVariable.valueOf(var);
    }
}

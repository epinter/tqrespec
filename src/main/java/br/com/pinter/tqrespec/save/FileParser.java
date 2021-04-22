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

import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("unused")
public abstract class FileParser {
    private static final System.Logger logger = Log.getLogger(FileParser.class.getName());

    private static final byte[] BEGIN_BLOCK_BYTES = new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    private static final byte[] END_BLOCK_BYTES = new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    private ConcurrentHashMap<Integer, BlockInfo> blockInfoTable = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Integer>> variableLocation = new ConcurrentHashMap<>();
    private List<Integer> blocksIgnore = new ArrayList<>();
    private ByteBuffer buffer = null;
    protected static final int BEGIN_BLOCK_SIZE = BEGIN_BLOCK_BYTES.length + 4;
    protected static final int END_BLOCK_SIZE = END_BLOCK_BYTES.length + 4;
    protected static final String BEGIN_BLOCK = "begin_block";
    protected static final String END_BLOCK = "end_block";
    private static final String BUG_VARIABLESIZE_ERROR_MSG = "BUG: variable size != 0";
    private final ListMultimap<String, VariableInfo> specialVariableStore = MultimapBuilder.hashKeys().arrayListValues().build();


    public ConcurrentMap<Integer, BlockInfo> getBlockInfo() {
        return blockInfoTable;
    }

    public ConcurrentMap<String, List<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    protected void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    protected List<Integer> getBlocksIgnore() {
        return blocksIgnore;
    }

    void reset() {
        blockInfoTable = new ConcurrentHashMap<>();
        variableLocation = new ConcurrentHashMap<>();
        blocksIgnore = new ArrayList<>();
        buffer = null;
    }

    public void prepareBufferForRead() {
        buffer.rewind();
    }

    protected void putVarIndex(String varName, int blockStart) {
        this.getVariableLocation().computeIfAbsent(varName, k -> Collections.synchronizedList(new ArrayList<>()));
        this.getVariableLocation().get(varName).add(blockStart);
    }

    public void parse() {
        try {
            fillBuffer();
            buildBlocksTable();
            prepareForParse();
            parseAllBlocks();
        } catch (Exception e) {
            throw new UnhandledRuntimeException(e);
        }
    }

    /**
     * This method can be used to execute any operation needed before the parse of all data blocks.
     * Before this method, {@link FileParser#buildBlocksTable()} is executed. So start, end and size of all blocks are
     * available.
     *
     * @throws IOException
     */
    protected abstract void prepareForParse() throws IOException, IncompatibleSavegameException;

    /**
     * This method should load whole file (raw data) into the bytebuffer.
     *
     * @throws IOException
     */
    public void fillBuffer() throws IOException {
        readFile();
        prepareBufferForRead();
    }

    /**
     * Method that starts the parse process and returns the buffer
     *
     * @return buffer
     */
    public ByteBuffer load() {
        parse();
        return getBuffer();
    }

    /**
     * Reads a file into the buffer
     *
     * @throws IOException
     */
    protected abstract void readFile() throws IOException;

    /**
     * This method is called to parse a block, and should return a table of variables found inside the block.
     *
     * @param block the block the method should parse.
     * @return a table with all variables found
     */
    protected ImmutableListMultimap<String, VariableInfo> parseBlock(BlockInfo block) {
        ArrayListMultimap<String, VariableInfo> ret = ArrayListMultimap.create();
        BlockType blockType = FileBlockType.BODY;
        this.getBuffer().position(block.getStart() + BEGIN_BLOCK_SIZE);

        specialVariableStore.clear();

        while (this.getBuffer().position() < block.getEnd() - END_BLOCK_SIZE) {
            int keyOffset = getBuffer().position();
            String name = readString();

            skipSubBlock(block, name, keyOffset);

            if (StringUtils.isEmpty(name) || name.equals(END_BLOCK) || name.equals(BEGIN_BLOCK)) {
                continue;
            }

            //pass current blockType (detected from previous variable read), so we can distinguish variables that repeat
            blockType = validateBlockType(block, name, blockType);

            VariableInfo variableInfo = readVar(name, blockType);
            variableInfo.setBlockOffset(block.getStart());
            variableInfo.setName(name);
            variableInfo.setKeyOffset(keyOffset);

            prepareBlockSpecialVariable(variableInfo, name);

            if (variableInfo.getBlockOffset() == -1) {
                throw new IllegalStateException("Illegal block offset");
            }
            ret.put(variableInfo.getName(), variableInfo);
            putVarIndex(variableInfo.getName(), block.getStart());
        }

        processBlockSpecialVariable(block);

        block.setBlockType(blockType);
        return ImmutableListMultimap.copyOf(ret);
    }

    /**
     * Method called by parseBlock to skips child blocks processing.
     *
     * @param block Current block
     * @param name Variable name
     * @param keyOffset Key offset for current variable
     */
    protected void skipSubBlock(BlockInfo block, String name, int keyOffset) {
        if (BEGIN_BLOCK.equals(name)) {
            //ignore all child blocks, will be parsed by main loop in parseAllBlocks
            BlockInfo subBlock = getBlockInfo().get(keyOffset);
            getBuffer().position(subBlock.getEnd() + 1);
        }
    }

    protected BlockType validateBlockType(BlockInfo block, String name, BlockType previous) {
        BlockType blockType = previous;
        FileVariable fileVariable;
        try {
            fileVariable = getFileVariable(filterFileVariableName(name));
            if (!fileVariable.location().equals(FileBlockType.BODY)
                    && !fileVariable.location().equals(FileBlockType.UNKNOWN)
                    && !fileVariable.location().equals(FileBlockType.MULTIPLE)) {
                blockType = fileVariable.location();
            }
        } catch (Exception e) {
            throw new IllegalStateException(String.format("An invalid variable (%s) was found in block %s, aborting."
                    , name, block.getStart()), e.getCause());
        }

        blockType = filterBlockType(blockType, name);

        return blockType;
    }

    /**
     *  Used to change or execute something related to blocktypes
     *
     * @param blockType BlockType
     * @param name Variable name
     * @return The new block type
     */
    protected BlockType filterBlockType(BlockType blockType, String name) {
        return blockType;
    }

    /**
     * Prepare data related to special variables. It's called at the end of parseBlock's variable processing.
     *
     * @param variableInfo The variable object
     * @param name The name found by parseBlock
     */
    protected abstract void prepareBlockSpecialVariable(VariableInfo variableInfo, String name);

    /**
     * Process all the special variables inside current block. It's called at the end of parseBlock's block processing.
     *
     * @param block Current block
     */
    protected abstract void processBlockSpecialVariable(BlockInfo block);

    /**
     * Getter for specialVariableStore
     * @return specialVariableStore
     */
    public ListMultimap<String, VariableInfo> getSpecialVariableStore() {
        return specialVariableStore;
    }

    /**
     * All blocks mapped by {@link FileParser#buildBlocksTable()} are parsed with {@link FileParser#parseBlock(BlockInfo)}.
     * Blocks listed in {@link FileParser#blocksIgnore} are skipped (e.g. a header).
     */
    public void parseAllBlocks() {
        for (BlockInfo block : blockInfoTable.values()) {
            //ignore header
            if (block == null || getBlocksIgnore().contains(block.getStart())) {
                continue;
            }

            blockInfoTable.get(block.getStart()).setVariables(parseBlock(block));

            setParentType(block);

            logger.log(System.Logger.Level.TRACE, "''{0}''", block);
        }
    }

    private void setParentType(BlockInfo block) {
        BlockInfo parentBlock = blockInfoTable.get(block.getParentOffset());
        if (parentBlock != null && parentBlock.getVariables().isEmpty()
                && parentBlock.getBlockType().equals(FileBlockType.BODY)) {
            parentBlock.setBlockType(block.getBlockType());
            setParentType(parentBlock);
        }
    }

    /**
     * Searches the raw data for blocks
     */
    public void buildBlocksTable() {
        int foundBegin = 0;
        int foundEnd = 0;

        LinkedList<Integer> queueBegin = new LinkedList<>();

        int lastBegin = -1;
        for (int i = 0; i < getBuffer().capacity(); i++) {
            Byte b = getBuffer().get(i);

            if (foundBegin > 0 && !b.equals(BEGIN_BLOCK_BYTES[foundBegin])) {
                foundBegin = 0;
            }

            if (foundEnd > 0 && !b.equals(END_BLOCK_BYTES[foundEnd])) {
                foundEnd = 0;
            }

            if (b.equals(BEGIN_BLOCK_BYTES[foundBegin]) && ++foundBegin == BEGIN_BLOCK_BYTES.length) {
                int blockTagOffset = i - (foundBegin - 1);
                queueBegin.add(blockTagOffset);
                lastBegin = blockTagOffset;
                foundBegin = 0;
                logger.log(System.Logger.Level.TRACE, "adding begin-block ''{0}'' to queue", blockTagOffset);
            }

            if (b.equals(END_BLOCK_BYTES[foundEnd]) && ++foundEnd == END_BLOCK_BYTES.length) {
                //discard 4 bytes after end_block
                int blockEnd = i + 4;
                int blockStart = -1;
                blockStart = queueBegin.removeLast();
                BlockInfo block = new BlockInfo();
                //byte offset where block starts
                block.setStart(blockStart);
                //byte offset where block ends
                block.setEnd(blockEnd);
                block.setSize(blockEnd - blockStart + 1);
                if (lastBegin >= 0 && queueBegin.peekLast() != null) {
                    block.setParentOffset(queueBegin.peekLast());
                }
                blockInfoTable.put(blockStart, block);
                foundEnd = 0;
                int logBlockStart = blockStart;
                logger.log(System.Logger.Level.TRACE, "adding end-block ''{0}'' to queue, (start=''{1}'',end=''{2}'')", blockEnd, logBlockStart, blockEnd);
            }

        }
        if (!queueBegin.isEmpty()) {
            logger.log(System.Logger.Level.ERROR, queueBegin::toString);
            throw new UnhandledRuntimeException(String.format("BUG: Error building map: '%s' data block(s) not closed", queueBegin.size()));
        }
    }

    void readString(VariableInfo variableInfo) {
        this.readString(variableInfo, false);
    }

    void readString(VariableInfo variableInfo, boolean utf16le) {
        if (variableInfo.getValSize() != -1) {
            logger.log(System.Logger.Level.ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            return;
        }
        int valOffset = getBuffer().position();

        try {
            int len = getBuffer().getInt();
            variableInfo.setVariableType(VariableType.STRING);
            variableInfo.setValSize(len);
            if (len <= 0) {
                return;
            }
            if (utf16le) {
                variableInfo.setVariableType(VariableType.STRING_UTF_16_LE);
                len *= 2;
            }

            byte[] buf = new byte[len];

            getBuffer().get(buf, 0, len);
            variableInfo.setValue(new String(buf, utf16le ? "UTF-16LE" : "UTF-8"));
            variableInfo.setValOffset(valOffset);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    void readInt(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(System.Logger.Level.ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            return;
        }
        int valOffset = getBuffer().position();

        variableInfo.setValue(getBuffer().getInt());
        variableInfo.setVariableType(VariableType.INTEGER);
        variableInfo.setValSize(4);
        variableInfo.setValOffset(valOffset);
    }

    void readFloat(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(System.Logger.Level.ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            return;
        }
        int valOffset = getBuffer().position();

        variableInfo.setValue(getBuffer().getFloat());
        variableInfo.setVariableType(VariableType.FLOAT);
        variableInfo.setValSize(4);
        variableInfo.setValOffset(valOffset);
    }

    void readUid(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(System.Logger.Level.ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            return;
        }
        int valOffset = getBuffer().position();


        byte[] buf = readUid();

        variableInfo.setValue(buf);
        variableInfo.setVariableType(VariableType.UID);
        variableInfo.setValSize(16);
        variableInfo.setValOffset(valOffset);
    }

    byte[] readUid() {
        byte[] buf = new byte[16];
        getBuffer().get(buf, 0, 16);
        return buf;
    }

    void readStream(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(System.Logger.Level.ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            return;
        }
        int valOffset = getBuffer().position();

        byte[] buf = readStream();

        variableInfo.setValue(buf);
        variableInfo.setVariableType(VariableType.STREAM);
        variableInfo.setValSize(buf.length);
        variableInfo.setValOffset(valOffset);
    }

    byte[] readStream() {
        int len = getBuffer().getInt();
        if (len <= 0) {
            return new byte[0];
        }
        byte[] buf = new byte[len];

        getBuffer().get(buf, 0, len);
        return buf;
    }

    protected String readString() {
        return this.readString(false);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    String readString(boolean utf16le) {
        byte[] buf = null;
        int len = -1;
        int offset = getBuffer().position();
        try {
            len = getBuffer().getInt();
            if (len <= 0) {
                return null;
            }
            if (utf16le) {
                len *= 2;
            }
            buf = new byte[len];

            getBuffer().get(buf, 0, len);
            return new String(buf, utf16le ? "UTF-16LE" : "UTF-8");
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        } catch (BufferUnderflowException e) {
            String bufStr = null;
            if (buf != null && buf.length < 50) {
                bufStr = new String(buf);
            }
            throw new UnhandledRuntimeException(String.format("Error parsing string. Invalid data(strlen=%d,buf=%s,position=%d).", len, bufStr, offset), e.getCause());
        }
        return null;
    }

    protected abstract FileVariable getFileVariable(String var);

    VariableInfo readVar(String name) {
        return readVar(name, new VariableInfo(), FileBlockType.UNKNOWN);
    }

    protected VariableInfo readVar(String name, VariableInfo variableInfo) {
        return readVar(name, variableInfo, FileBlockType.UNKNOWN);
    }

    protected VariableInfo readVar(String name, BlockType fileBlock) {
        return readVar(name, new VariableInfo(), fileBlock);
    }

    VariableInfo readVar(String name, VariableInfo variableInfo, BlockType fileBlock) {
        String varId = filterFileVariableName(name);

        VariableType type = null;
        FileVariable fileVariable = getFileVariable(varId);
        type = getFileVariable(varId).type();

        if (type == VariableType.UNKNOWN && fileVariable.location().equals(FileBlockType.MULTIPLE)) {
            try {
                FileVariable fileVariableMultiple = getFileVariable(
                        String.format("%s__%s", name, fileBlock.name()));
                type = fileVariableMultiple.type();
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, "Variable definition for ''{0}'' not found.", varId);
            }
        }

        if (type == VariableType.INTEGER) {
            readInt(variableInfo);
        } else if (type == VariableType.FLOAT) {
            readFloat(variableInfo);
        } else if (type == VariableType.STRING) {
            readString(variableInfo);
        } else if (type == VariableType.STRING_UTF_16_LE) {
            readString(variableInfo, true);
        } else if (type == VariableType.UID) {
            readUid(variableInfo);
        } else if (type == VariableType.STREAM) {
            readStream(variableInfo);
        } else {
            throw new IllegalArgumentException(String.format("Variable type undefined for '%s'.", name));
        }

        return variableInfo;
    }

    protected String filterFileVariableName(String name) {
        String varId = name.replaceAll("^[^a-zA-Z_$0-9.]*([a-zA-Z_$0-9.]*).*$", "$1");
        return varId.replace(".", "_");
    }
}

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
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;

@SuppressWarnings("unused")
public abstract class FileParser {
    private static final System.Logger logger = Log.getLogger(FileParser.class);
    protected static final String BEGIN_BLOCK = "begin_block";
    protected static final String END_BLOCK = "end_block";
    private static final byte[] BEGIN_BLOCK_BYTES = new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    protected static final int BEGIN_BLOCK_SIZE = BEGIN_BLOCK_BYTES.length + 4;
    private static final byte[] END_BLOCK_BYTES = new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    protected static final int END_BLOCK_SIZE = END_BLOCK_BYTES.length + 4;
    private static final String BUG_VARIABLESIZE_ERROR_MSG = "BUG: variable size != 0";
    private final ListMultimap<String, VariableInfo> specialVariableStore = MultimapBuilder.hashKeys().arrayListValues().build();
    private ConcurrentHashMap<Integer, BlockInfo> blockInfoTable = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Integer>> variableLocation = new ConcurrentHashMap<>();
    private List<Integer> blocksIgnore = new ArrayList<>();
    private ByteBuffer buffer = null;
    private Platform detectedPlatform = Platform.WINDOWS;

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

    public Platform getDetectedPlatform() {
        return detectedPlatform;
    }

    protected void setDetectedPlatform(Platform detectedPlatform) {
        this.detectedPlatform = detectedPlatform;
        if (detectedPlatform.equals(Platform.MOBILE)) {
            logger.log(INFO, "Mobile savegame detected");
        }

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
        } catch (IOException | IncompatibleSavegameException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            throw new UnhandledRuntimeException(e);
        }
    }

    /**
     * This method can be used to execute any operation needed before the parse of all data blocks.
     * Before this method, {@link FileParser#buildBlocksTable()} is executed. So start, end and size of all blocks are
     * available.
     *
     */
    protected abstract void prepareForParse() throws IOException, IncompatibleSavegameException;

    /**
     * This method should load whole file (raw data) into the bytebuffer.
     *
     */
    public void fillBuffer() throws IOException {
        if (readFile()) {
            prepareBufferForRead();
        }
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
     */
    protected abstract boolean readFile() throws IOException;

    /**
     * This method is called to parse a block, and should return a table of variables found inside the block.
     *
     * @param block the block the method should parse.
     * @return a table with all variables found
     */
    protected ImmutableListMultimap<String, VariableInfo> parseBlock(BlockInfo block) {
        ArrayListMultimap<String, VariableInfo> ret = ArrayListMultimap.create();
        BlockType blockType = FileBlockType.UNKNOWN;
        this.getBuffer().position(block.getStart() + BEGIN_BLOCK_SIZE);

        specialVariableStore.clear();

        while (this.getBuffer().position() < block.getEnd() - END_BLOCK_SIZE) {
            int keyOffset = getBuffer().position();
            String name = readStringKey();

            skipSubBlock(block, name, keyOffset);

            if (StringUtils.isEmpty(name) || name.equals(END_BLOCK) || name.equals(BEGIN_BLOCK)) {
                continue;
            }

            //pass current blockType (detected from previous variable read), so we can distinguish variables that repeat
            try {
                blockType = validateBlockType(block, name, blockType);
            } catch (InvalidVariableException e) {
                logger.log(ERROR, "Invalid variable ''{0}'' at block ''{1}'', offset ''{2}''", name, block.getStart(), keyOffset);
                throw e;
            }

            preprocessVariable(name, keyOffset, blockType);

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

            if (isDetectedBlockType(blockType) && !isDetectedBlockType(block.getBlockType())) {
                block.setBlockType(blockType);
            }
        }

        processBlockSpecialVariable(block);

        block.setBlockType(blockType);
        return ImmutableListMultimap.copyOf(ret);
    }

    protected abstract void preprocessVariable(String name, int keyOffset, BlockType block);

    /**
     * Method called by parseBlock to skips child blocks processing.
     *
     * @param block     Current block
     * @param name      Variable name
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
        final String invalidVarMsg = "An invalid variable (%s) was found in block %s, aborting.";
        String varName = filterFileVariableName(name);
        fileVariable = getPlatformFileVariable(getDetectedPlatform(), varName);
        if (fileVariable == null) {
            //try to detect the platform based on current variable
            for (Platform t : Platform.values()) {
                if (getPlatformFileVariable(t, varName) != null) {
                    setDetectedPlatform(t);
                    break;
                }
            }

            fileVariable = getFileVariable(varName);
            if (fileVariable == null) {
                throw new IllegalStateException(String.format(invalidVarMsg, name, block.getStart()));
            }
        }
        if (!isDetectedBlockType(blockType)) {
            if (isDetectedBlockType(fileVariable.location())) {
                blockType = fileVariable.location();
            } else if (FileBlockType.MULTIPLE.equals(fileVariable.location())) {
                BlockInfo parent = blockInfoTable.get(block.getParentOffset());
                if (parent != null && isDetectedBlockType(parent.getBlockType())) {
                    BlockType guessed = getBlockTypeFromParent(detectedPlatform, parent.getBlockType(), name);
                    if (!guessed.equals(FileBlockType.UNKNOWN)) {
                        blockType = guessed;
                    }
                }
            }
        }

        blockType = filterBlockType(blockType, name);

        return blockType;
    }

    private boolean isDetectedBlockType(BlockType blockType) {
        return !FileBlockType.UNKNOWN.equals(blockType) && !FileBlockType.MULTIPLE.equals(blockType);
    }

    protected BlockType getBlockTypeFromParent(Platform platform, BlockType parent, String varName) {
        throw new NotImplementedException("Not implemented");
    }

    /**
     * Used to change or execute something related to blocktypes
     *
     * @param blockType BlockType
     * @param name      Variable name
     * @return The new block type
     */
    protected BlockType filterBlockType(BlockType blockType, String name) {
        return blockType;
    }

    /**
     * Prepare data related to special variables. It's called at the end of parseBlock's variable processing.
     *
     * @param variableInfo The variable object
     * @param name         The name found by parseBlock
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
     *
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
        for (BlockInfo block : blockInfoTable.values().stream().sorted(Comparator.comparing(BlockInfo::getStart)).toList()) {
            //ignore header
            if (block == null || getBlocksIgnore().contains(block.getStart())) {
                continue;
            }

            blockInfoTable.get(block.getStart()).setVariables(parseBlock(block));

            setParentType(block);

            logger.log(TRACE, "''{0}''", block);
        }
    }

    private void setParentType(BlockInfo block) {
        BlockInfo parentBlock = blockInfoTable.get(block.getParentOffset());
        if (parentBlock != null && parentBlock.getVariables().isEmpty()
                && parentBlock.getBlockType().equals(FileBlockType.UNKNOWN)) {
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
                logger.log(TRACE, "adding begin-block ''{0}'' to queue", blockTagOffset);
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
                logger.log(TRACE, "adding end-block ''{0}'' to queue, (start=''{1}'',end=''{2}'')", blockEnd, logBlockStart, blockEnd);
            }

        }
        if (!queueBegin.isEmpty()) {
            logger.log(ERROR, queueBegin::toString);
            throw new UnhandledRuntimeException(String.format("Error building file map: '%s' data block(s) not closed. Corrupted file ?", queueBegin.size()));
        }
    }

    void readString(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            throw new IllegalStateException(BUG_VARIABLESIZE_ERROR_MSG);
        }
        int valOffset = getBuffer().position();

        try {
            int len = getBuffer().getInt();
            variableInfo.setValSize(len);
            if (len <= 0) {
                return;
            }
            String charset = "UTF-8";
            if (variableInfo.getVariableType().equals(VariableType.STRING_UTF_16_LE)) {
                charset = "UTF-16LE";
            } else if (variableInfo.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
                charset = "UTF-32LE";
            }

            len = variableInfo.getValBytesLength();

            byte[] buf = new byte[len];

            getBuffer().get(buf, 0, len);
            variableInfo.setValue(new String(buf, charset));
            variableInfo.setValOffset(valOffset);
        } catch (IOException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    void readInt(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1 && variableInfo.getVariableType() == null) {
            logger.log(ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            throw new IllegalStateException(BUG_VARIABLESIZE_ERROR_MSG);
        }

        variableInfo.setValOffset(getBuffer().position());
        variableInfo.setValue(getBuffer().getInt());
    }

    void readFloat(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1 && variableInfo.getVariableType() == null) {
            logger.log(ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            throw new IllegalStateException(BUG_VARIABLESIZE_ERROR_MSG);
        }

        variableInfo.setValOffset(getBuffer().position());
        variableInfo.setValue(getBuffer().getFloat());
    }

    void readUid(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1 && variableInfo.getVariableType() == null) {
            logger.log(ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            throw new IllegalStateException(BUG_VARIABLESIZE_ERROR_MSG);
        }
        int valOffset = getBuffer().position();


        byte[] buf = readUid();

        variableInfo.setValue(buf);
        variableInfo.setValOffset(valOffset);
    }

    byte[] readUid() {
        byte[] buf = new byte[VariableType.UID.dataTypeSize()];
        getBuffer().get(buf, 0, VariableType.UID.dataTypeSize());
        return buf;
    }

    void readStream(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            logger.log(ERROR, BUG_VARIABLESIZE_ERROR_MSG);
            throw new IllegalStateException(BUG_VARIABLESIZE_ERROR_MSG);
        }
        int valOffset = getBuffer().position();

        byte[] buf = readStream();

        variableInfo.setValue(buf);
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

    protected String readStringKey() {
        byte[] buf = null;
        int len = -1;
        int offset = getBuffer().position();
        try {
            len = getBuffer().getInt();
            if (len <= 0) {
                return null;
            }
            buf = new byte[len];

            getBuffer().get(buf, 0, len);
            return new String(buf, StandardCharsets.UTF_8);
        } catch (BufferUnderflowException e) {
            String bufStr = null;
            if (buf != null && buf.length < 50) {
                bufStr = new String(buf);
            }
            throw new UnhandledRuntimeException(String.format("Error parsing string. Invalid data(strlen=%d,buf=%s,position=%d).", len, bufStr, offset), e.getCause());
        }
    }

    protected abstract FileVariable getFileVariable(String variable);

    protected abstract FileVariable getPlatformFileVariable(Platform platform, String variable);

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

        VariableType type;
        FileVariable fileVariable = getFileVariable(varId);
        type = getFileVariable(varId).type();

        if (type == VariableType.UNKNOWN && fileVariable.location().equals(FileBlockType.MULTIPLE)) {
            FileVariable fileVariableMultiple = getFileVariable(
                    String.format("%s__%s", name, fileBlock.name()));

            if (fileVariableMultiple == null) {
                String msg = String.format("Variable definition for '%s' not found.", varId);
                logger.log(ERROR, "Variable definition for ''{0}'' not found.", varId);
                throw new UnhandledRuntimeException(msg);
            }
            type = fileVariableMultiple.type();
        }

        variableInfo.setVariableType(type);

        switch (type) {
            case INTEGER -> readInt(variableInfo);
            case FLOAT -> readFloat(variableInfo);
            case STRING, STRING_UTF_16_LE, STRING_UTF_32_LE -> readString(variableInfo);
            case UID -> readUid(variableInfo);
            case STREAM -> readStream(variableInfo);
            case null, default -> throw new IllegalArgumentException(String.format("Variable type undefined for '%s'.", name));
        }

        return variableInfo;
    }

    protected String filterFileVariableName(String name) {
        String varId = name.replaceAll("^[^a-zA-Z_$0-9.]*([a-zA-Z_$0-9.]*).*$", "$1");
        return varId.replace(".", "_");
    }
}

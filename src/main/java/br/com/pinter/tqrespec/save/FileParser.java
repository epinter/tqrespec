/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

import br.com.pinter.tqrespec.util.Util;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

abstract class FileParser {
    private final static boolean DBG = false;
    private byte[] beginBlock = new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    private byte[] endBlock = new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B};
    private ConcurrentHashMap<Integer, BlockInfo> blockInfoTable = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ArrayList<Integer>> variableLocation = new ConcurrentHashMap<>();
    private List<Integer> blocksIgnore = new ArrayList<>();
    private ByteBuffer buffer = null;
    final int BEGIN_BLOCK_SIZE = beginBlock.length + 4;
    final int END_BLOCK_SIZE = endBlock.length + 4;
    final String BEGIN_BLOCK = "begin_block";
    final String END_BLOCK = "end_block";


    ConcurrentHashMap<Integer, BlockInfo> getBlockInfo() {
        return blockInfoTable;
    }

    ConcurrentHashMap<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    ByteBuffer getBuffer() {
        return buffer;
    }

    void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    List<Integer> getBlocksIgnore() {
        return blocksIgnore;
    }


    void reset() {
        blockInfoTable = new ConcurrentHashMap<>();
        variableLocation = new ConcurrentHashMap<>();
        buffer = null;
    }

    void prepareBufferForRead() {
        buffer.rewind();
    }

    void putVarIndex(String varName, int blockStart) {
        this.getVariableLocation().computeIfAbsent(varName, k -> new ArrayList<>());
        this.getVariableLocation().get(varName).add(blockStart);
    }

    void parse() throws Exception {
        fillBuffer();
        buildBlocksTable();
        prepareForParse();
        parseAllBlocks();
    }

    /**
     * This method can be used to execute any operation needed before the parse of all data blocks.
     * Before this method, {@link FileParser#buildBlocksTable()} is executed. So start, end and size of all blocks are
     * available.
     *
     * @throws Exception
     */
    abstract void prepareForParse() throws Exception;

    /**
     * This method should load whole file (raw data) into the bytebuffer.
     *
     * @throws Exception
     */
    abstract void fillBuffer() throws Exception;

    /**
     * This method is called to parse a block, and should return a table of variables found inside the block.
     *
     * @param blockInfo the block the method should parse.
     * @return a table with all variables found
     */
    abstract Hashtable<String, VariableInfo> parseBlock(BlockInfo blockInfo);

    /**
     * All blocks mapped by {@link FileParser#buildBlocksTable()} are parsed with {@link FileParser#parseBlock(BlockInfo)}.
     * Blocks listed in {@link FileParser#blocksIgnore} are skipped (e.g. a header).
     */
    void parseAllBlocks() {
        for (BlockInfo block : blockInfoTable.values()) {
            //ignore header
            if (block == null || getBlocksIgnore().contains(block.getStart())) {
                continue;
            }

            blockInfoTable.get(block.getStart()).setVariables(parseBlock(block));

            if (DBG)
                Util.log(block);
        }
    }

    /**
     * Searches the raw data for blocks
     */
    void buildBlocksTable() {
        int foundBegin = 0;
        int foundEnd = 0;

        LinkedList<Integer> queueBegin = new LinkedList<>();

        int lastBegin = -1;
        for (int i = 0; i < getBuffer().capacity(); i++) {
            Byte b = getBuffer().get(i);

            if (DBG) {
                if (foundBegin < beginBlock.length && foundEnd < endBlock.length) {
                    System.err.printf("position:%d foundBegin:%d foundEnd:%d, str-begin:%s str-end:%s byte:%s\n",
                            i, foundBegin, foundEnd, Character.toString(beginBlock[foundBegin]), Character.toString(endBlock[foundEnd]),
                            new String(new byte[]{b}));
                } else {
                    System.err.printf("position:%d foundBegin:%d foundEnd:%d, byte:%s\n", i, foundBegin, foundEnd, new String(new byte[]{b}));
                }
            }

            if (foundBegin > 0 && !b.equals(beginBlock[foundBegin])) {
                foundBegin = 0;
            }

            if (foundEnd > 0 && !b.equals(endBlock[foundEnd])) {
                foundEnd = 0;
            }

            if (b.equals(beginBlock[foundBegin]) && ++foundBegin == beginBlock.length) {
                int blockTagOffset = i - (foundBegin - 1);
                queueBegin.add(blockTagOffset);
                lastBegin = blockTagOffset;
                if (DBG)
                    Util.log("adding begin-block %s to queue\n", blockTagOffset);
                foundBegin = 0;
            }

            if (b.equals(endBlock[foundEnd]) && ++foundEnd == endBlock.length) {
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
                if (DBG)
                    Util.log("adding end-block %s to queue, (start=%d,end=%d)\n", blockEnd, blockStart, blockEnd);
                foundEnd = 0;
            }

        }
        if (queueBegin.size() > 0) {
            Util.log(queueBegin);
            throw new RuntimeException(String.format("BUG: Error building map: '%s' data block(s) not closed", queueBegin.size()));
        }
    }

    void readString(VariableInfo variableInfo) {
        this.readString(variableInfo, false);
    }

    void readString(VariableInfo variableInfo, boolean utf16le) {
        if (variableInfo.getValSize() != -1) {
            Util.log("BUG: variable size != 0");
            return;
        }
        int valOffset = getBuffer().position();

        try {
            int len = getBuffer().getInt();
            variableInfo.setVariableType(VariableType.String);
            variableInfo.setValSize(len);
            if (len <= 0) {
                return;
            }
            if (utf16le) {
                len *= 2;
            }

            byte[] buf = new byte[len];

            getBuffer().get(buf, 0, len);
            variableInfo.setValue(new String(buf, utf16le ? "UTF-16LE" : "UTF-8"));
            variableInfo.setValOffset(valOffset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readInt(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            Util.log("BUG: variable size != 0");
            return;
        }
        int valOffset = getBuffer().position();

        variableInfo.setValue(getBuffer().getInt());
        variableInfo.setVariableType(VariableType.Integer);
        variableInfo.setValSize(4);
        variableInfo.setValOffset(valOffset);
    }

    void readFloat(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            Util.log("BUG: variable size != 0");
            return;
        }
        int valOffset = getBuffer().position();

        variableInfo.setValue(getBuffer().getFloat());
        variableInfo.setVariableType(VariableType.Float);
        variableInfo.setValSize(4);
        variableInfo.setValOffset(valOffset);
    }

    void readUid(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            Util.log("BUG: variable size != 0");
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
            Util.log("BUG: variable size != 0");
            return;
        }
        int valOffset = getBuffer().position();

        byte[] buf = readStream();

        variableInfo.setValue(buf);
        variableInfo.setVariableType(VariableType.Stream);
        variableInfo.setValSize(buf.length);
        variableInfo.setValOffset(valOffset);
    }

    byte[] readStream() {
        int len = getBuffer().getInt();
        if (len <= 0) {
            return null;
        }
        byte[] buf = new byte[len];

        getBuffer().get(buf, 0, len);
        return buf;
    }

    String readString() {
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
            e.printStackTrace();
        } catch (BufferUnderflowException e) {
            throw new RuntimeException(String.format("Error parsing string. Invalid data(strlen=%d,buf=%s,position=%d).", len, new String(Objects.requireNonNull(buf)), offset), e.getCause());
        }
        return null;
    }

    VariableInfo readVar(String name) {
        return readVar(name, new VariableInfo(), FileBlockType.Unknown);
    }

    VariableInfo readVar(String name, VariableInfo variableInfo) {
        return readVar(name, variableInfo, FileBlockType.Unknown);
    }

    VariableInfo readVar(String name, FileBlockType fileBlock) {
        return readVar(name, new VariableInfo(), fileBlock);
    }

    VariableInfo readVar(String name, VariableInfo variableInfo, FileBlockType fileBlock) {
        String varId = filterFileVariableName(name);

        VariableType type = null;
        PlayerFileVariable fileVariable = PlayerFileVariable.valueOf(varId);
        type = PlayerFileVariable.valueOf(varId).type();

        if (type == VariableType.Unknown && fileVariable.location() == FileBlockType.Multiple) {
            try {
                PlayerFileVariable fileVariableMultiple = PlayerFileVariable.valueOf(
                        String.format("%s__%s", name, fileBlock.name()));
                type = fileVariableMultiple.type();
            } catch (Exception ignored) {
            }
        }


        if (type == VariableType.Integer) {
            readInt(variableInfo);
        } else if (type == VariableType.Float) {
            readFloat(variableInfo);
        } else if (type == VariableType.String) {
            readString(variableInfo);
        } else if (type == VariableType.StringUtf16le) {
            readString(variableInfo, true);
        } else if (type == VariableType.UID) {
            readUid(variableInfo);
        } else if (type == VariableType.Stream) {
            readStream(variableInfo);
        } else {
            throw new IllegalArgumentException(String.format("Variable type undefined for '%s'.", name));
        }

        return variableInfo;
    }


    String filterFileVariableName(String name) {
        String varId = name.replaceAll("^[^a-zA-Z_$0-9.]*([a-zA-Z_$0-9.]*).*$", "$1");
        return varId.replace(".", "_");
    }
}

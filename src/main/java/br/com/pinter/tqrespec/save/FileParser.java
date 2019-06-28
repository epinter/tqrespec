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

import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;

@SuppressWarnings("CanBeFinal")
abstract class FileParser {
    private final static boolean DBG = false;

    abstract Hashtable<String, VariableInfo> parseBlock(BlockInfo blockInfo);

    abstract ByteBuffer getBuffer();

    abstract void prepareBufferForRead();

    abstract void parse() throws Exception;

    int inventoryStart = -1;

    abstract protected Hashtable<String, ArrayList<Integer>> getVariableLocation();

    protected void reset() {
        inventoryStart = -1;
    }

    void putVarIndex(String varName, int blockStart) {
        this.getVariableLocation().computeIfAbsent(varName, k -> new ArrayList<>());
        this.getVariableLocation().get(varName).add(blockStart);
    }

    private Hashtable<String, byte[]> blockTag = new Hashtable<>() {{
        put("begin_block", new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
        put("end_block", new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
    }};

    int getBlockTagSize(String tag) {
        int blockTagSize = blockTag.get(tag).length;
        return (blockTagSize + 4);
    }

    Hashtable<Integer, BlockInfo> parseAllBlocks() {
        if(inventoryStart != -1) {
            throw new IllegalStateException("Illegal inventory offset, value was not reset on load");
        }

        Hashtable<Integer, BlockInfo> ret = new Hashtable<>();
        int nextBlock = 0;
        while (nextBlock >= 0) {
            BlockInfo blockInfo = this.getNextBlock(nextBlock);

            if (blockInfo == null) {
                break;
            }
            if (DBG)
                Util.log(String.format("nextBlock=%d; blockStart=%d; blockEnd=%d; blockSize=%d", nextBlock, blockInfo.getStart(), blockInfo.getEnd(), blockInfo.getSize()));

            if (blockInfo.getSize() < 4) continue;

            if ((blockInfo.getStart() >= inventoryStart && Constants.SKIP_INVENTORY_BLOCKS && inventoryStart > 0)) {
                break;
            }
            Hashtable<String, VariableInfo> variables = parseBlock(blockInfo);
            blockInfo.setVariables(variables);
            ret.put(blockInfo.getStart(), blockInfo);
            nextBlock = blockInfo.getEnd();
        }

        return ret;
    }

    private BlockInfo getNextBlock(int offset) {
        int blockStart = searchBlockTag("begin_block", offset);
        int blockEnd = searchBlockTag("end_block", blockStart) + getBlockTagSize("end_block");

        if (blockStart > 0 && blockEnd > 0) {
            int size = blockEnd - blockStart;
            BlockInfo blockInfo = new BlockInfo();
            blockInfo.setStart(blockStart);
            blockInfo.setEnd(blockEnd);
            blockInfo.setSize(size);
            return blockInfo;
        }
        return null;
    }

    int searchBlockTag(String tag, int offset) {
        int count = 0;
        for (int i = offset; i >= 0 && i < this.getBuffer().capacity(); i++) {
            Byte b = this.getBuffer().get(i);
            byte[] blockTagBytes = this.blockTag.get(tag);
            if (b.equals(blockTagBytes[count])) {
                int blockTagOffset = i - count;
                if (++count == blockTagBytes.length) {
                    return blockTagOffset;
                }
            } else if (count > 0) {
                //outside a blocktag the count needs to be 0
                count = 0;
                if (b.equals(blockTagBytes[count])) {
                    count++;
                }
            }

        }
        return -1;
    }

    void readString(VariableInfo variableInfo, ByteBuffer byteBuffer) {
        this.readString(variableInfo, byteBuffer, false);
    }

    void readString(VariableInfo variableInfo, ByteBuffer byteBuffer, boolean utf16le) {
        if (variableInfo.getValSize() != -1) {
            System.err.println("BUG: variable size != 0");
            return;
        }
        try {
            int len = byteBuffer.getInt();
            variableInfo.setVariableType(VariableInfo.VariableType.String);
            variableInfo.setValSize(len);
            if (len <= 0) {
                return;
            }
            if (utf16le) {
                len *= 2;
            }

            byte[] buf = new byte[len];

            byteBuffer.get(buf, 0, len);

            variableInfo.setValue(new String(buf, utf16le ? "UTF-16LE" : "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void readInt(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            System.err.println("BUG: variable size != 0");
            return;
        }

        variableInfo.setValue(getBuffer().getInt());
        variableInfo.setVariableType(VariableInfo.VariableType.Integer);
        variableInfo.setValSize(4);
    }

    void readFloat(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            System.err.println("BUG: variable size != 0");
            return;
        }

        variableInfo.setValue(getBuffer().getFloat());
        variableInfo.setVariableType(VariableInfo.VariableType.Float);
        variableInfo.setValSize(4);
    }

    String readString(ByteBuffer byteBuffer) {
        return this.readString(byteBuffer, false);
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    String readString(ByteBuffer byteBuffer, boolean utf16le) {
        try {
            int len = byteBuffer.getInt();
            if (len <= 0) {
                return null;
            }
            if (utf16le) {
                len *= 2;
            }
            byte[] buf = new byte[len];

            byteBuffer.get(buf, 0, len);
            return new String(buf, utf16le ? "UTF-16LE" : "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    void parseFooter() {
        if (DBG) Util.log(String.format("Buffer(footer): '%s'", this.getBuffer()));

        while (this.getBuffer().position() < this.getBuffer().capacity()) {
            String name = readString(this.getBuffer());
            if (StringUtils.isEmpty(name)) continue;
            if (name.equalsIgnoreCase("description")) {
                String value = readString(this.getBuffer());
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
            }
        }
    }

}

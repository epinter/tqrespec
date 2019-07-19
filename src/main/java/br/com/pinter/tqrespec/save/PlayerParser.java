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

import br.com.pinter.tqrespec.gui.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Util;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

final class PlayerParser extends FileParser {
    private static final Logger logger = Log.getLogger();

    private String player = null;
    private boolean customQuest = false;
    private HeaderInfo headerInfo = new HeaderInfo();

    void setPlayer(String player) {
        this.player = player;
    }

    HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    HeaderInfo parseHeader() {
        BlockInfo block = new BlockInfo();
        int headerEnd = getBuffer().capacity() - 1;
        block.setStart(0);
        block.setEnd(headerEnd);
        block.setSize(headerEnd + 1);
        block.setVariables(new ConcurrentHashMap<>());

        HeaderInfo h = new HeaderInfo();
        while (this.getBuffer().position() <= headerEnd) {
            int keyOffset = getBuffer().position();

            String name = readString();

            if (BEGIN_BLOCK.equals(name)) {
                BlockInfo b = getBlockInfo().get(keyOffset);
                logger.fine(() -> "ignoring block offset: " + keyOffset);
                getBuffer().position(b.getEnd() + 1);
            }

            if (BEGIN_BLOCK.equals(name) || StringUtils.isBlank(name)) {
                continue;
            }

            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setKeyOffset(keyOffset);
            variableInfo.setName(name);
            variableInfo.setVariableType(VariableType.Unknown);

            String logFmt = "name=%s; value=%s";

            PlayerFileVariable e = PlayerFileVariable.valueOf(name);

            if (e.var().equals(name) && e.location() == FileBlockType.PlayerHeader) {
                readVar(name, variableInfo);

                String valueLog = null;

                if (e.type() == VariableType.Integer) {
                    int valueInt = (int) variableInfo.getValue();
                    valueLog = String.valueOf(valueInt);
                    readIntegerFromHeader(h,name,valueInt);
                }

                if (e.type() == VariableType.String) {
                    String valueString = (String) variableInfo.getValue();
                    valueLog = valueString;
                    readStringFromHeader(h,name,valueString);
                }

                if (e.type() == VariableType.Stream) {
                    byte[] value = (byte[]) variableInfo.getValue();
                    valueLog = new String(value);
                }

                String logMsg = String.format(logFmt, name, valueLog);
                logger.fine(logMsg);
            }

            if (variableInfo.getVariableType() == VariableType.Unknown) {
                throw new IllegalStateException(String.format("An invalid variable (%s) was found in header, aborting."
                        , name));
            }
            block.getVariables().put(variableInfo.getName(), variableInfo);
        }
        getBlockInfo().put(block.getStart(), block);
        return h;
    }

    private void readIntegerFromHeader(HeaderInfo h, String name, int valueInt) {
        if (name.equals(PlayerFileVariable.valueOf("headerVersion").var()))
            h.setHeaderVersion(valueInt);
        if (name.equals(PlayerFileVariable.valueOf("playerVersion").var()))
            h.setPlayerVersion(valueInt);
        if (name.equals(PlayerFileVariable.valueOf("playerLevel").var()))
            h.setPlayerLevel(valueInt);
    }

    private void readStringFromHeader(HeaderInfo h, String name, String valueString) {
        if (name.equals(PlayerFileVariable.valueOf("playerCharacterClass").var()))
            h.setPlayerCharacterClass(valueString);
        if (name.equals(PlayerFileVariable.valueOf("playerClassTag").var())) {
            h.setPlayerClassTag(valueString);
        }
    }

    @Override
    void fillBuffer() throws IOException {
        readPlayerChr();
        prepareBufferForRead();
    }

    @Override
    void prepareForParse() throws IOException, IncompatibleSavegameException {
        //add header to list of ignored blocks
        getBlocksIgnore().add(0);

        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read Player.chr from player " + this.player);
        }
        logger.fine(() -> String.format("File '%s' loaded, size=%d", this.player, this.getBuffer().capacity()));

        headerInfo = parseHeader();

        if (headerInfo.getHeaderVersion() != 2 && headerInfo.getHeaderVersion() != 3) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (headerVersion must be 2 or 3)", this.player));
        }
        if (headerInfo.getPlayerVersion() != 5) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (playerVersion must be == 5)", this.player));
        }
    }

    ByteBuffer loadPlayer(String playerName, boolean customQuest) {
        if (State.get().getSaveInProgress() != null && State.get().getSaveInProgress()) {
            return null;
        }
        this.customQuest = customQuest;
        this.player = playerName;
        parse();
        return getBuffer();
    }

    @Override
    void reset() {
        super.reset();
        headerInfo = new HeaderInfo();
    }

    void readPlayerChr() throws IOException {
        reset();

        File playerChr = new File(Util.playerChr(player, customQuest).toString());

        if (!playerChr.exists()) {
            logger.info(() -> String.format("File '%s' doesn't exists", playerChr.toString()));
            return;
        }

        try (FileChannel in = new FileInputStream(playerChr).getChannel()) {
            setBuffer(ByteBuffer.allocate((int) in.size()));
            this.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

            while (true) {
                if (in.read(this.getBuffer()) <= 0) break;
            }
        }

        logger.fine("File read to buffer: " + this.getBuffer());
    }

    @Override
    ConcurrentHashMap<String, VariableInfo> parseBlock(BlockInfo block) {
        ConcurrentHashMap<String, VariableInfo> ret = new ConcurrentHashMap<>();
        FileBlockType fileBlock = FileBlockType.Body;
        this.getBuffer().position(block.getStart() + BEGIN_BLOCK_SIZE);
        ArrayList<VariableInfo> temp = new ArrayList<>();

        while (this.getBuffer().position() < block.getEnd() - END_BLOCK_SIZE) {
            int keyOffset = getBuffer().position();
            String name = readString();

            if (StringUtils.isEmpty(name)) {
                logger.info(() -> String.format("empty name at block %d pos %d (BEGIN_BLOCK_SIZE=%d, END_BLOCK_SIZE=%d, block_start=%s block_end=%d",
                        block.getStart(), keyOffset, BEGIN_BLOCK_SIZE, END_BLOCK_SIZE, block.getStart(), block.getEnd()));
            }

            if (BEGIN_BLOCK.equals(name)) {
                //ignore all child blocks, will be parsed by main loop in parseAllBlocks
                BlockInfo subBlock = getBlockInfo().get(keyOffset);
                getBuffer().position(subBlock.getEnd() + 1);
            }

            if (StringUtils.isEmpty(name) || name.equals(END_BLOCK) || name.equals(BEGIN_BLOCK)) {
                continue;
            }

            IFileVariable fileVariable;
            try {
                fileVariable = PlayerFileVariable.valueOf(filterFileVariableName(name));
                if (fileVariable.location() != FileBlockType.Body
                        && fileVariable.location() != FileBlockType.Unknown
                        && fileVariable.location() != FileBlockType.Multiple) {
                    fileBlock = fileVariable.location();
                }
            } catch (Exception e) {
                throw new IllegalStateException(String.format("An invalid variable (%s) was found in block %s (%s), aborting."
                        , name, block.getStart(), fileBlock), e.getCause());
            }

            //prepare fileblock for special var 'temp'(attributes)
            if (name.equals("temp") && fileBlock == FileBlockType.Body) {
                fileBlock = FileBlockType.PlayerAttributes;
            }

            VariableInfo variableInfo = readVar(name, fileBlock);
            variableInfo.setName(name);
            variableInfo.setKeyOffset(keyOffset);

            //store variable for attributes and difficulty in a dedicated list
            if (name.equals("temp")) {
                temp.add(variableInfo);
            }

            ret.put(variableInfo.getName(), variableInfo);
            putVarIndex(variableInfo.getName(), block.getStart());
        }

        if (temp.size() == 1) {
            VariableInfo difficulty = temp.get(0);
            difficulty.setName("difficulty");
            ret.put(difficulty.getName(), difficulty);
            putVarIndex(difficulty.getName(), block.getStart());
        }

        if (temp.size() == 5) {
            VariableInfo str = temp.get(0);
            VariableInfo dex = temp.get(1);
            VariableInfo inl = temp.get(2);
            VariableInfo life = temp.get(3);
            VariableInfo mana = temp.get(4);
            str.setName("str");
            dex.setName("dex");
            inl.setName("int");
            life.setName("life");
            mana.setName("mana");

            ret.put(str.getName(), str);
            putVarIndex(str.getName(), block.getStart());
            ret.put(dex.getName(), dex);
            putVarIndex(dex.getName(), block.getStart());
            ret.put(inl.getName(), inl);
            putVarIndex(inl.getName(), block.getStart());
            ret.put(life.getName(), life);
            putVarIndex(life.getName(), block.getStart());
            ret.put(mana.getName(), mana);
            putVarIndex(mana.getName(), block.getStart());
            String logMsg = "blockStart: %d; variableInfo: %s;";

            logger.fine(() -> String.format(logMsg, block.getStart(), ret.get("str").toString()));
            logger.fine(() -> String.format(logMsg, block.getStart(), ret.get("dex").toString()));
            logger.fine(() -> String.format(logMsg, block.getStart(), ret.get("int").toString()));
            logger.fine(() -> String.format(logMsg, block.getStart(), ret.get("life").toString()));
            logger.fine(() -> String.format(logMsg, block.getStart(), ret.get("mana").toString()));
        }
        return ret;
    }
}

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

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.*;
import br.com.pinter.tqrespec.tqdata.GameVersion;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.EnumSet;

final class PlayerParser extends FileParser {
    private static final System.Logger logger = Log.getLogger(PlayerParser.class.getName());

    private final String player;
    private final File playerChr;
    private HeaderInfo headerInfo;

    PlayerParser(File playerChr, String playerName) {
        this.playerChr = playerChr;
        this.player = playerName;
    }

    HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    HeaderInfo parseHeader() throws IncompatibleSavegameException {
        ArrayListMultimap<String, VariableInfo> variables = ArrayListMultimap.create();

        BlockInfo block = new BlockInfo();
        int headerEnd = getBuffer().capacity() - 1;
        block.setStart(0);
        block.setEnd(headerEnd);
        block.setSize(headerEnd + 1);
        block.setBlockType(PlayerBlockType.PLAYER_HEADER);

        HeaderInfo h = new HeaderInfo();
        while (this.getBuffer().position() <= headerEnd) {
            int keyOffset = getBuffer().position();

            String name = readStringKey();

            if (BEGIN_BLOCK.equals(name)) {
                BlockInfo b = getBlockInfo().get(keyOffset);
                logger.log(System.Logger.Level.DEBUG, "ignoring block offset: ''{0}''", keyOffset);
                getBuffer().position(b.getEnd() + 1);
            }

            if (BEGIN_BLOCK.equals(name) || StringUtils.isBlank(name)) {
                continue;
            }

            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setBlockOffset(block.getStart());
            variableInfo.setKeyOffset(keyOffset);
            variableInfo.setName(name);
            variableInfo.setVariableType(VariableType.UNKNOWN);

            String logFmt = "name=%s; value=%s; type=%s";

            PlayerFileVariable e = null;
            try {
                e = PlayerFileVariable.valueOf(getDetectedPlatform(), name);
            } catch (InvalidVariableException exception) {
                logger.log(System.Logger.Level.ERROR, "", exception);
                logger.log(System.Logger.Level.ERROR, "Variable ''{0}'' not found for {1}, trying {2} ",
                        name, getDetectedPlatform(), Platform.MOBILE);
                if(Platform.WINDOWS.equals(getDetectedPlatform()) && PlayerFileVariable.valueOf(Platform.MOBILE, name)!=null
                        && h.getHeaderVersion().equals(GameVersion.TQLE)) {
                    e = PlayerFileVariable.valueOf(Platform.MOBILE, name);
                    setDetectedPlatform(Platform.MOBILE);
                }
            }

            if(e == null) {
                throw new IncompatibleSavegameException("Invalid variable '{}'");
            }

            if (e.variable().equals(name) && e.location().equals(PlayerBlockType.PLAYER_HEADER)) {
                readVar(name, variableInfo);

                String valueLog = null;

                if (e.type() == VariableType.INTEGER) {
                    int valueInt = (int) variableInfo.getValue();
                    valueLog = String.valueOf(valueInt);
                    readIntegerFromHeader(h, name, valueInt);
                }

                if (e.type() == VariableType.STRING) {
                    String valueString = (String) variableInfo.getValue();
                    valueLog = valueString;
                    readStringFromHeader(h, name, valueString);
                }

                String logMsg = String.format(logFmt, name, valueLog, e.type());
                logger.log(System.Logger.Level.DEBUG, logMsg);
            }

            if (variableInfo.getVariableType() == VariableType.UNKNOWN) {
                throw new IllegalStateException(String.format("An invalid variable (%s) was found in header, aborting."
                        , name));
            }
            variables.put(variableInfo.getName(), variableInfo);
            if (variableInfo.getName().equals("playerCharacterClass")
                || variableInfo.getName().equals("playerVersion")) {
                putVarIndex(variableInfo.getName(), block.getStart());
            }
        }
        getBlockInfo().put(block.getStart(), block);
        block.setVariables(ImmutableListMultimap.copyOf(variables));
        if(block.getVariables().containsKey("currentDifficulty") && h.getHeaderVersion().equals(GameVersion.TQLE)) {
            setDetectedPlatform(Platform.MOBILE);
        }
        return h;
    }

    private void readIntegerFromHeader(HeaderInfo h, String name, int valueInt) throws IncompatibleSavegameException {
        try {
            if (name.equals(PlayerFileVariable.valueOf(getDetectedPlatform(), "headerVersion").variable()))
                h.setHeaderVersion(GameVersion.fromValue(valueInt));
        } catch (EnumConstantNotPresentException e) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible character '%s' (unknown headerVersion)", this.player));

        }
        if (name.equals(PlayerFileVariable.valueOf(getDetectedPlatform(), "playerVersion").variable()))
            h.setPlayerVersion(valueInt);
        if (name.equals(PlayerFileVariable.valueOf(getDetectedPlatform(), "playerLevel").variable()))
            h.setPlayerLevel(valueInt);
    }

    private void readStringFromHeader(HeaderInfo h, String name, String valueString) {
        if (name.equals(PlayerFileVariable.valueOf(getDetectedPlatform(), "playerCharacterClass").variable()))
            h.setPlayerCharacterClass(valueString);
        if (name.equals(PlayerFileVariable.valueOf(getDetectedPlatform(), "playerClassTag").variable())) {
            h.setPlayerClassTag(valueString);
        }
    }

    @Override
    protected void prepareForParse() throws IOException, IncompatibleSavegameException {
        //add header to list of ignored blocks
        getBlocksIgnore().add(0);

        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read Player.chr from player " + this.player);
        }
        logger.log(System.Logger.Level.DEBUG, "Character ''{0}'' loaded, size=''{1}''", this.player, this.getBuffer().capacity());

        headerInfo = parseHeader();

        if (!EnumSet.of(GameVersion.TQIT, GameVersion.TQAE, GameVersion.TQLE).contains(headerInfo.getHeaderVersion())) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible character '%s' (unknown headerVersion)", this.player));
        }

        if (headerInfo.getPlayerVersion() < 5) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible character '%s' (playerVersion must be >= 5)", this.player));
        }
    }

    @Override
    protected boolean readFile() throws IOException {
        if (!playerChr.exists()) {
            logger.log(System.Logger.Level.ERROR, "File ''{0}'' doesn't exists", playerChr.toString());
            throw new IOException("Couldn't load file");
        }

        try (FileChannel in = new FileInputStream(playerChr).getChannel()) {
            setBuffer(ByteBuffer.allocate((int) in.size()));
            this.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

            while (true) {
                if (in.read(this.getBuffer()) <= 0) break;
            }

        }

        logger.log(System.Logger.Level.DEBUG, "File ''{0}'' read to buffer: ''{1}''", playerChr, this.getBuffer());
        return this.getBuffer() != null;
    }

    @Override
    protected void preprocessVariable(String name, int keyOffset, BlockType blockType) {
        if (! Platform.MOBILE.equals(getDetectedPlatform())
                && ((name.equals("mySaveId") && blockType.equals(PlayerBlockType.PLAYER_MAIN))
                || headerInfo.getHeaderVersion().equals(GameVersion.TQLE))) {
            setDetectedPlatform(Platform.MOBILE);
        }
    }

    @Override
    protected BlockType filterBlockType(BlockType type, String name) {
        //prepare fileblock for special var 'temp' (attributes)
        //temp variables for the attributes are always inside a separate block, so the current blocktype will be always BODY
        //difficulty variable is always at the end of main block, so blocktype will be PLAYER_MAIN
        if (name.equals("temp") && type.equals(FileBlockType.UNKNOWN)) {
            return PlayerBlockType.PLAYER_ATTRIBUTES;
        }
        return type;
    }

    @Override
    protected BlockType getBlockTypeFromParent(Platform platform, BlockType parent, String varName) {
        return PlayerFileVariable.getBlockTypeFromParent(platform, parent, varName);
    }

    @Override
    protected void prepareBlockSpecialVariable(VariableInfo variableInfo, String name) {
        //store variable for attributes and difficulty in a dedicated list
        if (name.equals("temp")) {
            getSpecialVariableStore().put("temp", variableInfo);
        }
    }

    @Override
    protected void processBlockSpecialVariable(BlockInfo block) {
        String key = "temp";
        String logMsg = "blockStart: ''{0}''; variableInfo: ''{1}'';";
        if (getSpecialVariableStore().get(key).size() == 1) {
            VariableInfo difficulty = getSpecialVariableStore().get(key).get(0);
            difficulty.setAlias("difficulty");
            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), difficulty.toString());
        } else if (getSpecialVariableStore().get(key).size() == 5) {
            VariableInfo str = getSpecialVariableStore().get(key).get(0);
            VariableInfo dex = getSpecialVariableStore().get(key).get(1);
            VariableInfo inl = getSpecialVariableStore().get(key).get(2);
            VariableInfo life = getSpecialVariableStore().get(key).get(3);
            VariableInfo mana = getSpecialVariableStore().get(key).get(4);
            str.setAlias("str");
            dex.setAlias("dex");
            inl.setAlias("int");
            life.setAlias("life");
            mana.setAlias("mana");

            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), str.toString());
            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), dex.toString());
            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), inl.toString());
            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), life.toString());
            logger.log(System.Logger.Level.DEBUG, logMsg, block.getStart(), mana.toString());
        }
    }

    @Override
    protected FileVariable getFileVariable(String variable) {
        return PlayerFileVariable.valueOf(getDetectedPlatform(), variable);
    }

    @Override
    protected FileVariable getPlatformFileVariable(Platform platform, String variable) {
        try {
            return PlayerFileVariable.valueOf(platform, variable);
        } catch (InvalidVariableException e) {
            return null;
        }
    }

}

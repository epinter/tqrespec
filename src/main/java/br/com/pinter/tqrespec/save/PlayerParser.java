/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
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

import br.com.pinter.tqrespec.Constants;
import br.com.pinter.tqrespec.GameInfo;
import br.com.pinter.tqrespec.Util;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Hashtable;

public class PlayerParser {
    private final static boolean DBG = false;
    private String playerSelected = null;

    private int inventoryStart = -1;

    private Hashtable<String, byte[]> blockTag = new Hashtable<String, byte[]>() {{
        put("begin_block", new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
        put("end_block", new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
    }};

    PlayerParser(String playerSelected) throws Exception {
        this(playerSelected, false);

    }

    private PlayerParser(String playerName, boolean customQuest) throws Exception {
        this.playerSelected = playerName;
        PlayerData.getInstance().reset();
        this.loadPlayerChr(customQuest);

        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read Player.chr from player " + this.playerSelected);
        }
        if (DBG) Util.log(String.format("File '%s' loaded, size=%d",
                this.playerSelected, this.getBuffer().capacity()));
        HeaderInfo headerInfo = parseHeader();

        PlayerData.getInstance().setHeaderInfo(headerInfo);
        PlayerData.getInstance().setVariableLocation(new Hashtable<String, ArrayList<Integer>>());

        if (headerInfo.getHeaderVersion() != 2) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (headerVersion must be == 2)", this.playerSelected));
        }
        if (headerInfo.getPlayerVersion() != 5) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (playerVersion must be == 5)", this.playerSelected));
        }
        Hashtable<Integer, BlockInfo> blocks = this.parseAllBlocks();
        PlayerData.getInstance().setBlockInfo(blocks);
        if(inventoryStart==-1) {
            this.parseFooter();
        }
        this.prepareBufferForRead();
        PlayerData.getInstance().setPlayerName(playerName);
        PlayerData.getInstance().setChanges(new Hashtable<Integer, byte[]>());
        PlayerData.getInstance().setValuesLengthIndex(new Hashtable<Integer, Integer>());
    }

    private void prepareBufferForRead() {
        PlayerData.getInstance().getBuffer().rewind();
    }

    private ByteBuffer getBuffer() {
        return PlayerData.getInstance().getBuffer();
    }

    private int getBlockTagSize(String tag) {
        int blockTagSize = blockTag.get(tag).length;
        return (blockTagSize + 4);
    }

    private void loadPlayerChr(boolean customQuest) throws Exception {
        if (this.getBuffer() != null) {
            PlayerData.getInstance().reset();
        }
        String path;
        if (customQuest) {
            path = GameInfo.getInstance().getSaveDataUserPath();
        } else {
            path = GameInfo.getInstance().getSaveDataMainPath();
        }
        File playerChr = new File(path +
                String.format("\\_%s\\Player.chr", this.playerSelected));

        if (!playerChr.exists()) {
            return;
        }

        try {
            FileChannel in = new FileInputStream(playerChr).getChannel();
            PlayerData.getInstance().setBuffer(ByteBuffer.allocate((int) in.size()));
            this.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
            PlayerData.getInstance().getBuffer().rewind();

            while (true) {
                if (in.read(this.getBuffer()) <= 0) break;
            }
            in.close();

            this.prepareBufferForRead();

            if(DBG) Util.log("File read to buffer: " + this.getBuffer());

            PlayerData.getInstance().setPlayerChr(playerChr.toPath());
        } catch (Exception e) {
            PlayerData.getInstance().reset();
            throw e;
        }

    }

    private Hashtable<Integer, BlockInfo> parseAllBlocks() {
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

            if((blockInfo.getStart() >= inventoryStart && Constants.SKIP_INVENTORY_BLOCKS && inventoryStart > 0)) {
                break;
            }
            Hashtable<String, VariableInfo> variables = parseBlock(blockInfo);
            blockInfo.setVariables(variables);
            ret.put(blockInfo.getStart(), blockInfo);
            nextBlock = blockInfo.getEnd();
        }

        return ret;
    }

    private Hashtable<String, VariableInfo> parseBlock(BlockInfo blockInfo) {
        Hashtable<String, VariableInfo> ret = new Hashtable<>();

        this.getBuffer().position(blockInfo.getStart() + getBlockTagSize("begin_block"));

        int keyread = 0;
        int valread = 0;

        ArrayList<VariableInfo> temp = new ArrayList<>();

        while (this.getBuffer().position() <= (blockInfo.getEnd() - getBlockTagSize("end_block"))) {
            int dataOffset = 0;
            int valSize = -1;
            int keyOffset = getBuffer().position();
            String name = readString(getBuffer());
            keyread++;

            if (StringUtils.isEmpty(name)) {
                continue;
            }

            String value = null;

            if (StringUtils.isEmpty(name)) {
                if (DBG)
                    Util.log(String.format("empty name at block %d pos %d", blockInfo.getStart(), getBuffer().position()));
            }

            int valOffset = getBuffer().position();

            VariableInfo.VariableType valType = VariableInfo.VariableType.Unknown;

            if (name.equalsIgnoreCase("begin_block")) {
                dataOffset -= getBlockTagSize("begin_block");
                valread++;
            } else if (name.equalsIgnoreCase("end_block")) {
                dataOffset -= getBlockTagSize("end_block");
                valread++;
                if(temp.size() == 1) {
                    VariableInfo variableInfo = temp.get(temp.size() - 1);
                    variableInfo.setName("difficulty");
                    ret.put(variableInfo.getName(), variableInfo);
                    putVarIndex(variableInfo.getName(), blockInfo.getStart());
                    int diffInt = this.getBuffer().getInt(variableInfo.getValOffset());
                    variableInfo.setValue(diffInt);
                    variableInfo.setVariableType(VariableInfo.VariableType.Integer);
                    if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s; data_offset=%d", blockInfo.getStart(), variableInfo.toString(), dataOffset));
                }
            } else if (name.equalsIgnoreCase("myPlayerName")) {
                valType = VariableInfo.VariableType.String;
                value = this.readString(this.getBuffer(), true);
                if (StringUtils.isNotEmpty(value)) {
                    try {
                        value = new String(value.getBytes(), "UTF-16LE");
                        valSize = value.getBytes().length * 2;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                } else {
                    valSize = 0;
                }
                valread++;
            } else if (name.equalsIgnoreCase("defaultText")) {
                valType = VariableInfo.VariableType.String;
                value = this.readString(this.getBuffer(), true);
                if (StringUtils.isNotEmpty(value)) {
                    try {
                        value = new String(value.getBytes(), "UTF-16LE");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    valSize = value.getBytes().length;
                } else {
                    valSize = 0;
                }
                valread++;
            } else if (name.equalsIgnoreCase("(*greatestMonsterKilledName)[i]")) {
                valType = VariableInfo.VariableType.String;
                value = this.readString(this.getBuffer(), true);
                if (StringUtils.isNotEmpty(value)) {
                    try {
                        value = new String(value.getBytes(), "UTF-16LE");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    valSize = value.getBytes().length;
                } else {
                    valSize = 0;
                }
                valread++;
            } else if (name.equalsIgnoreCase("playerTexture")) {
                valType = VariableInfo.VariableType.String;
                value = this.readString(this.getBuffer());
                if (StringUtils.isNotEmpty(value)) {
                    valSize = value.getBytes().length;
                } else {
                    valSize = 0;
                }
                valread++;
            } else if (name.equalsIgnoreCase("skillName")) {
                valType = VariableInfo.VariableType.String;
                value = this.readString(this.getBuffer());
                if (StringUtils.isNotEmpty(value)) {
                    valSize = value.getBytes().length;
                } else {
                    valSize = 0;
                }
                valread++;
            } else if (name.equalsIgnoreCase("isInMainQuest")
                    || name.equalsIgnoreCase("disableAutoPopV2")
                    || name.equalsIgnoreCase("numTutorialPagesV2")
                    || name.equalsIgnoreCase("currentPageV2")
                    || name.equalsIgnoreCase("teleportUIDsSize")
                    || name.equalsIgnoreCase("markerUIDsSize")
                    || name.equalsIgnoreCase("respawnUIDsSize")
                    || name.equalsIgnoreCase("versionCheckRespawnInfo")
                    || name.equalsIgnoreCase("versionCheckTeleportInfo")
                    || name.equalsIgnoreCase("versionCheckMovementInfo")
                    || name.equalsIgnoreCase("compassState")
                    || name.equalsIgnoreCase("skillWindowShowHelp")
                    || name.equalsIgnoreCase("useAlternate")
                    || name.equalsIgnoreCase("alternateConfig")
                    || name.equalsIgnoreCase("alternateConfigEnabled")
                    || name.equalsIgnoreCase("alternateConfig")
                    || name.equalsIgnoreCase("itemsFoundOverLifetimeUniqueTotal")
                    || name.equalsIgnoreCase("itemsFoundOverLifetimeRandomizedTotal")
                    || name.equalsIgnoreCase("hasBeenInGame")
                    || name.equalsIgnoreCase("tempBool")
                    || name.equalsIgnoreCase("alternate")
                    || name.equalsIgnoreCase("skillLevel")
                    || name.equalsIgnoreCase("skillEnabled")
                    || name.equalsIgnoreCase("skillSubLevel")
                    || name.startsWith("skillActive")
                    || name.equalsIgnoreCase("skillWindowSelection")
                    || name.equalsIgnoreCase("skillSettingValid")
                    || name.startsWith("primarySkill")
                    || name.startsWith("secondarySkill")
                    || name.equalsIgnoreCase("playTimeInSeconds")
                    || name.equalsIgnoreCase("numberOfDeaths")
                    || name.equalsIgnoreCase("numberOfKills")
                    || name.equalsIgnoreCase("experienceFromKills")
                    || name.equalsIgnoreCase("healthPotionsUsed")
                    || name.equalsIgnoreCase("manaPotionsUsed")
                    || name.equalsIgnoreCase("maxLevel")
                    || name.equalsIgnoreCase("numHitsReceived")
                    || name.equalsIgnoreCase("numHitsInflicted")
                    || name.equalsIgnoreCase("greatestDamageInflicted")
                    || name.equalsIgnoreCase("(*greatestMonsterKilledLevel)[i]")
                    || name.equalsIgnoreCase("(*greatestMonsterKilledLifeAndMana)[i]")
                    || name.equalsIgnoreCase("criticalHitsInflicted")
                    || name.equalsIgnoreCase("skillTransition")
                    || name.equalsIgnoreCase("criticalHitsReceived")
                    || name.equalsIgnoreCase("size")
                    || name.equalsIgnoreCase("max")
                    || name.equalsIgnoreCase("equipmentSelection")
                    || name.equalsIgnoreCase("equipmentCtrlIOStreamVersion")
                    || name.equalsIgnoreCase("equipmentSelection")
                    || name.equalsIgnoreCase("equipmentSelection")
                    || name.equalsIgnoreCase("equipmentSelection")
                    || name.equalsIgnoreCase("seed")
                    || name.equalsIgnoreCase("var1")) {
                valType = VariableInfo.VariableType.Integer;
                dataOffset = 4;
                valSize = dataOffset;
                valread++;
            } else if (name.equalsIgnoreCase("teleportUID")
                    || name.equalsIgnoreCase("respawnUID")
                    || name.equalsIgnoreCase("markerUID")
                    || name.equalsIgnoreCase("strategicMovementRespawnPoint[i]")
                    ) {
                valType = VariableInfo.VariableType.UID;
                dataOffset = 16;
                valSize = dataOffset;
                valread++;
            } else if (name.equalsIgnoreCase("currentStats.experiencePoints")) {
                valType = VariableInfo.VariableType.Integer;
                value = String.valueOf(getBuffer().getInt());
                valSize = 4;
                valread++;
            } else if (name.equalsIgnoreCase("currentStats.charLevel")) {
                valType = VariableInfo.VariableType.Integer;
                value = String.valueOf(getBuffer().getInt());
                valSize = 4;
                valread++;
            } else if (name.equalsIgnoreCase("modifierPoints")
                    || name.equalsIgnoreCase("versionRespawnPoint")
                    || name.equalsIgnoreCase("money")) {
                valType = VariableInfo.VariableType.Integer;
                value = String.valueOf(getBuffer().getInt());
                valSize = 4;
                valread++;
            } else if (name.equalsIgnoreCase("storedType")) {
                valType = VariableInfo.VariableType.Integer;
                dataOffset = 4;
                valSize = dataOffset;
                valread++;
            } else if (name.equalsIgnoreCase("itemPositionsSavedAsGridCoords")) {
                //inventory
                if(Constants.SKIP_INVENTORY_BLOCKS) {
                    inventoryStart = valOffset;
                }
                break;
            } else if (name.equalsIgnoreCase("temp")) {
                valType = VariableInfo.VariableType.Float;
                value = String.valueOf(getBuffer().getFloat());
                valSize = 4;
                valread++;
            } else {
                valType = VariableInfo.VariableType.Integer;
                dataOffset = this.getBuffer().getInt();
                valread++;
            }
            if (keyread > 0) {
                if (valread > 0) {
                    keyread = 0;
                    valread = 0;
                } else {
                    dataOffset = 4;
                }
            }

            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setName(name);
            variableInfo.setKeyOffset(keyOffset);
            variableInfo.setValOffset(valOffset);
            variableInfo.setVariableType(valType);

            if (StringUtils.isNotEmpty(value)) {
                variableInfo.setValSize(valSize);
                if(variableInfo.getVariableType() == VariableInfo.VariableType.Float) {
                    variableInfo.setValue(Float.parseFloat(value));
                } else if(variableInfo.getVariableType() == VariableInfo.VariableType.Integer) {
                    variableInfo.setValue(Integer.parseInt(value));
                } else if(variableInfo.getVariableType() == VariableInfo.VariableType.String) {
                    variableInfo.setValue(value);
                }
                if (name.equalsIgnoreCase("temp")) {
                    if (temp.size() == 4) {
                        VariableInfo str = temp.get(0);
                        VariableInfo dex = temp.get(1);
                        VariableInfo inl = temp.get(2);
                        VariableInfo life = temp.get(3);
                        str.setName("str");
                        dex.setName("dex");
                        inl.setName("int");
                        life.setName("life");
                        variableInfo.setName("mana");

                        ret.put(str.getName(), str);
                        putVarIndex(str.getName(), blockInfo.getStart());
                        ret.put(dex.getName(), dex);
                        putVarIndex(dex.getName(), blockInfo.getStart());
                        ret.put(inl.getName(), inl);
                        putVarIndex(inl.getName(), blockInfo.getStart());
                        ret.put(life.getName(), life);
                        putVarIndex(life.getName(), blockInfo.getStart());
                        if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("str").toString()));
                        if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("dex").toString()));
                        if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("int").toString()));
                        if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("life").toString()));
                    } else {
                        temp.add(variableInfo);
                    }
                    if (DBG) Util.log(variableInfo.getName());
                }

                if (!variableInfo.getName().equals("temp")) {
                    ret.put(variableInfo.getName(), variableInfo);
                    putVarIndex(variableInfo.getName(), blockInfo.getStart());
                    if (DBG) Util.log(String.format("blockStart: %d; variableInfo: %s; data_offset=%d", blockInfo.getStart(), variableInfo.toString(), dataOffset));
                }
            } else {
                if (DBG)
                    Util.log(String.format("IGNORED name=%s; value=%s; key_offset=%d, val_offset=%d; val_size=%d", name, value, keyOffset, valOffset, valSize));
            }
            if (dataOffset > 0) {
                this.getBuffer().position(this.getBuffer().position() + dataOffset);
                if (DBG) Util.log(String.format("DATAOFFSET: %d; POSITION: %d", dataOffset, getBuffer().position()));
            }

        }
        return ret;

    }

    private void parseFooter() {
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

    private void putVarIndex(String varName, int blockStart) {
        if(PlayerData.getInstance().getVariableLocation().get(varName)==null) {
            PlayerData.getInstance().getVariableLocation().put(varName, new ArrayList<Integer>());
        }
        PlayerData.getInstance().getVariableLocation().get(varName).add(blockStart);
    }
    private HeaderInfo parseHeader() {
        int headerEnd = searchBlockTag("begin_block", 0) - 1;
        HeaderInfo headerInfo = new HeaderInfo();

        while (this.getBuffer().position() <= headerEnd) {
            String name = readString(this.getBuffer());
            if (StringUtils.isEmpty(name)) continue;
            if (name.equalsIgnoreCase("headerVersion")) {
                int value = this.getBuffer().getInt();
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
                headerInfo.setHeaderVersion(value);
            } else if (name.equalsIgnoreCase("playerCharacterClass")) {
                String value = readString(this.getBuffer());
                headerInfo.setPlayerCharacterClass(value);
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
            } else if (name.equalsIgnoreCase("uniqueId")) {
                this.getBuffer().position(this.getBuffer().position() + 16);
            } else if (name.equalsIgnoreCase("streamData")) {
                int data_length = this.getBuffer().getInt();
                this.getBuffer().position(this.getBuffer().position() + data_length);
            } else if (name.equalsIgnoreCase("playerClassTag")) {
                String value = readString(this.getBuffer());
                headerInfo.setPlayerClassTag(value);
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
            } else if (name.equalsIgnoreCase("playerLevel")) {
                int value = this.getBuffer().getInt();
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
                headerInfo.setPlayerLevel(value);
            } else if (name.equalsIgnoreCase("playerVersion")) {
                int value = this.getBuffer().getInt();
                if (DBG) Util.log(String.format("name=%s; value=%s", name, value));
                headerInfo.setPlayerVersion(value);
            }
        }
        return headerInfo;
    }

    private String readString(ByteBuffer byteBuffer) {
        return this.readString(byteBuffer, false);
    }

    private String readString(ByteBuffer byteBuffer, boolean utf16le) {

        try {
            int len = byteBuffer.getInt();
            if (len <= 0) {
                return null;
            }
            if (utf16le) {
                len *= 2;
            }
            byte buf[] = new byte[len];

            byteBuffer.get(buf, 0, len);
            String value = new String(buf, "UTF-8");
            return new String(value.getBytes(), "Windows-1252");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int searchBlockTag(String tag, int offset) {
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

}

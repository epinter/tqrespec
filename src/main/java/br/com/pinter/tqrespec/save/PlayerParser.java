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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Hashtable;

public class PlayerParser {
    private final static boolean DBG = false;
    private String player = null;
    private boolean customQuest = false;

    private int inventoryStart = -1;

    private Hashtable<String, byte[]> blockTag = new Hashtable<String, byte[]>() {{
        put("begin_block", new byte[]{0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
        put("end_block", new byte[]{0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B});
    }};

    public void parse() throws Exception {
        PlayerData.getInstance().reset();
        this.loadPlayerChr();

        if (this.getBuffer() == null || this.getBuffer().capacity() <= 50) {
            throw new IOException("Can't read Player.chr from player " + this.player);
        }
        if (DBG) Util.log(String.format("File '%s' loaded, size=%d",
                this.player, this.getBuffer().capacity()));
        HeaderInfo headerInfo = parseHeader();

        PlayerData.getInstance().setHeaderInfo(headerInfo);

        if (headerInfo.getHeaderVersion() != 2) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (headerVersion must be == 2)", this.player));
        }
        if (headerInfo.getPlayerVersion() != 5) {
            throw new IncompatibleSavegameException(
                    String.format("Incompatible player '%s' (playerVersion must be == 5)", this.player));
        }
        Hashtable<Integer, BlockInfo> blocks = this.parseAllBlocks();
        PlayerData.getInstance().setBlockInfo(blocks);
        if (inventoryStart == -1) {
            this.parseFooter();
        }
        this.prepareBufferForRead();
        PlayerData.getInstance().setPlayerName(player);
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

    private void loadPlayerChr() throws Exception {
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
                String.format("\\_%s\\Player.chr", this.player));

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

            if (DBG) Util.log("File read to buffer: " + this.getBuffer());

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

    private Hashtable<String, VariableInfo> parseBlock(BlockInfo blockInfo) {
        Hashtable<String, VariableInfo> ret = new Hashtable<>();

        this.getBuffer().position(blockInfo.getStart() + getBlockTagSize("begin_block"));

        int keyread = 0;
        int valread = 0;

        ArrayList<VariableInfo> temp = new ArrayList<>();

        while (this.getBuffer().position() <= (blockInfo.getEnd() - getBlockTagSize("end_block"))) {
            int dataOffset = 0;
            int keyOffset = getBuffer().position();
            String name = readString(getBuffer());
            keyread++;

            if (StringUtils.isEmpty(name)) {
                continue;
            }

            if (StringUtils.isEmpty(name)) {
                if (DBG)
                    Util.log(String.format("empty name at block %d pos %d", blockInfo.getStart(), getBuffer().position()));
            }

            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setValOffset(getBuffer().position());
            variableInfo.setVariableType(VariableInfo.VariableType.Unknown);
            variableInfo.setKeyOffset(keyOffset);
            variableInfo.setName(name);

            if (name.equalsIgnoreCase("begin_block")) {
                dataOffset -= getBlockTagSize("begin_block");
                valread++;
            } else if (name.equalsIgnoreCase("end_block")) {
                dataOffset -= getBlockTagSize("end_block");
                valread++;
                if (temp.size() == 1) {
                    VariableInfo endBlockVar = temp.get(temp.size() - 1);
                    endBlockVar.setName("difficulty");
                    ret.put(endBlockVar.getName(), endBlockVar);
                    putVarIndex(endBlockVar.getName(), blockInfo.getStart());
                    int diffInt = this.getBuffer().getInt(endBlockVar.getValOffset());
                    endBlockVar.setValue(diffInt);
                    endBlockVar.setVariableType(VariableInfo.VariableType.Integer);
                    if (DBG)
                        Util.log(String.format("blockStart: %d; variableInfo: %s; data_offset=%d", blockInfo.getStart(), endBlockVar.toString(), dataOffset));
                }
            } else if (name.equalsIgnoreCase("myPlayerName")
                    || name.equalsIgnoreCase("defaultText")
                    || name.equalsIgnoreCase("(*greatestMonsterKilledName)[i]")) {
                this.readString(variableInfo, this.getBuffer(), true);
                if (variableInfo.getValSize() >= 0) {
                    valread++;
                }
            } else if (name.equalsIgnoreCase("playerTexture")
                    || name.equalsIgnoreCase("skillName")) {
                this.readString(variableInfo, this.getBuffer());
                if (variableInfo.getValSize() >= 0) {
                    valread++;
                }
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
                    || name.equalsIgnoreCase("var1")
                    || name.equalsIgnoreCase("storedType")) {
                variableInfo.setVariableType(VariableInfo.VariableType.Integer);
                this.readInt(variableInfo);
                if (variableInfo.getValSize() >= 0) {
                    valread++;
                }
            } else if (name.equalsIgnoreCase("teleportUID")
                    || name.equalsIgnoreCase("respawnUID")
                    || name.equalsIgnoreCase("markerUID")
                    || name.equalsIgnoreCase("strategicMovementRespawnPoint[i]")
            ) {
                variableInfo.setVariableType(VariableInfo.VariableType.UID);
                dataOffset = 16;
                variableInfo.setValSize(16);
                valread++;
            } else if (name.equalsIgnoreCase("currentStats.experiencePoints")
                    || name.equalsIgnoreCase("currentStats.charLevel")
                    || name.equalsIgnoreCase("modifierPoints")
                    || name.equalsIgnoreCase("versionRespawnPoint")
                    || name.equalsIgnoreCase("money")) {
                this.readInt(variableInfo);
                if (variableInfo.getValSize() >= 0) {
                    valread++;
                }
            } else if (name.equalsIgnoreCase("itemPositionsSavedAsGridCoords")) {
                //inventory
                if (Constants.SKIP_INVENTORY_BLOCKS) {
                    inventoryStart = variableInfo.getValOffset();
                }
                break;
            } else if (name.equalsIgnoreCase("temp")) {
                this.readFloat(variableInfo);
                if (variableInfo.getValSize() >= 0) {
                    valread++;
                }
            } else {
                variableInfo.setVariableType(VariableInfo.VariableType.Integer);
                dataOffset = this.getBuffer().getInt();
                variableInfo.setValSize(dataOffset);
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

            if (variableInfo.getValSize() >= 0) {
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
                        if (DBG)
                            Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("str").toString()));
                        if (DBG)
                            Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("dex").toString()));
                        if (DBG)
                            Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("int").toString()));
                        if (DBG)
                            Util.log(String.format("blockStart: %d; variableInfo: %s;", blockInfo.getStart(), ret.get("life").toString()));
                    } else {
                        temp.add(variableInfo);
                    }
                    if (DBG) Util.log(variableInfo.getName());
                }

                if (!variableInfo.getName().equals("temp")) {
                    ret.put(variableInfo.getName(), variableInfo);
                    putVarIndex(variableInfo.getName(), blockInfo.getStart());
                    if (DBG)
                        Util.log(String.format("blockStart: %d; variableInfo: %s; data_offset=%d", blockInfo.getStart(), variableInfo.toString(), dataOffset));
                }
            } else {
                if (DBG)
                    Util.log(String.format("IGNORED %s", variableInfo));
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
        if (PlayerData.getInstance().getVariableLocation().get(varName) == null) {
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

    private void readString(VariableInfo variableInfo, ByteBuffer byteBuffer) {
        this.readString(variableInfo, byteBuffer, false);
    }

    private void readString(VariableInfo variableInfo, ByteBuffer byteBuffer, boolean utf16le) {
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

            byte buf[] = new byte[len];

            byteBuffer.get(buf, 0, len);

            variableInfo.setValue(new String(buf, utf16le ? "UTF-16LE" : "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readInt(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            System.err.println("BUG: variable size != 0");
            return;
        }

        variableInfo.setValue(getBuffer().getInt());
        variableInfo.setVariableType(VariableInfo.VariableType.Integer);
        variableInfo.setValSize(4);
    }

    private void readFloat(VariableInfo variableInfo) {
        if (variableInfo.getValSize() != -1) {
            System.err.println("BUG: variable size != 0");
            return;
        }

        variableInfo.setValue(getBuffer().getFloat());
        variableInfo.setVariableType(VariableInfo.VariableType.Float);
        variableInfo.setValSize(4);
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
            return new String(buf, utf16le ? "UTF-16LE" : "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPlayer() {
        return player;
    }

    public PlayerParser player(String player) {
        this.player = player;
        return this;
    }

    public boolean isCustomQuest() {
        return customQuest;
    }

    public PlayerParser customQuest(boolean customQuest) {
        this.customQuest = customQuest;
        return this;
    }
}

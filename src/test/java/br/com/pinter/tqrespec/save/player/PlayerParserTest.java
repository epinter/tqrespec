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

import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.core.InjectionContext;
import br.com.pinter.tqrespec.save.UID;
import br.com.pinter.tqrespec.save.VariableInfo;
import br.com.pinter.tqrespec.save.VariableType;
import br.com.pinter.tqrespec.tqdata.DefaultMapTeleport;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.GameVersion;
import br.com.pinter.tqrespec.tqdata.MapTeleport;
import br.com.pinter.tqrespec.util.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameInfo.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PlayerParserTest {
    private static final Logger logger = Logger.getLogger(PlayerParserTest.class.getName());

    private InjectionContext injectionContext = new InjectionContext(this,
            Collections.singletonList(new GuiceModule()));

    @Inject
    private CurrentPlayerData saveData;

    @Inject
    private Player player;

    private PlayerParser playerParser;

    @Before
    public void setUp() throws IOException {
        injectionContext.initialize();
        File playerChr = new File("src/test/resources/_savegame/Player.chr");
        if (!playerChr.exists()) {
            throw new IOException(String.format("File %s is missing," +
                    " copy the savegame to execute the tests", playerChr));
        }

        playerParser = new PlayerParser(playerChr,
                "savegame");
    }

    @Test
    public void parseAllBlocks_Should_parseAllBlocksFromSavegame() {
        try {
            playerParser.fillBuffer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail("parseAllBlocks: readPlayerChr() failed");
        }


        try {
            playerParser.fillBuffer();
            playerParser.buildBlocksTable();
            playerParser.prepareForParse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }

        playerParser.parseAllBlocks();
        assertNotNull(playerParser.getBlockInfo());
        assertFalse(playerParser.getBlockInfo().isEmpty());
        assertTrue(playerParser.getBlockInfo().size() > 1);

    }

    private void parse() {
        try {
            saveData.reset();
            playerParser.parse();
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    public void readGender_Should_readGenderFromSaveGame() {
        parse();

        String playerCharacterClass = saveData.getPlayerCharacterClass();
        assertNotNull(playerCharacterClass);
        assertTrue(playerCharacterClass.equals("Warrior") || playerCharacterClass.equals("Sorceress"));
    }

    @Test
    public void prepareBufferForRead_Should_rewindBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }
        playerParser.prepareBufferForRead();
        assertEquals(0, playerParser.getBuffer().position());
    }

    @Test
    public void getBuffer_Should_returnByteBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }
        assertNotNull(playerParser.getBuffer());
        assertTrue(playerParser.getBuffer().capacity() > 0);
    }

    @Test
    public void getVariableLocation_Should_returnVariableOffsetList() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }
        ConcurrentMap<String, List<Integer>> variableLocation = playerParser.getVariableLocation();
        assertNotNull(variableLocation);
        assertFalse(variableLocation.isEmpty());
    }

    @Test
    public void parse_Should_parseWholeSavegame() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    public void parseHeader_Should_parseFileHeader() {
        try {
            playerParser.fillBuffer();
            playerParser.buildBlocksTable();
            playerParser.prepareForParse();
            playerParser.prepareBufferForRead();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }

        HeaderInfo headerInfo = playerParser.parseHeader();
        assertNotNull(headerInfo);
        assertTrue(headerInfo.getHeaderVersion() == GameVersion.TQIT || headerInfo.getHeaderVersion() == GameVersion.TQAE);
        assertTrue(headerInfo.getPlayerVersion() > 0);
        assertTrue(headerInfo.getPlayerLevel() > 0);
    }

    @Test
    public void readDifficulty_Should_readLegendaryDifficultyFromSaveGame() {
        parse();

        List<VariableInfo> variableInfoList = player.getTempVariableInfo("difficulty");
        Assert.assertEquals(VariableType.INTEGER, variableInfoList.get(0).getVariableType());
        Integer difficulty = (Integer) variableInfoList.get(0).getValue();

        assertEquals(2, difficulty, 0);
    }

    @Test
    public void readStr_Should_readStrFromSavegame() {
        float str = readFloatTempVar("str");
        assertEquals(54.0, str, 0);

    }

    @Test
    public void readIntl_Should_readStrFromSavegame() {
        float intl = readFloatTempVar("int");
        assertEquals(622.0, intl, 0);

    }

    @Test
    public void readDex_Should_readStrFromSavegame() {
        float dex = readFloatTempVar("dex");
        assertEquals(102.0, dex, 0);

    }

    @Test
    public void readMana_Should_readStrFromSavegame() {
        float mana = readFloatTempVar("mana");
        assertEquals(340.0, mana, 0);
    }

    @Test
    public void readLife_Should_readStrFromSavegame() {
        float life = readFloatTempVar("life");
        assertEquals(460.0, life, 0);
    }

    @Test
    public void readAvailableAttributePoints_Should_readAvailableAttributePointsFromSavegame() {
        float available = readIntegerVar("modifierPoints");
        assertEquals(8.0, available, 0);
    }

    @Test
    public void readXp_Should_readXpFromSavegame() {
        float xp = readIntegerVar("currentStats.experiencePoints");
        assertEquals(160547234, xp, 0);
    }

    @Test
    public void readLevel_Should_readLevelFromSavegame() {
        float level = readIntegerVar("currentStats.charLevel");
        assertEquals(75, level, 0);
    }

    @Test
    public void readClass_Should_readClassFromSavegame() {
        parse();

        assertEquals("tagCClass27", player.getSaveData().getHeaderInfo().getPlayerClassTag());
    }

    @Test
    public void readPlayerName_Should_readPlayerNameFromSavegame() {
        assertEquals("teste4", readStringVar("myPlayerName"));
    }

    @Test
    public void matchTeleports_Should_matchTeleportsFromSavegame() {
        parse();
        List<TeleportDifficulty> saveTeleports = player.getTeleports();
        assertEquals(3, saveTeleports.size());
        Map<Integer, List<UID>> teleports = new HashMap<>();

        final String helos = "4136144580-999965812-3093316465-1160239764";
        final String hades = "137995607-2789228967-2353681595-2123008457";

        boolean helosNormal = false;
        boolean helosEpic = false;
        boolean helosLegendary = false;
        boolean hadesNormal = false;
        boolean hadesEpic = false;
        boolean hadesLegendary = false;
        for (TeleportDifficulty t: saveTeleports) {
            assertTrue(t.getTeleportList().size() > 0);
            for (VariableInfo v: t.getTeleportList()) {
                UID uid = new UID((byte[]) v.getValue());
                switch (uid.getUid()) {
                    case helos -> {
                        helosNormal = t.getDifficulty() == 0 || helosNormal;
                        helosEpic = t.getDifficulty() == 1 || helosEpic;
                        helosLegendary = t.getDifficulty() == 2 || helosLegendary;
                    }
                    case hades -> {
                        hadesNormal = t.getDifficulty() == 0 || hadesNormal;
                        hadesEpic = t.getDifficulty() == 1 || hadesEpic;
                        hadesLegendary = t.getDifficulty() == 2 || hadesLegendary;
                    }
                    default -> {
                        //ignored
                        }
                }
                teleports.computeIfAbsent(t.getDifficulty(), k -> new ArrayList<>());
                if(!teleports.get(t.getDifficulty()).contains(uid)) {
                    teleports.get(t.getDifficulty()).add(uid);
                }
            }
        }
        assertEquals(30,teleports.get(0).size());
        assertEquals(30,teleports.get(1).size());
        assertEquals(38,teleports.get(2).size());
        assertTrue(hadesNormal && hadesEpic && hadesLegendary && helosNormal && helosEpic && helosLegendary);
    }

    @Test
    public void readMonsterName_Should_readMonsterNameFromSavegame() {
        assertEquals("{^r}Hades ~ God of the Dead", readStringVar(PlayerFileVariable.valueOf("greatestMonsterKilledName").var()));
    }

    private float readFloatTempVar(String alias) {
        parse();

        List<VariableInfo> variableInfoList = player.getTempVariableInfo(alias);
        Assert.assertEquals(VariableType.FLOAT, variableInfoList.get(0).getVariableType());
        return (Float) variableInfoList.get(0).getValue();
    }

    private int readIntegerVar(String name) {
        parse();

        return saveData.getDataMap().getInt(name);
    }

    private String readStringVar(String name) {
        parse();

        return saveData.getDataMap().getString(name);
    }

}
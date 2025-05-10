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
import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.UID;
import br.com.pinter.tqrespec.save.VariableInfo;
import br.com.pinter.tqrespec.tqdata.GameVersion;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class PlayerParserTest {
    private static final System.Logger logger = Log.getLogger(PlayerParserTest.class.getName());
    private Injector injector;
    private PlayerParser playerParser;

    @Mock
    private CurrentPlayerData mockSaveData;

    @Inject
    @InjectMocks
    private CurrentPlayerData saveData;

    @InjectMocks
    private PlayerLoader player;


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        try {
            injector = GuiceInjector.init(this);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        File playerChr = new File("src/test/resources/_savegame/Player.chr");
        if (!playerChr.exists()) {
            throw new IOException(String.format("File %s is missing," +
                    " copy the savegame to execute the tests", playerChr));
        }

        playerParser = new PlayerParser(playerChr,
                "savegame");
    }

    @Test
    void parseAllBlocks_Should_parseAllBlocksFromSavegame() {
        try {
            playerParser.fillBuffer();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            fail("parseAllBlocks: readPlayerChr() failed");
        }


        try {
            playerParser.fillBuffer();
            playerParser.buildBlocksTable();
            playerParser.prepareForParse();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }

        playerParser.parseAllBlocks();

        assertNotNull(playerParser.getBlockInfo());
        assertFalse(playerParser.getBlockInfo().isEmpty());
        assertTrue(playerParser.getBlockInfo().size() > 1);

    }

    private void parse() {
        try {
            saveData.reset();
            saveData.setBuffer(playerParser.load());
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    void readGender_Should_readGenderFromSaveGame() {
        parse();

        String playerCharacterClass = saveData.getPlayerCharacterClass();
        assertNotNull(playerCharacterClass);
        assertTrue(playerCharacterClass.equals("Warrior") || playerCharacterClass.equals("Sorceress"));
    }

    @Test
    void prepareBufferForRead_Should_rewindBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
        playerParser.prepareBufferForRead();
        assertEquals(0, playerParser.getBuffer().position());
    }

    @Test
    void getBuffer_Should_returnByteBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
        assertNotNull(playerParser.getBuffer());
        assertTrue(playerParser.getBuffer().capacity() > 0);
    }

    @Test
    void getVariableLocation_Should_returnVariableOffsetList() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
        ConcurrentMap<String, List<Integer>> variableLocation = playerParser.getVariableLocation();
        assertNotNull(variableLocation);
        assertFalse(variableLocation.isEmpty());
    }

    @Test
    void parse_Should_parseWholeSavegame() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    void parseHeader_Should_parseFileHeader() {
        try {
            playerParser.fillBuffer();
            playerParser.buildBlocksTable();
            playerParser.prepareForParse();
            playerParser.prepareBufferForRead();

            HeaderInfo headerInfo = playerParser.parseHeader();
            assertNotNull(headerInfo);
            assertTrue(headerInfo.getHeaderVersion() == GameVersion.TQIT || headerInfo.getHeaderVersion() == GameVersion.TQAE);
            assertEquals(9, headerInfo.getPlayerVersion());
            assertTrue(headerInfo.getPlayerLevel() > 0);
        } catch (Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }

    }

    @Test
    void readDifficulty_Should_readLegendaryDifficultyFromSaveGame() {
        assertEquals(2, readTempVar("difficulty"), 0);
    }

    @Test
    void readStr_Should_readStrFromSavegame() {
        assertEquals(54, readTempVar("str"), 0);

    }

    @Test
    void readIntl_Should_readStrFromSavegame() {
        assertEquals(622, readTempVar("int"), 0);

    }

    @Test
    void readDex_Should_readStrFromSavegame() {
        assertEquals(102, readTempVar("dex"), 0);
    }

    @Test
    void readMana_Should_readStrFromSavegame() {
        assertEquals(340, readTempVar("mana"), 0);
    }

    @Test
    void readLife_Should_readStrFromSavegame() {
        assertEquals(460, readTempVar("life"), 0);
    }

    @Test
    void readAvailableAttributePoints_Should_readAvailableAttributePointsFromSavegame() {
        assertEquals(8, readIntegerVar("modifierPoints"), 0);
    }

    @Test
    void readXp_Should_readXpFromSavegame() {
        assertEquals(160547234, readIntegerVar("currentStats.experiencePoints"), 0);
    }

    @Test
    void readLevel_Should_readLevelFromSavegame() {
        assertEquals(75, readIntegerVar("currentStats.charLevel"), 0);
    }

    @Test
    void readClass_Should_readClassFromSavegame() {
        parse();

        Mockito.when(mockSaveData.getHeaderInfo()).thenReturn(saveData.getHeaderInfo());
        assertEquals("tagCClass27", player.getSaveData().getHeaderInfo().getPlayerClassTag());
    }

    @Test
    void readPlayerName_Should_readPlayerNameFromSavegame() {
        assertEquals("teste4", readStringVar("myPlayerName"));
    }

    @Test
    void matchTeleports_Should_matchTeleportsFromSavegame() {
        parse();
        injector.injectMembers(player);

        List<TeleportDifficulty> saveTeleports = player.getTeleportDifficulty();
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
        for (TeleportDifficulty t : saveTeleports) {
            assertTrue(t.getVariables().size() > 0);
            for (VariableInfo v : t.getVariables()) {
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
                if (!teleports.get(t.getDifficulty()).contains(uid)) {
                    teleports.get(t.getDifficulty()).add(uid);
                }
            }
        }
        assertEquals(30, teleports.get(0).size());
        assertEquals(30, teleports.get(1).size());
        assertEquals(38, teleports.get(2).size());
        assertTrue(hadesNormal && hadesEpic && hadesLegendary && helosNormal && helosEpic && helosLegendary);
    }

    @Test
    void readMonsterName_Should_readMonsterNameFromSavegame() {
        assertEquals("{^r}Hades ~ God of the Dead", readStringVar(PlayerFileVariable.valueOf(Platform.WINDOWS, "greatestMonsterKilledName").variable()));
    }

    private int readTempVar(String alias) {
        parse();

        return saveData.getDataMap().getTempAttr(alias);
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
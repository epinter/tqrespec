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

import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.stash.StashData;
import br.com.pinter.tqrespec.save.stash.StashLoader;
import br.com.pinter.tqrespec.save.stash.StashWriter;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlayerWriterTest {
    private static final Logger logger = Logger.getLogger(PlayerWriterTest.class.getName());

    @Mock
    private CurrentPlayerData mockSaveData;

    @InjectMocks
    private CurrentPlayerData saveData;

    private PlayerParser playerParser;

    @Mock
    GameInfo gameInfo;

    @InjectMocks
    private PlayerWriter playerWriter;

    @BeforeEach
    void setUp() throws IOException {
        File playerChr = new File("src/test/resources/_savegame/Player.chr");
        if (!playerChr.exists()) {
            throw new IOException(String.format("File %s is missing," +
                    " copy the savegame to execute the tests", playerChr));
        }

        playerParser = new PlayerParser(playerChr,
                "savegame");
    }

    @Test
    void copyAndSet_Should_copySavegameAndTestNewValue() {
        try {
            saveData.reset();
            saveData.setPlayerName("savegame");
            saveData.setBuffer(playerParser.load());
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        MockitoAnnotations.openMocks(this);

        try {
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/winsys.dxg"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/winsys.dxb"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/Player.chr"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mockito.when(mockSaveData.getPlayerPath()).thenReturn(Path.of(String.format("%s/_%s","src/test/resources", "savegame")));
        Mockito.when(mockSaveData.getPlayerName()).thenReturn(saveData.getPlayerName());
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int isInMainQuestBefore = saveData.getDataMap().getInt("isInMainQuest");
        assertEquals(1, isInMainQuestBefore);
        saveData.getDataMap().setInt("isInMainQuest", 0);

        try {
            playerWriter.copyCurrentSave("testcopy");
        } catch (IOException e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        File testcopyChr = new File("src/test/resources/_testcopy/Player.chr");
        playerParser = new PlayerParser(testcopyChr,
                "testcopy");

        try {
            saveData.reset();
            saveData.setPlayerName("testcopy");
            saveData.setBuffer(playerParser.load());
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
            int isInMainQuestAfter = saveData.getDataMap().getInt("isInMainQuest");
            assertTrue(isInMainQuestBefore != isInMainQuestAfter && isInMainQuestAfter == 0);

        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    private void prepareCopySavegame() {
        try {
            saveData.reset();
            saveData.setPlayerName("savegame");
            saveData.setBuffer(playerParser.load());
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        MockitoAnnotations.openMocks(this);

        try {
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/winsys.dxg"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/winsys.dxb"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/Player.chr"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Mockito.when(mockSaveData.getPlayerPath()).thenReturn(Path.of(String.format("%s/_%s","src/test/resources", "savegame")));
        Mockito.when(mockSaveData.getPlayerName()).thenReturn(saveData.getPlayerName());
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());
    }

    private void copyAndParseSavegame() {
        copyAndParseSavegame(Platform.UNDEFINED);
    }

    private void copyAndParseSavegame(Platform conversionTarget) {
        try {
            playerWriter.copyCurrentSave("testcopy", conversionTarget, null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        File testcopyChr = new File("src/test/resources/_testcopy/Player.chr");
        playerParser = new PlayerParser(testcopyChr,
                "testcopy");

        try {
            saveData.reset();
            saveData.setPlayerName("testcopy");
            saveData.setBuffer(playerParser.load());
            saveData.getDataMap().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getDataMap().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    void readStashFname_Should_ReadStashFnameFromSaveGame() {
        prepareCopySavegame();

        copyAndParseSavegame();

        StashData stashData = null;
        StashLoader stashLoader = new StashLoader();
        if (stashLoader.loadStash(Paths.get("src/test/resources/_testcopy"), "testcopy")) {
            stashData = stashLoader.getSaveData();
            StashWriter stashWriter = new StashWriter(stashData);
            stashWriter.save();
        } else {
            fail();
        }

        assertTrue("src\\test\\resources\\_testcopy\\winsys.dxb".equals(stashData.getDataMap().getString("fName"))
                || "src\\test\\resources\\_testcopy/winsys.dxb".equals(stashData.getDataMap().getString("fName")));
    }

    @Test
    void writeGender_Should_writeAndReadGenderFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setString(Constants.Save.PLAYER_CHARACTER_CLASS, "XXGenderX");
        saveData.getDataMap().setString(Constants.Save.PLAYER_TEXTURE, "XXTextureX");

        copyAndParseSavegame();

        String playerCharacterClass = saveData.getPlayerCharacterClass();
        String texture = saveData.getDataMap().getString(Constants.Save.PLAYER_TEXTURE);

        assertNotNull(playerCharacterClass);
        assertTrue(playerCharacterClass.equals("XXGenderX") && texture.equals("XXTextureX"));
    }

    @Test
    void writeSaveId_Should_writeAndSaveIdFromSaveGame() {
        prepareCopySavegame();

        copyAndParseSavegame(Platform.MOBILE);

        String parsedSaveId = saveData.getDataMap().getString("mySaveId");

        assertTrue(StringUtils.isNotBlank(parsedSaveId));
    }

    @Test
    void writePoints_Should_writeAndReadPointsFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setInt("modifierPoints", 121);

        copyAndParseSavegame();

        assertEquals(121, saveData.getDataMap().getInt("modifierPoints"), 0);
    }

    @Test
    void writeStr_Should_writeAndReadStrFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setTempAttr("str", 300);

        copyAndParseSavegame();

        assertEquals(300, saveData.getDataMap().getTempAttr("str"), 0);
    }

    @Test
    void writeIntl_Should_writeAndReadIntlFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setTempAttr("int", 320);

        copyAndParseSavegame();

        assertEquals(320, saveData.getDataMap().getTempAttr("int"), 0);
    }

    @Test
    void writeDex_Should_writeAndReadDexFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setTempAttr("dex", 350);

        copyAndParseSavegame();

        assertEquals(350, saveData.getDataMap().getTempAttr("dex"), 0);
    }

    @Test
    void writeLife_Should_writeAndReadLifeFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setTempAttr("life", 420);

        copyAndParseSavegame();

        assertEquals(420, saveData.getDataMap().getTempAttr("life"), 0);
    }

    @Test
    void writeMana_Should_writeAndReadLifeFromSaveGame() {
        prepareCopySavegame();

        saveData.getDataMap().setTempAttr("mana", 30000);

        copyAndParseSavegame();

        assertEquals(30000, saveData.getDataMap().getTempAttr("mana"), 0);
    }

    @Test
    void readPlayerName_Should_writeAndReadPlayerNameFromSaveGame() {
        prepareCopySavegame();

        copyAndParseSavegame();

        assertEquals("testcopy", saveData.getDataMap().getCharacterName());
    }

    @Test
    void readPlayerNameMobile_Should_writeAndReadPlayerNameMobileFromSaveGame() {
        prepareCopySavegame();

        copyAndParseSavegame(Platform.MOBILE);

        assertEquals("testcopy", saveData.getDataMap().getCharacterName());
    }


}
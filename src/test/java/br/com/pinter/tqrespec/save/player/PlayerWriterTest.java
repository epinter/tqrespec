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
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GameInfo.class, Util.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PlayerWriterTest {
    private static final Logger logger = Logger.getLogger(PlayerWriterTest.class.getName());

    private final InjectionContext injectionContext = new InjectionContext(this,
            Collections.singletonList(new GuiceModule()));

    @Mock
    private CurrentPlayerData mockSaveData;

    @InjectMocks
    private CurrentPlayerData saveData;

    private PlayerParser playerParser;

    @Mock
    GameInfo gameInfo;

    @InjectMocks
    private PlayerWriter playerWriter;

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
    public void copyAndSet_Should_copySavegameAndTestNewValue() {
        try {
            saveData.reset();
            saveData.setPlayerName("savegame");
            saveData.setBuffer(playerParser.loadPlayer());
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        MockitoAnnotations.openMocks(this);

        try {
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/Player.chr"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PowerMockito.when(gameInfo.getSaveDataMainPath()).thenReturn("src/test/resources");
        PowerMockito.when(gameInfo.getSaveDataUserPath()).thenReturn("src/test/resources");

        PowerMockito.when(mockSaveData.getPlayerName()).thenReturn(saveData.getPlayerName());
        PowerMockito.when(mockSaveData.getHeaderInfo()).thenReturn(saveData.getHeaderInfo());
        PowerMockito.when(mockSaveData.isCustomQuest()).thenReturn(saveData.isCustomQuest());
        PowerMockito.when(mockSaveData.getChanges()).thenReturn(saveData.getChanges());
        PowerMockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int isInMainQuestBefore = saveData.getChanges().getInt("isInMainQuest");
        Assert.assertEquals(1,isInMainQuestBefore);
        saveData.getChanges().setInt("isInMainQuest", 0);

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
            saveData.setBuffer(playerParser.loadPlayer());
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
            int isInMainQuestAfter = saveData.getChanges().getInt("isInMainQuest");
            Assert.assertTrue(isInMainQuestBefore != isInMainQuestAfter && isInMainQuestAfter == 0);

        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }

    @Test
    public void writeGender_Should_writeAndReadGenderFromSaveGame() {
        try {
            saveData.reset();
            saveData.setPlayerName("savegame");
            saveData.setBuffer(playerParser.loadPlayer());
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        MockitoAnnotations.openMocks(this);

        try {
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy/Player.chr"));
            Files.deleteIfExists(Paths.get("src/test/resources/_testcopy"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PowerMockito.when(gameInfo.getSaveDataMainPath()).thenReturn("src/test/resources");
        PowerMockito.when(gameInfo.getSaveDataUserPath()).thenReturn("src/test/resources");

        PowerMockito.when(mockSaveData.getPlayerName()).thenReturn(saveData.getPlayerName());
        PowerMockito.when(mockSaveData.getHeaderInfo()).thenReturn(saveData.getHeaderInfo());
        PowerMockito.when(mockSaveData.isCustomQuest()).thenReturn(saveData.isCustomQuest());
        PowerMockito.when(mockSaveData.getChanges()).thenReturn(saveData.getChanges());
        PowerMockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        saveData.getChanges().setString(Constants.Save.PLAYER_CHARACTER_CLASS, "XXGenderX");
        saveData.getChanges().setString(Constants.Save.PLAYER_TEXTURE, "XXTextureX");

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
            saveData.setBuffer(playerParser.loadPlayer());
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
            String playerCharacterClass = saveData.getPlayerCharacterClass();
            String texture = saveData.getChanges().getString(Constants.Save.PLAYER_TEXTURE);

            assertNotNull(playerCharacterClass);
            assertTrue(playerCharacterClass.equals("XXGenderX") && texture.equals("XXTextureX"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }
    }
}
/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GameInfo.class, Util.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PlayerWriterTest {
    private static final Logger logger = Logger.getLogger(PlayerWriterTest.class.getName());

    private InjectionContext injectionContext = new InjectionContext(this,
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

        MockitoAnnotations.initMocks(this);

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
}
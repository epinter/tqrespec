/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.core.InjectionContext;
import br.com.pinter.tqrespec.save.VariableInfo;
import br.com.pinter.tqrespec.save.VariableType;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.GameVersion;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
            playerParser.readPlayerChr();
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

    @Test
    public void readFloat_Should_readFloatFromSavegame() {
        try {
            playerParser.parse();
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            fail();
        }

        List<VariableInfo> variableInfoList = player.getTempVariableInfo("str");
        Assert.assertEquals(VariableType.FLOAT, variableInfoList.get(0).getVariableType());
        Float str = (Float) variableInfoList.get(0).getValue();
        assertNotNull(str);
        assertTrue(str > 0.0);
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
            playerParser.readPlayerChr();
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
}
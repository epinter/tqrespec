/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.core.InjectionContext;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.util.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameInfo.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PlayerParserTest {
    private static final Logger logger = Log.getLogger();


//    @Rule
//    public WeldInitiator weld = WeldInitiator.from(
//            SaveData.class,
//            PlayerParser.class,
//            PlayerData.class,
//            ChangesTable.class
//    ).inject(this).build();
//

    private InjectionContext injectionContext = new InjectionContext(this,
            Collections.singletonList(new GuiceModule()));

    @Inject
    private SaveData saveData;

    @Inject
    private PlayerParser playerParser;

    @Before
    public void setUp() throws IOException {
        injectionContext.initialize();

        if (!new File("src/test/resources/_savegame/Player.chr").exists()) {
            throw new IOException("File src/test/resources/_savegame/Player.chr is missing," +
                    " copy the savegame to execute the tests");
        }

        GameInfo gameInfo = PowerMockito.mock(GameInfo.class);

        PowerMockito.mockStatic(GameInfo.class);
        PowerMockito.when(GameInfo.getInstance()).thenReturn(gameInfo);
        PowerMockito.when(gameInfo.getSaveDataMainPath()).thenReturn("src/test/resources");
        playerParser.setPlayer("savegame");
    }

    @Test
    public void parseAllBlocks_Should_parseAllBlocksFromSavegame() {
        try {
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
            saveData.setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.setVariableLocation(playerParser.getVariableLocation());
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }

        int varLocation = saveData.getVariableLocation().get("str").get(0);
        assertTrue(varLocation > 0);
        BlockInfo blockInfo = saveData.getBlockInfo().get(varLocation);
        assertNotNull(blockInfo);
        assertEquals(VariableType.FLOAT, blockInfo.getVariables().get("str").getVariableType());
        Float str = (Float) blockInfo.getVariables().get("str").getValue();
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
        assertEquals(0,playerParser.getBuffer().position());
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
        ConcurrentHashMap<String, ArrayList<Integer>> variableLocation = playerParser.getVariableLocation();
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
        assertTrue(headerInfo.getHeaderVersion() > 0);
        assertTrue(headerInfo.getPlayerVersion() > 0);
        assertTrue(headerInfo.getPlayerLevel() > 0);
    }
}
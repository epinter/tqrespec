/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.tqdata.GameInfo;
import org.junit.Before;
import org.junit.Rule;
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
import java.util.Hashtable;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GameInfo.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "com.sun.org.apache.xalan.*"})
public class PlayerParserTest {
//    @Rule
//    public WeldInitiator weld = WeldInitiator.from(
//            SaveData.class,
//            PlayerParser.class,
//            PlayerData.class,
//            ChangesTable.class
//    ).inject(this).build();
//
    @Inject
    private SaveData saveData;

    @Inject
    private PlayerParser playerParser;

    @Before
    public void setUp() throws IOException {
        if(!new File("src/test/resources/_savegame/Player.chr").exists()) {
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
            e.printStackTrace();
            fail("parseAllBlocks: readPlayerChr() failed");
        }
        Hashtable<Integer, BlockInfo> blocks = playerParser.parseAllBlocks();
        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.size() > 1);

    }

    @Test
    public void readFloat_Should_readFloatFromSavegame() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int varLocation = saveData.getVariableLocation().get("str").get(0);
        assertTrue(varLocation > 0);
        BlockInfo blockInfo = saveData.getBlockInfo().get(varLocation);
        assertNotNull(blockInfo);
        assertEquals(blockInfo.getVariables().get("str").getVariableType(), VariableInfo.VariableType.Float);
        Float str = (Float) blockInfo.getVariables().get("str").getValue();
        assertNotNull(str);
        assertTrue(str > 0.0);
    }

    @Test
    public void prepareBufferForRead_Should_rewindBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerParser.prepareBufferForRead();
        assertEquals(playerParser.getBuffer().position(), 0);
    }

    @Test
    public void getBuffer_Should_returnByteBuffer() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(playerParser.getBuffer());
        assertTrue(playerParser.getBuffer().capacity() > 0);
    }

    @Test
    public void getVariableLocation_Should_returnVariableOffsetList() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Hashtable<String, ArrayList<Integer>> variableLocation = playerParser.getVariableLocation();
        assertNotNull(variableLocation);
        assertFalse(variableLocation.isEmpty());
    }

    @Test
    public void parse_Should_parseWholeSavegame() {
        try {
            playerParser.parse();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void parseHeader_Should_parseFileHeader() {
        try {
            playerParser.readPlayerChr();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HeaderInfo headerInfo = playerParser.parseHeader();
        assertNotNull(headerInfo);
        assertTrue(headerInfo.getHeaderVersion() > 0);
        assertTrue(headerInfo.getPlayerVersion() > 0);
        assertTrue(headerInfo.getPlayerLevel() > 0);
    }
}
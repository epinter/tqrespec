/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.UID;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.DefaultMapTeleport;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PlayerTest {
    private static final System.Logger logger = Log.getLogger(PlayerTest.class);
    private Path playerChr;
    private Injector injector;

    @Mock
    private CurrentPlayerData mockSaveData;

    @Inject
    private Db db;

    @Inject
    private Db txt;

    @Inject
    @InjectMocks
    private CurrentPlayerData saveData;

    @InjectMocks
    private PlayerLoader player;

    @InjectMocks
    private PlayerWriter playerWriter;

    @InjectMocks
    private PlayerParser playerParser;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        try {
            injector = GuiceInjector.init(this);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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

    private void prepareTestSavegame(String from, String to) throws IOException {
        Path pathFrom = Path.of(String.format("src/test/resources/_%s/Player.chr", from));
        Path pathTo = Path.of(String.format("src/test/resources/_%s/Player.chr", to));

        Files.deleteIfExists(pathTo);

        new File(String.format("src/test/resources/_%s", to)).mkdir();

        try {
            Files.copy(pathFrom, pathTo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        playerChr = pathTo.toAbsolutePath();
        if (!Files.exists(playerChr)) {
            throw new IOException(String.format("File %s is missing, copy the savegame to execute the tests", playerChr));
        }

        playerParser = new PlayerParser(playerChr.toFile(), to);
        parse();
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());
        db.skills();
    }

    @Test
    public void shouldRemoveAllTeleportsAndAddThree() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(98, total);
        assertEquals(98, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));

        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID teleport : td.getTeleports()) {
                player.removeTeleport(td.getDifficulty(), teleport);
            }
        }

        player.insertTeleport(0, DefaultMapTeleport.get(52).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(53).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(54).getUid());


        player.setTeleportUIDsSize();

        playerWriter.save();
        parse();
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());

        total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID tp : td.getTeleports()) {
                logger.log(INFO, DefaultMapTeleport.get(tp).getRecordId());
                total++;
            }
        }

        assertEquals(3, total);
        assertEquals(3, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
    }

    @Test
    public void shouldRemoveFourTeleportsAndAddTwo() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(98, total);

        player.removeTeleport(0, DefaultMapTeleport.get(1).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(2).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(3).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(4).getUid());

        player.insertTeleport(0, DefaultMapTeleport.get(52).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(53).getUid());

        player.setTeleportUIDsSize();

        playerWriter.save();
        parse();
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());

        total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID tp : td.getTeleports()) {
                total++;
            }
        }

        Set<UID> teleportsFound = new HashSet<>();
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID tp : td.getTeleports()) {
                if (tp.equals(DefaultMapTeleport.get(52).getUid())
                        || tp.equals(DefaultMapTeleport.get(53).getUid())) {
                    logger.log(INFO, DefaultMapTeleport.get(tp).getRecordId());
                    teleportsFound.add(tp);
                }
            }
        }

        assertEquals(96, total);
        assertEquals(96, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));

        assertTrue(teleportsFound.containsAll(Set.of(
                DefaultMapTeleport.get(52).getUid(),
                DefaultMapTeleport.get(53).getUid()
        )), "teleport not found");
    }

    @Test
    public void shouldRemoveFromAllDiff() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(98, total);
        assertEquals(98, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(30, player.getTeleportUIDsSize(0));
        assertEquals(30, player.getTeleportUIDsSize(1));
        assertEquals(38, player.getTeleportUIDsSize(2));

        player.removeTeleport(0, DefaultMapTeleport.get(0).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(1).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(2).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(45).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(46).getUid());
        player.insertTeleport(0, DefaultMapTeleport.get(47).getUid());

        player.removeTeleport(1, DefaultMapTeleport.get(1).getUid());
        player.removeTeleport(1, DefaultMapTeleport.get(2).getUid());
        player.removeTeleport(1, DefaultMapTeleport.get(3).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(48).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(49).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(50).getUid());

        player.removeTeleport(2, DefaultMapTeleport.get(2).getUid());
        player.removeTeleport(2, DefaultMapTeleport.get(3).getUid());
        player.removeTeleport(2, DefaultMapTeleport.get(4).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(52).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(53).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(54).getUid());


        player.setTeleportUIDsSize();

        playerWriter.save();
        parse();
        Mockito.when(mockSaveData.getDataMap()).thenReturn(saveData.getDataMap());

        total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID tp : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(98, total);
        assertEquals(98, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(30, player.getTeleportUIDsSize(0));
        assertEquals(30, player.getTeleportUIDsSize(1));
        assertEquals(38, player.getTeleportUIDsSize(2));

        Set<UID> teleportsFound = new HashSet<>();
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID tp : td.getTeleports()) {
                if (tp.equals(DefaultMapTeleport.get(52).getUid())
                        || tp.equals(DefaultMapTeleport.get(53).getUid())) {
                    logger.log(INFO, DefaultMapTeleport.get(tp).getRecordId());
                    teleportsFound.add(tp);
                }
            }
        }

        assertTrue(teleportsFound.containsAll(Set.of(
                DefaultMapTeleport.get(52).getUid(),
                DefaultMapTeleport.get(53).getUid()
        )), "teleport not found");
    }

    @Test
    public void shouldReturnEmptyStats() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        Mockito.when(mockSaveData.getPlatform()).thenReturn(saveData.getPlatform());
        player.resetPlayerStats();
        playerWriter.save();
        parse();

        assertEquals(0, player.getStatPlayTimeInSeconds());
        assertNull(player.getStatGreatestMonsterKilledName());
        assertEquals(0, player.getStatGreatestMonsterKilledLevel());
        assertEquals(0, player.getStatNumberOfKills());
        assertEquals(0, player.getStatNumberOfDeaths());
        assertEquals(0, player.getStatHealthPotionsUsed());
        assertEquals(0, player.getStatManaPotionsUsed());
        assertEquals(0, player.getStatExperienceFromKills());
        assertEquals(0, player.getStatNumHitsReceived());
        assertEquals(0, player.getStatNumHitsInflicted());
        assertEquals(0, player.getStatCriticalHitsReceived());
        assertEquals(0, player.getStatCriticalHitsInflicted());
    }

    private void reclaimSkillsFromMastery(int indexZeroOrOne) {
        for (Skill s : player.getPlayerSkillsFromMastery(player.getPlayerMasteries().get(indexZeroOrOne))) {
            PlayerSkill sb = player.getPlayerSkills().get(s.getRecordPath());
            if (sb == null || s.getRecordPath() == null) continue;
            player.reclaimSkillPoints(sb);
        }
    }

    private List<Skill> getSkillsFromMastery(int indexZeroOrOne) {
        return player.getPlayerSkillsFromMastery(player.getPlayerMasteries().get(indexZeroOrOne));
    }


    private void removeMasteryPoints(int indexZeroOrOne) {
        Skill mastery = player.getPlayerMasteries().get(indexZeroOrOne);
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());
        if (masteryHasSkills(mastery)) {
            return;
        }
        player.reclaimMasteryPoints(sb);
    }

    private void removeMastery(int indexZeroOrOne) {
        Skill mastery = player.getPlayerMasteries().get(indexZeroOrOne);
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());
        if (masteryHasSkills(mastery)) {
            return;
        }
        player.removeMastery(sb);
    }

    public boolean masteryHasSkills(Skill mastery) {
        List<Skill> list = player.getPlayerSkillsFromMastery(mastery);
        if (!list.isEmpty()) {
            return true;
        }
        return false;
    }

    @Test
    public void shouldRemoveSkills() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        injector.injectMembers(player);
        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());
        Mockito.when(mockSaveData.getPlayerSkills()).thenReturn(saveData.getPlayerSkills());
        List<Skill> skillsMastery0 = getSkillsFromMastery(0);
        List<Skill> skillsMastery1 = getSkillsFromMastery(1);
        assertEquals(22, skillsMastery0.size());
        assertEquals(15, skillsMastery1.size());
        assertEquals(40, player.getMasteryLevel(0));
        assertEquals(24, player.getMasteryLevel(1));

        reclaimSkillsFromMastery(0);
        reclaimSkillsFromMastery(1);

        playerWriter.save();
        parse();

        skillsMastery0 = getSkillsFromMastery(0);
        skillsMastery1 = getSkillsFromMastery(1);
        assertEquals(0, skillsMastery0.size());
        assertEquals(0, skillsMastery1.size());

        assertEquals(40, player.getMasteryLevel(0));
        assertEquals(24, player.getMasteryLevel(1));
    }

    @Test
    public void shouldRemoveMasteryPoints() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        injector.injectMembers(player);
        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());
        Mockito.when(mockSaveData.getPlayerSkills()).thenReturn(saveData.getPlayerSkills());

        reclaimSkillsFromMastery(0);
        reclaimSkillsFromMastery(1);

        removeMasteryPoints(0);
        removeMasteryPoints(1);

        playerWriter.save();
        parse();

        assertEquals(0, getSkillsFromMastery(0).size());
        assertEquals(0, getSkillsFromMastery(1).size());


        assertEquals(1, player.getMasteryLevel(0));
        assertEquals(1, player.getMasteryLevel(1));
    }

    @Test
    public void shouldRemoveMastery() throws IOException {
        prepareTestSavegame("savegame", "testplayer");

        injector.injectMembers(player);
        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());
        Mockito.when(mockSaveData.getPlayerSkills()).thenReturn(saveData.getPlayerSkills());

        reclaimSkillsFromMastery(0);
        reclaimSkillsFromMastery(1);

        removeMasteryPoints(0);
        removeMasteryPoints(1);

        removeMastery(1);
        removeMastery(0);

        playerWriter.save();
        parse();

        assertEquals(0, player.getPlayerMasteries().size());
    }

    @Test
    public void shouldCountTeleportsCorrectlyAfterAdd() throws IOException {
        prepareTestSavegame("savegame2", "testplayer");

        injector.injectMembers(player);
        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(45, total);
        assertEquals(45, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(45, player.getTeleportUIDsSize(0));
        assertEquals(0, player.getTeleportUIDsSize(1));
        assertEquals(0, player.getTeleportUIDsSize(2));

        player.insertTeleport(1, DefaultMapTeleport.get(0).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(1).getUid());
        player.setTeleportUIDsSize();

        playerWriter.save();
        parse();

        total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(45, player.getTeleportUIDsSize(0));
        assertEquals(2, player.getTeleportUIDsSize(1));
        assertEquals(0, player.getTeleportUIDsSize(2));
        assertEquals(47, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(47, total);

    }

    @Test
    public void shouldPassAddAndRemoveMultilpeTeleports() throws IOException {
        prepareTestSavegame("savegame3", "testplayer");

        injector.injectMembers(player);
        Mockito.when(mockSaveData.getPlayerChr()).thenReturn(playerChr);
        Mockito.when(mockSaveData.getBuffer()).thenReturn(saveData.getBuffer());

        int total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(58, total);
        assertEquals(58, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(55, player.getTeleportUIDsSize(0));
        assertEquals(2, player.getTeleportUIDsSize(1));
        assertEquals(1, player.getTeleportUIDsSize(2));

        player.removeTeleport(0, DefaultMapTeleport.get(0).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(1).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(3).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(4).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(5).getUid());
        player.removeTeleport(0, DefaultMapTeleport.get(6).getUid());

        player.insertTeleport(1, DefaultMapTeleport.get(0).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(1).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(3).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(4).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(5).getUid());
        player.insertTeleport(1, DefaultMapTeleport.get(6).getUid());

        player.insertTeleport(2, DefaultMapTeleport.get(0).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(1).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(3).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(4).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(5).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(6).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(7).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(8).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(9).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(10).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(11).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(12).getUid());
        player.insertTeleport(2, DefaultMapTeleport.get(13).getUid());

        player.setTeleportUIDsSize();

        playerWriter.save();
        parse();

        total = 0;
        for (TeleportDifficulty td : player.getTeleportDifficulty()) {
            for (UID ignored : td.getTeleports()) {
                total++;
            }
        }

        assertEquals(49, player.getTeleportUIDsSize(0));
        assertEquals(7, player.getTeleportUIDsSize(1));
        assertEquals(13, player.getTeleportUIDsSize(2));
        assertEquals(69, player.getTeleportUIDsSize(0) + player.getTeleportUIDsSize(1) + player.getTeleportUIDsSize(2));
        assertEquals(69, total);

    }

}

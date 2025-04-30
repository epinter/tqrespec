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

import br.com.pinter.tqdatabase.Database;
import br.com.pinter.tqdatabase.models.Pc;
import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqdatabase.models.Teleport;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.*;
import br.com.pinter.tqrespec.tqdata.*;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.*;

public class Player {
    private static final System.Logger logger = Log.getLogger(Player.class);

    @Inject
    private GameInfo gameInfo;

    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private CurrentPlayerData saveData;

    public CurrentPlayerData getSaveData() {
        return saveData;
    }

    protected void prepareSaveData() {
        reset();
    }

    public boolean loadPlayer(String playerName, SaveLocation saveLocation) {
        if (State.get().isSaveInProgress()) {
            return false;
        }

        try {
            prepareSaveData();

            Path playerChrPath;

            getSaveData().setPlayerName(playerName);
            getSaveData().setLocation(saveLocation);
            playerChrPath = gameInfo.playerChr(playerName, saveLocation);

            logger.log(INFO, "Loading character ''{0}''", playerChrPath);


            getSaveData().setPlayerChr(playerChrPath);
            PlayerParser playerParser = new PlayerParser(
                    new File(getSaveData().getPlayerChr().toString()),
                    playerName);

            getSaveData().setBuffer(playerParser.load());
            getSaveData().setPlatform(playerParser.getDetectedPlatform());
            getSaveData().getDataMap().setBlockInfo(playerParser.getBlockInfo());
            getSaveData().setHeaderInfo(playerParser.getHeaderInfo());
            getSaveData().getDataMap().setVariableLocation(playerParser.getVariableLocation());
            saveData.getDataMap().validate();
            prepareSkillsList();
        } catch (RuntimeException e) {
            reset();
            logger.log(ERROR, "Error loading character", e);
            throw new UnhandledRuntimeException("Error loading character", e);
        }
        return true;
    }

    public PlayerCharacter getCharacter() {
        PlayerCharacter playerCharacter = new PlayerCharacter();
        playerCharacter.setPath(getSaveData().getPlayerPath());
        playerCharacter.setLocation(getSaveData().getLocation());
        playerCharacter.setGender(getGender());
        playerCharacter.setCharacterClass(getPlayerClassName());
        playerCharacter.setDifficulty(getDifficulty());
        playerCharacter.setExperience(getXp());
        playerCharacter.setGold(getMoney());
        playerCharacter.setLevel(getLevel());
        playerCharacter.setName(getCharacterName());
        playerCharacter.setStatAvailableAttrPoints(getModifierPoints());
        playerCharacter.setStatDex(getDex());
        playerCharacter.setStatInt(getInt());
        playerCharacter.setStatStr(getStr());
        playerCharacter.setStatLife(getLife());
        playerCharacter.setStatMana(getMana());
        playerCharacter.setMasteries(new ArrayList<>());
        playerCharacter.setStatAvailableSkillPoints(getAvailableSkillPoints());
        playerCharacter.setPlayTimeInSeconds(getStatPlayTimeInSeconds());
        playerCharacter.setGreatestMonsterKilledName(getStatGreatestMonsterKilledName());
        playerCharacter.setNumberOfDeaths(getStatNumberOfDeaths());
        playerCharacter.setNumberOfKills(getStatNumberOfKills());
        playerCharacter.setExperienceFromKills(getStatExperienceFromKills());
        playerCharacter.setHealthPotionsUsed(getStatHealthPotionsUsed());
        playerCharacter.setManaPotionsUsed(getStatManaPotionsUsed());
        playerCharacter.setNumHitsInflicted(getStatNumHitsInflicted());
        playerCharacter.setNumHitsReceived(getStatNumHitsReceived());
        playerCharacter.setGreatestDamageInflicted(getStatGreatestDamageInflicted());
        playerCharacter.setGreatestMonsterKilledLevel(getStatGreatestMonsterKilledLevel());
        playerCharacter.setCriticalHitsInflicted(getStatCriticalHitsInflicted());
        playerCharacter.getDefaultMapTeleports().put(0, getDefaultMapTeleports(0));
        playerCharacter.getDefaultMapTeleports().put(1, getDefaultMapTeleports(1));
        playerCharacter.getDefaultMapTeleports().put(2, getDefaultMapTeleports(2));

        List<Skill> playerMasteries = getPlayerMasteries();
        Map<String, PlayerSkill> playerSkills = getPlayerSkills();

        for (Skill skill : playerMasteries) {
            PlayerSkill ps = playerSkills.get(skill.getRecordPath());
            if (ps == null) {
                logger.log(ERROR, "Error, skill not found while loading character: " + skill.getRecordPath());
                continue;
            }
            int level = ps.getSkillLevel();
            Mastery mastery = new Mastery();
            mastery.setSkill(skill);
            mastery.setLevel(level);
            mastery.setDisplayName(skill.getSkillDisplayName());

            if (skill.isMastery()) {
                playerCharacter.getMasteries().add(mastery);
            }
        }

        return playerCharacter;
    }

    private void prepareSkillsList() {
        getSaveData().getPlayerSkills().clear();
        for (String v : getSaveData().getDataMap().getVariableLocation().keySet()) {
            if (!v.startsWith(Database.Variables.PREFIX_SKILL_NAME)) {
                continue;
            }

            for (int blockOffset : getSaveData().getDataMap().getVariableLocation().get(v)) {
                int parent = getSaveData().getDataMap().getBlockInfo().get(blockOffset).getParentOffset();
                BlockInfo b = getSaveData().getDataMap().getBlockInfo().get(blockOffset);
                if (parent < 0 || !getSaveData().getDataMap().getBlockInfo().get(parent).getVariables().containsKey("max")
                        || getSaveData().getDataMap().isRemoved(b.getStart())) {
                    //new block size is zero (was removed) or no parent
                    continue;
                }

                PlayerSkill sb = new PlayerSkill();
                sb.setSkillName((String) b.getVariables().get(Constants.Save.SKILL_NAME).getFirst().getValue());
                sb.setSkillEnabled((Integer) b.getVariables().get(Constants.Save.SKILL_ENABLED).getFirst().getValue());
                sb.setSkillActive((Integer) b.getVariables().get(Constants.Save.SKILL_ACTIVE).getFirst().getValue());
                sb.setSkillSubLevel((Integer) b.getVariables().get(Constants.Save.SKILL_SUB_LEVEL).getFirst().getValue());
                sb.setSkillTransition((Integer) b.getVariables().get(Constants.Save.SKILL_TRANSITION).getFirst().getValue());
                sb.setSkillLevel(getVariableValueInteger(b.getStart(), Constants.Save.SKILL_LEVEL));
                sb.setBlockStart(b.getStart());
                if (sb.getSkillName() != null) {
                    if (!db.recordExists(sb.getSkillName())) {
                        logger.log(WARNING, "The character \"{0}\" have the skill \"{1}\", but this" +
                                " skill was not found in the game database. Please check if the game installed is compatible" +
                                " with your save game.", getPlayerSavegameName(), sb.getSkillName());
                        getSaveData().setMissingSkills(true);
                    }
                    synchronized (getSaveData().getPlayerSkills()) {
                        getSaveData().getPlayerSkills().put(Objects.requireNonNull(Database.normalizeRecordPath(sb.getSkillName())),
                                sb);
                    }
                }
            }
        }
    }

    public boolean isMissingSkills() {
        return getSaveData().isMissingSkills();
    }


    /**
     * @return The character name from directory
     */
    public String getPlayerSavegameName() {
        return getSaveData().getPlayerName();
    }

    /**
     * @return The character name from Player.chr
     */
    public String getCharacterName() {
        return getSaveData().getDataMap().getCharacterName();
    }

    public boolean isCharacterLoaded() {
        return getSaveData().getBuffer() != null;
    }

    public int getAvailableSkillPoints() {
        if (!isCharacterLoaded()) return 0;

        int block = getSaveData().getDataMap().getVariableLocation().get(Constants.Save.SKILL_POINTS).getFirst();
        BlockInfo statsBlock = getSaveData().getDataMap().getBlockInfo().get(block);
        return getVariableValueInteger(statsBlock.getStart(), Constants.Save.SKILL_POINTS);
    }

    public void setAvailableSkillPoints(int skillPoints) {
        if (!isCharacterLoaded()) return;
        getSaveData().getDataMap().setInt(Constants.Save.SKILL_POINTS, skillPoints);
    }

    public Map<String, PlayerSkill> getPlayerSkills() {
        boolean update = false;

        for (PlayerSkill b : getSaveData().getPlayerSkills().values()) {
            if (getSaveData().getDataMap().isRemoved(b.getBlockStart())) {
                //new block size is zero, was removed, ignore
                update = true;
            }
        }

        if (getSaveData().getPlayerSkills().isEmpty() || update) {
            prepareSkillsList();
        }

        return getSaveData().getPlayerSkills();
    }

    public int getMasteryLevel(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error loading mastery. Skill detected.");
        }
        BlockInfo sk = getSaveData().getDataMap().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = sk.getVariables().get(Constants.Save.SKILL_LEVEL).getFirst();

        if (varSkillLevel.getVariableType() == VariableType.INTEGER) {
            return getVariableValueInteger(blockStart, Constants.Save.SKILL_LEVEL);
        }
        return -1;
    }

    public int getMasteryLevel(int i) {
        List<Skill> masteries = getPlayerMasteries();

        if (!(masteries.size() == 1 && i > 0) && !masteries.isEmpty()) {
            PlayerSkill sb = getPlayerSkills().get(masteries.get(i).getRecordPath());
            return getMasteryLevel(sb);
        } else {
            return -1;
        }
    }

    public void reclaimSkillPoints(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill skill = db.skills().getSkill(sb.getSkillName(), false);
        if (skill.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Mastery detected.");
        }

        BlockInfo skillToRemove = getSaveData().getDataMap().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = skillToRemove.getVariables().get(Constants.Save.SKILL_LEVEL).getFirst();
        if (varSkillLevel.getVariableType() == VariableType.INTEGER) {
            int currentSkillPoints = getVariableValueInteger(Constants.Save.SKILL_POINTS);
            int currentSkillLevel = (int) varSkillLevel.getValue();
            getSaveData().getDataMap().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            getSaveData().getDataMap().removeBlock(blockStart);
            getSaveData().getDataMap().setInt("max", getVariableValueInteger("max") - 1);

            if (getSaveData().getDataMap().isRemoved(blockStart)) {
                prepareSkillsList();
            }
        }
    }

    public void removeMastery(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error removing mastery. Not a mastery.");
        }
        List<Skill> currentSkillsInMastery = getPlayerSkillsFromMastery(mastery);
        if (!currentSkillsInMastery.isEmpty()) {
            throw new IllegalStateException("Mastery have skills, aborting.");
        }

        int currentSkillPoints = getVariableValueInteger(Constants.Save.SKILL_POINTS);
        int currentSkillLevel = getVariableValueInteger(blockStart, Constants.Save.SKILL_LEVEL);

        if (currentSkillLevel > 0) {
            getSaveData().getDataMap().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            getSaveData().getDataMap().removeBlock(blockStart);
            getSaveData().getDataMap().setInt("max", getVariableValueInteger("max") - 1);
        }

        if (getSaveData().getDataMap().isRemoved(blockStart)) {
            prepareSkillsList();
        }
    }

    public void reclaimMasteryPoints(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Not a mastery.");
        }

        int currentSkillPoints = getVariableValueInteger(Constants.Save.SKILL_POINTS);
        int currentSkillLevel = getVariableValueInteger(blockStart, Constants.Save.SKILL_LEVEL);
        if (currentSkillLevel > 1) {
            getSaveData().getDataMap().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + (currentSkillLevel - 1));
            getSaveData().getDataMap().setInt(blockStart, Constants.Save.SKILL_LEVEL, 1);
            prepareSkillsList();
        }
    }

    public List<Skill> getPlayerMasteries() {
        List<Skill> ret = new ArrayList<>();
        for (PlayerSkill sb : getPlayerSkills().values()) {
            Skill skill = db.skills().getSkill(sb.getSkillName(), false);
            if (skill != null && skill.isMastery()) {
                ret.add(skill);
            }
        }
        return ret;
    }

    public List<Skill> getPlayerSkillsFromMastery(Skill mastery) {
        List<Skill> ret = new ArrayList<>();
        for (PlayerSkill sb : getPlayerSkills().values()) {
            Skill skill = db.skills().getSkill(sb.getSkillName(), false);
            if (skill != null && !skill.isMastery() && mastery.getRecordPath().equals(skill.getParentPath())) {
                ret.add(skill);
            }
        }
        return ret;
    }

    public int getStr() {
        return getSaveData().getDataMap().getTempAttr("str");
    }

    public void setStr(int val) {
        getSaveData().getDataMap().setTempAttr("str", val);
    }

    public int getInt() {
        return getSaveData().getDataMap().getTempAttr("int");
    }

    public void setInt(int val) {
        getSaveData().getDataMap().setTempAttr("int", val);
    }

    public int getDex() {
        return getSaveData().getDataMap().getTempAttr("dex");
    }

    public void setDex(int val) {
        getSaveData().getDataMap().setTempAttr("dex", val);
    }

    public int getLife() {
        return getSaveData().getDataMap().getTempAttr("life");
    }

    public void setLife(int val) {
        getSaveData().getDataMap().setTempAttr("life", val);
    }

    public int getMana() {
        return getSaveData().getDataMap().getTempAttr("mana");
    }

    public void setMana(int val) {
        getSaveData().getDataMap().setTempAttr("mana", val);
    }

    public int getModifierPoints() {
        return getVariableValueInteger("modifierPoints");
    }

    public void setModifierPoints(int val) {
        getSaveData().getDataMap().setInt("modifierPoints", val);
    }

    public void setXp(int val) {
        getSaveData().getDataMap().setInt("currentStats.experiencePoints", val);
    }

    public void setCharLevel(int val) {
        getSaveData().getDataMap().setInt("currentStats.charLevel", val);
        getSaveData().getDataMap().setInt("currentStats.experiencePoints", getXpLevelMin(val));
    }

    public void setMoney(int gold) {
        getSaveData().getDataMap().setInt("money", gold);
    }

    public int getXp() {
        return getVariableValueInteger("currentStats.experiencePoints");
    }

    public int getXpLevelMin(int lvl) {
        lvl--;
        if (lvl == 0) {
            return 0;
        }
        return (int) (((Math.pow(1.2, lvl)) * (1 + (lvl / 0.8))) + (65 * (Math.pow((lvl + 1), 3.25))));
    }

    public int getLevel() {
        return getVariableValueInteger("currentStats.charLevel");
    }

    public int getMoney() {
        return getVariableValueInteger("money");
    }

    public int getAltMoney() {
        if (getSaveData().getDataMap().hasVariable("altMoney")) {
            return getVariableValueInteger("altMoney");
        }

        logger.log(INFO, "altMoney variable not found for character {0}, version {1}",
                saveData.getPlayerName(), saveData.getHeaderInfo().getPlayerVersion());
        return 0;
    }

    public void setAltMoney(int altMoney) {
        if (!getSaveData().getDataMap().hasVariable("altMoney")) {
            logger.log(INFO, "altMoney variable not found for character {0}, version {1}",
                    saveData.getPlayerName(), saveData.getHeaderInfo().getPlayerVersion());
            return;
        }

        getSaveData().getDataMap().setInt("altMoney", altMoney);
    }

    public int getBoostedCharacterForX4() {
        return getVariableValueIntegerValidate("boostedCharacterForX4", 0);
    }

    public void setBoostedCharacterForX4(int value) {
        setVariableValueIntegerValidate("boostedCharacterForX4", value);

    }

    public int getNumberOfSacks() {
        return getVariableValueIntegerValidate("numberOfSacks", 1);
    }

    public void setNumberOfSacks(int value) {
        setVariableValueIntegerValidate("numberOfSacks", value);
    }

    public int getCurrentlyFocusedSackNumber() {
        return getVariableValueIntegerValidate("currentlyFocusedSackNumber", 1);
    }

    public void setCurrentlyFocusedSackNumber(int value) {
        setVariableValueIntegerValidate("currentlyFocusedSackNumber", value);
    }

    public int getCurrentlySelectedSackNumber() {
        return getVariableValueIntegerValidate("currentlySelectedSackNumber", 1);
    }

    public void setCurrentlySelectedSackNumber(int value) {
        setVariableValueIntegerValidate("currentlySelectedSackNumber", value);
    }

    public BlockInfo getPlayerInventoryBlock() {
        int blockOffset = getSaveData().getDataMap().getVariableLocation().get("itemPositionsSavedAsGridCoords").getFirst();
        int validate = getSaveData().getDataMap().getVariableLocation().get("numberOfSacks").getFirst();
        if (blockOffset == validate) {
            return getSaveData().getDataMap().getBlockInfo().get(blockOffset);
        }
        return null;
    }

    public int getInventorySacksCount() {
        BlockInfo inv = getPlayerInventoryBlock();
        List<BlockInfo> playerSacks = getSaveData().getDataMap().getBlockInfo().values()
                .stream().filter(p -> p.getParentOffset() == inv.getStart()).toList();
        return playerSacks.size();
    }

    public void addEmptyPlayerSacks() {
        BlockInfo block = getPlayerInventoryBlock();
        int endBlockSize = 17; //FileParser.END_BLOCK_SIZE
        byte[] sack = new byte[]{
                0x0B, 0x00, 0x00, 0x00, 0x62, 0x65, 0x67, 0x69, 0x6E, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B,
                (byte) 0xCE, (byte) 0xFA, 0x1D, (byte) 0xB0, 0x08, 0x00, 0x00, 0x00, 0x74, 0x65, 0x6D, 0x70, 0x42, 0x6F, 0x6F,
                0x6C, 0x00, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x73, 0x69, 0x7A, 0x65, 0x00, 0x00,
                0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x65, 0x6E, 0x64, 0x5F, 0x62, 0x6C, 0x6F, 0x63, 0x6B,
                (byte) 0xDE, (byte) 0xC0, (byte) 0xAD, (byte) 0xDE};
        int sacksToCreate = 4 - getInventorySacksCount();
        int offset = block.getEnd() - endBlockSize + 1;
        System.out.println("ADDING BLOCK TO OFFSET " + offset);
        for (int i = 0; i < sacksToCreate; i++) {
            getSaveData().getDataMap().insertRawData(sack, offset);
        }
    }


    public int getHasBeenInGame() {
        return getVariableValueIntegerValidate("hasBeenInGame", -1);
    }

    public int getStatPlayTimeInSeconds() {
        return getVariableValueInteger("playTimeInSeconds");
    }

    public void setStatPlayTimeInSeconds(int secs) {
        getSaveData().getDataMap().setInt("playTimeInSeconds", secs);
    }

    public String getStatGreatestMonsterKilledName() {
        List<String> monsters = getSaveData().getDataMap().getStringValuesFromBlock(
                        (PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledName").variable()))
                .stream().filter(v -> v != null && !v.isEmpty()).toList();
        if (monsters.isEmpty()) {
            return null;
        }
        return monsters.getLast();
    }

    public void resetStatGreatestMonsterKilledName() {
        String var = PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledName").variable();
        int block = getSaveData().getDataMap().getVariableLocation().get(var).getFirst();
        if (getSaveData().getDataMap().getBlockInfo().get(block) != null) {
            for (VariableInfo vi : getSaveData().getDataMap().getBlockInfo().get(block).getVariables().values()) {
                if (vi.getValue() == null || !vi.getName().equals(var)) {
                    continue;
                }
                if (vi.getVariableType().equals(VariableType.STRING) || vi.getVariableType().equals(VariableType.STRING_UTF_16_LE) || vi.getVariableType().equals(VariableType.STRING_UTF_32_LE)) {
                    getSaveData().getDataMap().setString(vi, "");
                }
            }
        }
    }

    public int getStatGreatestMonsterKilledLevel() {
        List<Integer> monsterLevels = getSaveData().getDataMap().getIntValuesFromBlock(
                        PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledLevel").variable())
                .stream().filter(v -> v >= 0).toList();
        if (monsterLevels.isEmpty()) {
            return -1;
        }
        return monsterLevels.getLast();
    }

    public void resetStatGreatestMonsterKilledLevel() {
        String var = PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledLevel").variable();
        int block = getSaveData().getDataMap().getVariableLocation().get(var).getFirst();
        if (getSaveData().getDataMap().getBlockInfo().get(block) != null) {
            for (VariableInfo vi : getSaveData().getDataMap().getBlockInfo().get(block).getVariables().values()) {
                if (vi.getValue() == null || !vi.getName().equals(var)) {
                    continue;
                }
                getSaveData().getDataMap().setInt(vi, 0);
            }
        }
    }

    public void resetStatGreatestMonsterKilledLifeAndMana() {
        String var = PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledLifeAndMana").variable();
        int block = getSaveData().getDataMap().getVariableLocation().get(var).getFirst();
        if (getSaveData().getDataMap().getBlockInfo().get(block) != null) {
            for (VariableInfo vi : getSaveData().getDataMap().getBlockInfo().get(block).getVariables().values()) {
                if (vi.getValue() == null || !vi.getName().equals(var)) {
                    continue;
                }
                getSaveData().getDataMap().setInt(vi, 0);
            }
        }
    }

    public int getStatNumberOfDeaths() {
        return getVariableValueInteger("numberOfDeaths");
    }

    public void setStatNumberOfDeaths(int deaths) {
        getSaveData().getDataMap().setInt("numberOfDeaths", deaths);
    }

    public int getStatNumberOfKills() {
        return getVariableValueInteger("numberOfKills");
    }

    public void setStatNumberOfKills(int kills) {
        getSaveData().getDataMap().setInt("numberOfKills", kills);
    }

    public int getStatExperienceFromKills() {
        return getVariableValueInteger("experienceFromKills");
    }

    public void setStatExperienceFromKills(int xp) {
        getSaveData().getDataMap().setInt("experienceFromKills", xp);
    }

    public int getStatHealthPotionsUsed() {
        return getVariableValueInteger("healthPotionsUsed");
    }

    public void setStatHealthPotionsUsed(int health) {
        getSaveData().getDataMap().setInt("healthPotionsUsed", health);
    }

    public int getStatManaPotionsUsed() {
        return getVariableValueInteger("manaPotionsUsed");
    }

    public void setStatManaPotionsUsed(int mana) {
        getSaveData().getDataMap().setInt("manaPotionsUsed", mana);
    }

    public int getStatMaxLevel() {
        return getVariableValueInteger("maxLevel");
    }

    public void setStatMaxLevel(int level) {
        getSaveData().getDataMap().setInt("maxLevel", level);
    }

    public int getStatNumHitsReceived() {
        return getVariableValueInteger("numHitsReceived");
    }

    public void setStatNumHitsReceived(int hits) {
        getSaveData().getDataMap().setInt("numHitsReceived", hits);
    }

    public int getStatNumHitsInflicted() {
        return getVariableValueInteger("numHitsInflicted");
    }

    public void setStatNumHitsInflicted(int hits) {
        getSaveData().getDataMap().setInt("numHitsInflicted", hits);
    }

    public int getStatGreatestDamageInflicted() {
        return (int) getVariableValueFloat("greatestDamageInflicted");
    }

    public void setStatGreatestDamageInflicted(float dmg) {
        getSaveData().getDataMap().setFloat("greatestDamageInflicted", dmg);
    }

    public int getStatCriticalHitsInflicted() {
        return getVariableValueInteger("criticalHitsInflicted");
    }

    public void setStatCriticalHitsInflicted(int hits) {
        getSaveData().getDataMap().setInt("criticalHitsInflicted", hits);
    }

    public int getStatCriticalHitsReceived() {
        return getVariableValueInteger("criticalHitsReceived");
    }

    public void setStatCriticalHitsReceived(int hits) {
        getSaveData().getDataMap().setInt("criticalHitsReceived", hits);
    }

    public void resetPlayerStats() {
        resetStatGreatestMonsterKilledName();
        resetStatGreatestMonsterKilledLevel();
        resetStatGreatestMonsterKilledLifeAndMana();
        setStatPlayTimeInSeconds(0);
        setStatNumberOfDeaths(0);
        setStatNumberOfKills(0);
        setStatExperienceFromKills(0);
        setStatHealthPotionsUsed(0);
        setStatManaPotionsUsed(0);
        setStatMaxLevel(1);
        setStatNumHitsReceived(0);
        setStatNumHitsInflicted(0);
        setStatGreatestDamageInflicted(0);
        setStatCriticalHitsInflicted(0);
        setStatCriticalHitsReceived(0);
    }

    private int getVariableValueIntegerValidate(String variable, int defaultVal) {
        if (!getSaveData().getDataMap().hasVariable(variable)) {
            logger.log(INFO, "{0} variable not found for character {1}, version {2}",
                    variable, saveData.getPlayerName(), saveData.getHeaderInfo().getPlayerVersion());
            return defaultVal;
        }

        return getSaveData().getDataMap().getInt(variable);
    }

    private void setVariableValueIntegerValidate(String variable, int value) {
        if (!getSaveData().getDataMap().hasVariable(variable)) {
            logger.log(INFO, "{0} variable not found for character {1}, version {2}",
                    variable, saveData.getPlayerName(), saveData.getHeaderInfo().getPlayerVersion());
            return;
        }

        getSaveData().getDataMap().setInt(variable, value);
    }

    private int getVariableValueInteger(String variable) {
        return getSaveData().getDataMap().getInt(variable);
    }

    private int getVariableValueInteger(int blockStart, String variable) {
        return getSaveData().getDataMap().getInt(blockStart, variable);
    }

    private float getVariableValueFloat(String variable) {
        return getSaveData().getDataMap().getFloat(variable);
    }

    public String getPlayerClassName() {
        String charClass = getSaveData().getPlayerClassTag();
        if (StringUtils.isNotEmpty(charClass)) {
            return txt.getString(charClass);
        }
        return charClass;
    }

    public Gender getGender() {
        String playerCharacterClass = getSaveData().getPlayerCharacterClass();

        if (playerCharacterClass.isEmpty()) {
            throw new IllegalArgumentException("Error reading playerCharacterClass");
        }

        if (Constants.Save.VALUE_PC_CLASS_FEMALE.equals(playerCharacterClass)) {
            return Gender.FEMALE;
        }

        return Gender.MALE;
    }

    public void setGender(Gender gender) {
        String newTexture;
        Pc pc;

        if (gender.equals(Gender.FEMALE)) {
            getSaveData().getDataMap().setString(Constants.Save.PLAYER_CHARACTER_CLASS, Constants.Save.VALUE_PC_CLASS_FEMALE);
            pc = db.player().getPc(Pc.Gender.FEMALE);
            newTexture = Constants.Save.FEMALE_DEFAULT_TEXTURE;
        } else {
            getSaveData().getDataMap().setString(Constants.Save.PLAYER_CHARACTER_CLASS, Constants.Save.VALUE_PC_CLASS_MALE);
            pc = db.player().getPc(Pc.Gender.MALE);
            newTexture = Constants.Save.MALE_DEFAULT_TEXTURE;
        }

        if (pc.getPlayerTextures() != null && !pc.getPlayerTextures().isEmpty()) {
            newTexture = pc.getPlayerTextures().getFirst();
        }

        String currentTexture = getSaveData().getDataMap().getString(Constants.Save.PLAYER_TEXTURE);

        //try to match new gender with old texture color
        Matcher matcher = Pattern.compile("(?i).*_([^.]+)\\.tex$").matcher(currentTexture);
        if (matcher.matches()) {
            String color = matcher.group(1);
            for (String textureFile : pc.getPlayerTextures()) {
                if (textureFile.toLowerCase().contains(color.toLowerCase())) {
                    newTexture = textureFile;
                }
            }
        }

        getSaveData().getDataMap().setString(Constants.Save.PLAYER_TEXTURE, newTexture);
    }

    public int getDifficulty() {
        return getSaveData().getDataMap().getTempAttr("difficulty");
    }

    public void setDifficulty(int difficulty) {
        if (difficulty < 0 || difficulty > 2) {
            throw new IllegalArgumentException("invalid difficulty");
        }
        getSaveData().getDataMap().setTempAttr("difficulty", difficulty);
    }

    public List<MapTeleport> getDefaultMapTeleports(int difficulty) {
        List<MapTeleport> ret = new ArrayList<>();
        List<TeleportDifficulty> teleports = getTeleportDifficulty();
        if (teleports.size() >= difficulty + 1) {
            for (VariableInfo t : teleports.get(difficulty).getVariables()) {
                UID tpUid = new UID((byte[]) t.getValue());
                MapTeleport mapTeleport;
                try {
                    mapTeleport = DefaultMapTeleport.get(tpUid);
                } catch (NoSuchElementException e) {
                    logger.log(WARNING, String.format("teleport not found with uid = '%s' character=(%s) difficulty=%d",
                            tpUid, getPlayerSavegameName(), difficulty));
                    continue;
                }
                if (ret.contains(mapTeleport)) {
                    continue;
                }
                Teleport teleport = db.teleports().getTeleport(mapTeleport.getRecordId());
                mapTeleport.setName(teleport.getDescription());
                ret.add(mapTeleport);
            }
        }
        ret.sort(Comparator.comparingInt(MapTeleport::getOrder));
        return ret;
    }

    public List<TeleportDifficulty> getTeleportDifficulty() {
        List<TeleportDifficulty> ret = new ArrayList<>();

        for (int i = 0; i <= getDifficulty(); i++) {
            TeleportDifficulty teleports = getTeleportUidFromDifficulty(i);
            if (teleports != null) {
                ret.add(teleports);
            }
        }

        return ret;
    }

    public VariableInfo getTeleportUIDsSizeVar(int difficulty) {
        Optional<BlockInfo> first = getSaveData().getDataMap().getBlockInfo().values().stream().filter(
                f -> f.getBlockType() == PlayerBlockType.PLAYER_MAIN).findFirst();

        BlockInfo block;

        if (first.isPresent()) {
            block = first.get();
        } else {
            return null;
        }
        List<VariableInfo> teleportUidsSizeVars = new ArrayList<>(block.getVariables().get(Constants.Save.VAR_TELEPORTUIDSSIZE));
        VariableInfo var = teleportUidsSizeVars.stream().sorted(Comparator.comparing(VariableInfo::getKeyOffset)).toList().get(difficulty);
        if (var.isInt() && var.getName().equals(Constants.Save.VAR_TELEPORTUIDSSIZE)) {
            return var;
        }
        return null;
    }

    public void setTeleportUIDsSize() {
        for (int difficulty = 0; difficulty <= getDifficulty(); difficulty++) {
            TeleportDifficulty teleportDifficulty = getTeleportUidFromDifficulty(difficulty);
            if (teleportDifficulty != null) {
                logger.log(DEBUG, "setting teleportUIDsSize({0}) to {1}", difficulty, teleportDifficulty.getTeleports().size());
                getSaveData().getDataMap().setInt(getTeleportUIDsSizeVar(difficulty), teleportDifficulty.getTeleports().size());
            }
        }
    }

    public int getTeleportUIDsSize(int difficulty) {
        VariableInfo var = getTeleportUIDsSizeVar(difficulty);
        if (var != null) {
            return (int) var.getValue();
        }
        return 0;
    }

    public void removeTeleport(int difficulty, UID uid) {
        TeleportDifficulty teleportDifficulty = getTeleportUidFromDifficulty(difficulty);

        if (teleportDifficulty == null) {
            throw new UnhandledRuntimeException("error creating teleport");
        }

        VariableInfo uidsSize = getTeleportUIDsSizeVar(difficulty);

        List<VariableInfo> toRemove = new ArrayList<>();
        for (VariableInfo stagingVar : teleportDifficulty.getBlockInfo().getStagingVariables().values()) {
            if (stagingVar.getVariableType().equals(VariableType.UID) && stagingVar.getName().equals(Constants.Save.VAR_TELEPORTUID)) {
                UID uidTp;
                try {
                    uidTp = new UID((byte[]) stagingVar.getValue());
                } catch (IllegalArgumentException e) {
                    logger.log(WARNING, "Invalid map teleport data, uid: " + stagingVar.getValueString());
                    continue;
                }
                if (uidTp.equals(uid)) {
                    logger.log(DEBUG, "------------- removing portal " + uid + ".");
                    toRemove.add(stagingVar);
                }
            }
        }

        for (VariableInfo vi : teleportDifficulty.getVariables()) {
            if (vi.getVariableType().equals(VariableType.UID) && vi.getName().equals(Constants.Save.VAR_TELEPORTUID)) {
                MapTeleport currentTeleport;
                UID uidTp = new UID((byte[]) vi.getValue());
                try {
                    currentTeleport = DefaultMapTeleport.get(uidTp);
                } catch (NoSuchElementException e) {
                    logger.log(WARNING, "Invalid map teleport data, uid: " + vi.getValueString(), e);
                    continue;
                }

                if (currentTeleport != null && currentTeleport.getUid().equals(uid)) {
                    logger.log(DEBUG, "------------- removing portal " + uid);
                    toRemove.add(vi);
                }
            }
        }

        for (VariableInfo v : toRemove) {
            getSaveData().getDataMap().removeVariable(v);
            getSaveData().getDataMap().decrementInt(uidsSize);
        }
    }

    public void insertTeleport(int difficulty, UID uid) {
        TeleportDifficulty teleportDifficulty = getTeleportUidFromDifficulty(difficulty);

        if (difficulty > getDifficulty()) {
            throw new UnhandledRuntimeException(String.format("character doesn't have the difficulty %d unlocked", difficulty));
        }

        if (teleportDifficulty == null) {
            throw new UnhandledRuntimeException("error creating teleport");
        }
        int teleportUIDsSizeKeyLength = 4 + Constants.Save.VAR_TELEPORTUIDSSIZE.length();
        int teleportUIDKeyLength = 4 + Constants.Save.VAR_TELEPORTUID.length();

        int offset = teleportDifficulty.getOffset() + (teleportUIDsSizeKeyLength + 4);
        for (VariableInfo r : teleportDifficulty.getVariables()) {
            VariableInfo vi = getSaveData().getDataMap().getChangesForVariable(r);
            if (vi.getVariableType().equals(VariableType.UID) && vi.getName().equals(Constants.Save.VAR_TELEPORTUID)) {
                MapTeleport currentTeleport;
                try {
                    currentTeleport = DefaultMapTeleport.get(new UID((byte[]) vi.getValue()));
                } catch (NoSuchElementException e) {
                    logger.log(WARNING, "Invalid map teleport data, uid: " + vi.getValueString(), e);
                    continue;
                }

                if (currentTeleport.getUid().equals(uid)) {
                    logger.log(WARNING, "------------- portal ''{0}'' already exists for difficulty ''{1}''", uid, difficulty);
                    return;
                }
            }
        }

        if (offset <= 200) {
            throw new UnhandledRuntimeException("error creating teleport, offset not found");
        }

        VariableInfo uidSize = teleportDifficulty.getBlockInfo().getVariables().get(Constants.Save.VAR_TELEPORTUIDSSIZE).get(difficulty);
        VariableInfo newVi = new VariableInfo();
        newVi.setBlockOffset(uidSize.getBlockOffset());
        newVi.setVariableType(VariableType.UID);
        newVi.setName(Constants.Save.VAR_TELEPORTUID);
        newVi.setValue(uid.getBytes());
        newVi.setKeyOffset(offset);
        newVi.setValOffset(offset + teleportUIDKeyLength);
        newVi.setValSize(VariableType.UID.dataTypeSize());
        getSaveData().getDataMap().insertVariable(newVi);
        getSaveData().getDataMap().incrementInt(uidSize);
    }

    private TeleportDifficulty getTeleportUidFromDifficulty(int difficulty) {
        Optional<BlockInfo> first = getSaveData().getDataMap().getBlockInfo().values().stream().filter(
                f -> f.getBlockType() == PlayerBlockType.PLAYER_MAIN).findFirst();

        BlockInfo block;

        if (first.isPresent()) {
            block = first.get();
        } else {
            return null;
        }

        List<VariableInfo> teleportUidsSizeVars = new ArrayList<>(Objects.requireNonNull(block).getVariables().get(Constants.Save.VAR_TELEPORTUIDSSIZE));
        teleportUidsSizeVars.sort(Comparator.comparing(VariableInfo::getKeyOffset));
        int offsetStart = teleportUidsSizeVars.get(difficulty).getKeyOffset();
        VariableInfo size = teleportUidsSizeVars.get(difficulty);

        List<VariableInfo> teleports = new ArrayList<>();

        List<VariableInfo> teleportUidVars = block.getVariables().get(Constants.Save.VAR_TELEPORTUID)
                .stream().sorted(Comparator.comparing(VariableInfo::getKeyOffset)).toList();

        int curDiff = -1;
        int startOffset = -1;
        int endOffset = -1;

        for (VariableInfo v : block.getVariables().values().stream().sorted(Comparator.comparing(VariableInfo::getKeyOffset)).toList()) {
            if (!Constants.Save.VAR_TELEPORTUIDSSIZE.equals(v.getName()) && !Constants.Save.VAR_TELEPORTUID.equals(v.getName())) {
                continue;
            }
            if (Constants.Save.VAR_TELEPORTUIDSSIZE.equals(v.getName())) {
                curDiff++;
                if (curDiff == difficulty) {
                    startOffset = v.getKeyOffset() + v.getVariableBytesLength();
                    endOffset = startOffset;
                    continue;
                }
            }
            if (curDiff == difficulty && v.getKeyOffset() >= teleportUidsSizeVars.get(curDiff).getKeyOffset()) {
                endOffset = v.getValOffset() + v.getValSize() - 1;
            }
        }

        //search for teleports from savegame
        for (VariableInfo v : teleportUidVars) {
            if (getSaveData().getDataMap().isVariableRemoved(v)) { //skip pending remove teleports
                continue;
            }
            if (v.getKeyOffset() >= startOffset && v.getKeyOffset() <= endOffset) {
                teleports.add(v);
            }
        }

        //search for teleports pending save
        for (VariableInfo v : block.getStagingVariables().get(Constants.Save.VAR_TELEPORTUID)
                .stream().sorted(Comparator.comparing(VariableInfo::getKeyOffset)).toList()) {
            if (v.getKeyOffset() >= startOffset && v.getKeyOffset() <= endOffset) {
                teleports.add(v);
            }
        }

        logger.log(DEBUG, "difficulty{0}: start={1}; end={2}; count={3}; uidssize={4}", difficulty, startOffset, endOffset, teleports.size(), size.getValue());

        return new TeleportDifficulty(difficulty, (Integer) size.getValue(), offsetStart, teleports, block);
    }

    public void reset() {
        State.get().setSaveInProgress(null);
        if (getSaveData() != null) {
            getSaveData().reset();
        }
    }
}

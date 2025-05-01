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

    public int getXp() {
        return getVariableValueInteger("currentStats.experiencePoints");
    }

    public int getLevel() {
        return getVariableValueInteger("currentStats.charLevel");
    }

    public int getMoney() {
        return getVariableValueInteger("money");
    }

    public int getStatPlayTimeInSeconds() {
        return getVariableValueInteger("playTimeInSeconds");
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

    public int getStatGreatestMonsterKilledLevel() {
        List<Integer> monsterLevels = getSaveData().getDataMap().getIntValuesFromBlock(
                        PlayerFileVariable.valueOf(getSaveData().getPlatform(), "greatestMonsterKilledLevel").variable())
                .stream().filter(v -> v != 0).toList();
        if (monsterLevels.isEmpty()) {
            return -1;
        }
        return monsterLevels.getLast();
    }

    public int getStatNumberOfDeaths() {
        return getVariableValueInteger("numberOfDeaths");
    }

    public int getStatNumberOfKills() {
        return getVariableValueInteger("numberOfKills");
    }

    public int getStatExperienceFromKills() {
        return getVariableValueInteger("experienceFromKills");
    }

    public int getStatHealthPotionsUsed() {
        return getVariableValueInteger("healthPotionsUsed");
    }

    public int getStatManaPotionsUsed() {
        return getVariableValueInteger("manaPotionsUsed");
    }

    public int getStatMaxLevel() {
        return getVariableValueInteger("maxLevel");
    }

    public int getStatNumHitsReceived() {
        return getVariableValueInteger("numHitsReceived");
    }

    public int getStatNumHitsInflicted() {
        return getVariableValueInteger("numHitsInflicted");
    }

    public int getStatGreatestDamageInflicted() {
        return (int) getVariableValueFloat("greatestDamageInflicted");
    }

    public int getStatCriticalHitsInflicted() {
        return getVariableValueInteger("criticalHitsInflicted");
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

    public List<MapTeleport> getDefaultMapTeleports(int difficulty) {
        List<MapTeleport> ret = new ArrayList<>();
        if (getTeleports().size() >= difficulty + 1) {
            for (VariableInfo t : getTeleports().get(difficulty).getTeleportList()) {
                UID tpUid = new UID((byte[]) t.getValue());
                MapTeleport mapTeleport = DefaultMapTeleport.get(tpUid);
                if (mapTeleport == null) {
                    logger.log(WARNING, String.format("teleport not found with uid = '%s' character=(%s) difficulty=%d", tpUid, getPlayerSavegameName(), difficulty));
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

    public List<TeleportDifficulty> getTeleports() {
        List<TeleportDifficulty> ret = new ArrayList<>();

        for (int i = 0; i <= getDifficulty(); i++) {
            TeleportDifficulty teleports = getTeleportUidFromDifficulty(i);
            if (teleports != null) {
                ret.add(teleports);
            }
        }

        return ret;
    }

    public void removeTeleport(int difficulty, UID uid) {
        TeleportDifficulty teleportDifficulty = getTeleportUidFromDifficulty(difficulty);

        if (difficulty > getDifficulty()) {
            throw new UnhandledRuntimeException(String.format("character doesn't have the difficulty %d unlocked", difficulty));
        }

        if (teleportDifficulty == null) {
            throw new UnhandledRuntimeException("error creating teleport");
        }

        VariableInfo uidSize = teleportDifficulty.getBlockInfo().getVariables().get(Constants.Save.VAR_TELEPORTUIDSSIZE).get(difficulty);

        List<VariableInfo> toRemove = new ArrayList<>();
        for (VariableInfo stagingVar : teleportDifficulty.getBlockInfo().getStagingVariables().values()) {
            if (stagingVar.getVariableType().equals(VariableType.UID) && stagingVar.getName().equals(Constants.Save.VAR_TELEPORTUID)
                    && (new UID((byte[]) stagingVar.getValue())).equals(uid)) {
                logger.log(ERROR, "------------- removing portal " + uid + ".");
                toRemove.add(stagingVar);
            }
        }

        for (VariableInfo v : toRemove) {
            getSaveData().getDataMap().removeVariable(v.getKeyOffset(), v);
            getSaveData().getDataMap().decrementInt(uidSize);
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
        for (VariableInfo stagingVar : teleportDifficulty.getBlockInfo().getStagingVariables().values()) {
            if (stagingVar.getVariableType().equals(VariableType.UID) && stagingVar.getName().equals(Constants.Save.VAR_TELEPORTUID)
                    && (new UID((byte[]) stagingVar.getValue())).equals(uid)) {
                logger.log(ERROR, "------------- portal " + uid + "already exists");
                break;
            }
        }

        for (VariableInfo vi : teleportDifficulty.getTeleportList()) {
            if (vi.getVariableType().equals(VariableType.UID) && vi.getName().equals(Constants.Save.VAR_TELEPORTUID)) {
                MapTeleport currentTeleport = DefaultMapTeleport.get(new UID((byte[]) vi.getValue()));
                if (currentTeleport != null && currentTeleport.getUid().equals(uid)) {
                    logger.log(ERROR, "------------- portal " + uid + "already exists");
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
        teleportUidsSizeVars.sort(Comparator.comparingInt(VariableInfo::getValOffset));
        int offsetStart = teleportUidsSizeVars.get(difficulty).getKeyOffset();
        int offsetStop = teleportUidsSizeVars.get(Math.max(difficulty, 2)).getKeyOffset();
        VariableInfo size = teleportUidsSizeVars.get(difficulty);

        List<VariableInfo> teleports = new ArrayList<>();

        List<VariableInfo> teleportUidVars = new ArrayList<>(block.getVariables().get(Constants.Save.VAR_TELEPORTUID));
        for (VariableInfo v : teleportUidVars) {
            if (v.getKeyOffset() > offsetStart) {
                if (offsetStart != offsetStop && v.getKeyOffset() > offsetStop) {
                    break;
                }
                teleports.add(v);
            }
        }

        return new TeleportDifficulty(difficulty, (Integer) size.getValue(), offsetStart, teleports, block);
    }

    public void reset() {
        State.get().setSaveInProgress(null);
        if (getSaveData() != null) {
            getSaveData().reset();
        }
    }
}

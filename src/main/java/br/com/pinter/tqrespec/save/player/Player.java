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
import br.com.pinter.tqrespec.save.BlockInfo;
import br.com.pinter.tqrespec.save.UID;
import br.com.pinter.tqrespec.save.VariableInfo;
import br.com.pinter.tqrespec.save.VariableType;
import br.com.pinter.tqrespec.tqdata.*;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public void prepareSaveData() {
        reset();
    }

    public boolean loadPlayer(String playerName) {
        if (State.get().getSaveInProgress() != null && State.get().getSaveInProgress()) {
            return false;
        }

        try {
            prepareSaveData();

            getSaveData().setPlayerName(playerName);
            getSaveData().setPlayerChr(gameInfo.playerChr(playerName, saveData.isCustomQuest()));
            PlayerParser playerParser = new PlayerParser(
                    new File(getSaveData().getPlayerChr().toString()),
                    playerName);

            getSaveData().setBuffer(playerParser.loadPlayer());
            getSaveData().getChanges().setBlockInfo(playerParser.getBlockInfo());
            getSaveData().setHeaderInfo(playerParser.getHeaderInfo());
            getSaveData().getChanges().setVariableLocation(playerParser.getVariableLocation());
            prepareSkillsList();
        } catch (Exception e) {
            reset();
            logger.log(System.Logger.Level.ERROR, "Error loading character", e);
            throw new UnhandledRuntimeException("Error loading character", e);
        }
        return true;
    }

    public PlayerCharacter getCharacter() {
        PlayerCharacter playerCharacter = new PlayerCharacter();
        playerCharacter.setGender(getGender());
        playerCharacter.setCharacterClass(getPlayerClassName());
        playerCharacter.setDifficulty(getDifficulty());
        playerCharacter.setExperience(getXp());
        playerCharacter.setGold(getMoney());
        playerCharacter.setLevel(getLevel());
        playerCharacter.setName(getPlayerName());
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

        for (Skill skill: playerMasteries) {
            PlayerSkill ps = playerSkills.get(skill.getRecordPath());
            if(ps == null) {
                logger.log(System.Logger.Level.ERROR, "Error, skill not found while loading character: "+skill.getRecordPath());
                continue;
            }
            int level = ps.getSkillLevel();
            Mastery mastery = new Mastery();
            mastery.setMastery(skill);
            mastery.setLevel(level);
            mastery.setDisplayName(skill.getSkillDisplayName());

            if(skill.isMastery()) {
                playerCharacter.getMasteries().add(mastery);
            }
        }

        return playerCharacter;
    }

    private void prepareSkillsList() {
        getSaveData().getPlayerSkills().clear();
        for (String v : getSaveData().getChanges().getVariableLocation().keySet()) {
            if (!v.startsWith(Database.Variables.PREFIX_SKILL_NAME)) {
                continue;
            }

            for (int blockOffset : getSaveData().getChanges().getVariableLocation().get(v)) {
                int parent = getSaveData().getChanges().getBlockInfo().get(blockOffset).getParentOffset();
                BlockInfo b = getSaveData().getChanges().getBlockInfo().get(blockOffset);
                if (parent < 0 || !getSaveData().getChanges().getBlockInfo().get(parent).getVariables().containsKey("max")
                        || (getSaveData().getChanges().get(b.getStart()) != null && getSaveData().getChanges().get(b.getStart()).length == 0)) {
                    //new block size is zero (was removed) or no parent
                    continue;
                }

                PlayerSkill sb = new PlayerSkill();
                sb.setSkillName((String) b.getVariables().get(Constants.Save.SKILL_NAME).get(0).getValue());
                sb.setSkillEnabled((Integer) b.getVariables().get(Constants.Save.SKILL_ENABLED).get(0).getValue());
                sb.setSkillActive((Integer) b.getVariables().get(Constants.Save.SKILL_ACTIVE).get(0).getValue());
                sb.setSkillSubLevel((Integer) b.getVariables().get(Constants.Save.SKILL_SUB_LEVEL).get(0).getValue());
                sb.setSkillTransition((Integer) b.getVariables().get(Constants.Save.SKILL_TRANSITION).get(0).getValue());
                sb.setSkillLevel(getVariableValueInteger(b.getStart(), Constants.Save.SKILL_LEVEL));
                sb.setBlockStart(b.getStart());
                if (sb.getSkillName() != null) {
                    if (!db.recordExists(sb.getSkillName())) {
                        logger.log(System.Logger.Level.WARNING,"The character \"{0}\" have the skill \"{1}\", but this" +
                                " skill was not found in the game database. Please check if the game installed is compatible" +
                                " with your save game.", getPlayerName(), sb.getSkillName());
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

    public String getPlayerName() {
        return getSaveData().getPlayerName();
    }
    public boolean isCharacterLoaded() {
        return getSaveData().getBuffer() != null;
    }

    public int getAvailableSkillPoints() {
        if (!isCharacterLoaded()) return 0;

        int block = getSaveData().getChanges().getVariableLocation().get(Constants.Save.SKILL_POINTS).get(0);
        BlockInfo statsBlock = getSaveData().getChanges().getBlockInfo().get(block);
        return getVariableValueInteger(statsBlock.getStart(), Constants.Save.SKILL_POINTS);
    }

    public Map<String, PlayerSkill> getPlayerSkills() {
        boolean update = false;

        for (PlayerSkill b : getSaveData().getPlayerSkills().values()) {
            if (getSaveData().getChanges().get(b.getBlockStart()) != null
                    && getSaveData().getChanges().get(b.getBlockStart()).length == 0) {
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
        BlockInfo sk = getSaveData().getChanges().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = sk.getVariables().get(Constants.Save.SKILL_LEVEL).get(0);

        if (varSkillLevel.getVariableType() == VariableType.INTEGER) {
            return getVariableValueInteger(blockStart, Constants.Save.SKILL_LEVEL);
        }
        return -1;
    }

    public void reclaimSkillPoints(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill skill = db.skills().getSkill(sb.getSkillName(), false);
        if (skill.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Mastery detected.");
        }

        BlockInfo skillToRemove = getSaveData().getChanges().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = skillToRemove.getVariables().get(Constants.Save.SKILL_LEVEL).get(0);
        if (varSkillLevel.getVariableType() == VariableType.INTEGER) {
            int currentSkillPoints = getVariableValueInteger(Constants.Save.SKILL_POINTS);
            int currentSkillLevel = (int) varSkillLevel.getValue();
            getSaveData().getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            getSaveData().getChanges().removeBlock(blockStart);
            getSaveData().getChanges().setInt("max", getVariableValueInteger("max") - 1);

            if (getSaveData().getChanges().get(blockStart) != null
                    && getSaveData().getChanges().get(blockStart).length == 0) {
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
            getSaveData().getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            getSaveData().getChanges().removeBlock(blockStart);
            getSaveData().getChanges().setInt("max", getVariableValueInteger("max") - 1);
        }

        if (getSaveData().getChanges().get(blockStart) != null
                && getSaveData().getChanges().get(blockStart).length == 0) {
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
            getSaveData().getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + (currentSkillLevel - 1));
            getSaveData().getChanges().setInt(blockStart, Constants.Save.SKILL_LEVEL, 1);
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

    List<VariableInfo> getTempVariableInfo(String var) {
        List<Integer> temp = getSaveData().getChanges().getVariableLocation().get("temp");
        for (Integer blockStart : temp) {
            BlockInfo b = getSaveData().getChanges().getBlockInfo().get(blockStart);
            if (!b.getVariableByAlias(var).isEmpty()) {
                return b.getVariableByAlias(var);
            }
        }
        return List.of();
    }

    private int getTempAttr(String attr) {
        int ret = -1;
        List<VariableInfo> varList = getTempVariableInfo(attr);
        if (varList.size() == 1 && varList.get(0) != null) {
            VariableInfo attrVar = varList.get(0);
            if (attrVar.getVariableType() == VariableType.FLOAT) {
                ret = Math.round(getVariableValueFloat(attrVar));
            } else if (attrVar.getVariableType() == VariableType.INTEGER) {
                ret = getVariableValueInteger(attrVar);
            }
        }
        if (ret < 0) {
            throw new IllegalArgumentException(String.format("attribute not found %s", attr));
        }
        return ret;
    }

    private void setTempAttr(String attr, Integer val) {
        List<VariableInfo> varList = getTempVariableInfo(attr);
        if (!varList.isEmpty() && varList.get(0) != null) {
            VariableInfo attrVar = varList.get(0);
            if (attrVar.getVariableType() == VariableType.FLOAT) {
                getSaveData().getChanges().setFloat(attrVar, val);
            } else if (attrVar.getVariableType() == VariableType.INTEGER) {
                getSaveData().getChanges().setInt(attrVar, val);
            }
        } else {
            throw new IllegalArgumentException(String.format("attribute not found %s", attr));
        }
    }

    public int getStr() {
        return getTempAttr("str");
    }

    public void setStr(int val) {
        setTempAttr("str", val);
    }

    public int getInt() {
        return getTempAttr("int");
    }

    public void setInt(int val) {
        setTempAttr("int", val);
    }

    public int getDex() {
        return getTempAttr("dex");
    }

    public void setDex(int val) {
        setTempAttr("dex", val);
    }

    public int getLife() {
        return getTempAttr("life");
    }

    public void setLife(int val) {
        setTempAttr("life", val);
    }

    public int getMana() {
        return getTempAttr("mana");
    }

    public void setMana(int val) {
        setTempAttr("mana", val);
    }

    public int getModifierPoints() {
        return getVariableValueInteger("modifierPoints");
    }

    public void setModifierPoints(int val) {
        getSaveData().getChanges().setInt("modifierPoints", val);
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
        List<String> monsters =  getSaveData().getChanges().getStringValuesFromBlock(
                (PlayerFileVariable.valueOf("greatestMonsterKilledName").var()))
                .stream().filter((v) -> v != null && !v.isEmpty()).collect(Collectors.toList());
        if(monsters.isEmpty()) {
            return null;
        }
        return monsters.get(monsters.size()-1);
    }

    public int getStatGreatestMonsterKilledLevel() {
        List<Integer> monsterLevels = getSaveData().getChanges().getIntValuesFromBlock(
                PlayerFileVariable.valueOf("greatestMonsterKilledLevel").var())
                .stream().filter((v) -> v != 0).collect(Collectors.toList());
        if(monsterLevels.isEmpty()) {
            return -1;
        }
        return monsterLevels.get(monsterLevels.size()-1);
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

    private int getVariableValueInteger(VariableInfo variableInfo) {
        return getSaveData().getChanges().getInt(variableInfo);
    }

    private int getVariableValueInteger(String variable) {
        return getSaveData().getChanges().getInt(variable);
    }

    private int getVariableValueInteger(int blockStart, String variable) {
        return getSaveData().getChanges().getInt(blockStart, variable);
    }

    private float getVariableValueFloat(String variable) {
        return getSaveData().getChanges().getFloat(variable);
    }

    private float getVariableValueFloat(VariableInfo variableInfo) {
        return getSaveData().getChanges().getFloat(variableInfo);
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

        if(playerCharacterClass.isEmpty()) {
            throw new IllegalArgumentException("Error reading playerCharacterClass");
        }

        if(Constants.Save.VALUE_PC_CLASS_FEMALE.equals(playerCharacterClass)) {
            return Gender.FEMALE;
        }

        return  Gender.MALE;
    }

    public void setGender(Gender gender) {
        String newTexture;
        Pc pc;

        if(gender.equals(Gender.FEMALE)) {
            getSaveData().getChanges().setString(Constants.Save.PLAYER_CHARACTER_CLASS, Constants.Save.VALUE_PC_CLASS_FEMALE);
            pc = db.player().getPc(Pc.Gender.FEMALE);
            newTexture = Constants.Save.FEMALE_DEFAULT_TEXTURE;
        } else {
            getSaveData().getChanges().setString(Constants.Save.PLAYER_CHARACTER_CLASS, Constants.Save.VALUE_PC_CLASS_MALE);
            pc = db.player().getPc(Pc.Gender.MALE);
            newTexture = Constants.Save.MALE_DEFAULT_TEXTURE;
        }

        if(pc.getPlayerTextures()!=null && !pc.getPlayerTextures().isEmpty()) {
            newTexture = pc.getPlayerTextures().get(0);
        }

        String currentTexture = getSaveData().getChanges().getString(Constants.Save.PLAYER_TEXTURE);

        //try to match new gender with old texture color
        Matcher matcher = Pattern.compile("(?i).*_([^.]+)\\.tex$").matcher(currentTexture);
        if(matcher.matches()) {
            String color = matcher.group(1);
            for(String textureFile: pc.getPlayerTextures()) {
                if(textureFile.toLowerCase().contains(color.toLowerCase())) {
                    newTexture = textureFile;
                }
            }
        }

        getSaveData().getChanges().setString(Constants.Save.PLAYER_TEXTURE, newTexture);
    }

    public int getDifficulty() {
        return getTempAttr("difficulty");
    }

    public List<MapTeleport> getDefaultMapTeleports(int difficulty) {
        List<MapTeleport> ret = new ArrayList<>();
        if(getTeleports().size() >= difficulty+1) {
            for (VariableInfo t : getTeleports().get(difficulty).getTeleportList()) {
                UID tpUid = new UID((byte[]) t.getValue());
                MapTeleport mapTeleport = DefaultMapTeleport.get(tpUid);
                if(mapTeleport == null) {
                    logger.log(System.Logger.Level.WARNING,String.format("teleport not found with uid = '%s' character=(%s) difficulty=%d",tpUid,getPlayerName(),difficulty));
                    continue;
                }
                Teleport teleport = db.teleports().getTeleport(mapTeleport.getRecordId());
                mapTeleport.setName(teleport.getDescription());
                ret.add(mapTeleport);
            }
        }
        return ret;
    }

    public List<TeleportDifficulty> getTeleports() {
        List<TeleportDifficulty> ret = new ArrayList<>();

        Optional<BlockInfo> first = getSaveData().getChanges().getBlockInfo().values().stream().filter(
                f -> f.getBlockType() == PlayerBlockType.PLAYER_MAIN).findFirst();

        BlockInfo playerMain = null;
        if (first.isPresent()) {
            playerMain = first.get();

            for (int i = 0; i <= getDifficulty(); i++) {
                ret.add(getTeleportUidFromDifficulty(i, playerMain));
            }
        }

        return ret;
    }

    private TeleportDifficulty getTeleportUidFromDifficulty(int difficulty, BlockInfo block) {
        List<VariableInfo> teleportUidsSizeVars = new ArrayList<>(Objects.requireNonNull(block).getVariables().get("teleportUIDsSize"));
        teleportUidsSizeVars.sort(Comparator.comparingInt(VariableInfo::getValOffset));
        int offsetStart = teleportUidsSizeVars.get(difficulty).getKeyOffset();
        int offsetStop = teleportUidsSizeVars.get(Math.max(difficulty, 2)).getKeyOffset();
        VariableInfo size = teleportUidsSizeVars.get(difficulty);

        List<VariableInfo> teleports = new ArrayList<>();

        List<VariableInfo> teleportUidVars = new ArrayList<>(block.getVariables().get("teleportUID"));
        for (VariableInfo v : teleportUidVars) {
            if (v.getKeyOffset() > offsetStart) {
                if (offsetStart != offsetStop && v.getKeyOffset() > offsetStop) {
                    break;
                }
                teleports.add(v);
            }
        }

        return new TeleportDifficulty(difficulty, (Integer) size.getValue(), offsetStart, teleports);
    }

    public void reset() {
        State.get().setSaveInProgress(null);
        saveData.reset();
    }

}

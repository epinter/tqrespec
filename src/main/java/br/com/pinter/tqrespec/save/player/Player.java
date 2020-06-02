/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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
import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.save.BlockInfo;
import br.com.pinter.tqrespec.save.VariableInfo;
import br.com.pinter.tqrespec.save.VariableType;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class Player {
    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private CurrentPlayerData saveData;

    public boolean loadPlayer(String playerName) {
        if (State.get().getSaveInProgress() != null && State.get().getSaveInProgress()) {
            return false;
        }

        try {
            reset();
            saveData.setPlayerName(playerName);
            PlayerParser playerParser = new PlayerParser(
                    new File(saveData.getPlayerChr().toString()),
                    playerName);

            saveData.setBuffer(playerParser.loadPlayer());
            saveData.getChanges().setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.getChanges().setVariableLocation(playerParser.getVariableLocation());
            prepareSkillsList();
        } catch (Exception e) {
            reset();
            throw new UnhandledRuntimeException("Error loading character", e);
        }
        return true;
    }

    private void prepareSkillsList() {
        saveData.getPlayerSkills().clear();
        for (String v : saveData.getChanges().getVariableLocation().keySet()) {
            if (v.startsWith(Database.Variables.PREFIX_SKILL_NAME)) {
                for (int blockOffset : saveData.getChanges().getVariableLocation().get(v)) {
                    int parent = saveData.getChanges().getBlockInfo().get(blockOffset).getParentOffset();
                    BlockInfo b = saveData.getChanges().getBlockInfo().get(blockOffset);
                    if (parent < 0 || !saveData.getChanges().getBlockInfo().get(parent).getVariables().containsKey("max")
                            || (saveData.getChanges().get(b.getStart()) != null && saveData.getChanges().get(b.getStart()).length == 0)) {
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
                        synchronized (saveData.getPlayerSkills()) {
                            saveData.getPlayerSkills().put(Objects.requireNonNull(Database.normalizeRecordPath(sb.getSkillName())),
                                    sb);
                        }
                    }
                }
            }
        }
    }

    public boolean isCharacterLoaded() {
        return saveData.getBuffer() != null;
    }

    public int getAvailableSkillPoints() {
        if (!isCharacterLoaded()) return 0;

        int block = saveData.getChanges().getVariableLocation().get(Constants.Save.SKILL_POINTS).get(0);
        BlockInfo statsBlock = saveData.getChanges().getBlockInfo().get(block);
        return getVariableValueInteger(statsBlock.getStart(), Constants.Save.SKILL_POINTS);
    }

    public Map<String, PlayerSkill> getPlayerSkills() {
        boolean update = false;

        for (PlayerSkill b : saveData.getPlayerSkills().values()) {
            if (saveData.getChanges().get(b.getBlockStart()) != null
                    && saveData.getChanges().get(b.getBlockStart()).length == 0) {
                //new block size is zero, was removed, ignore
                update = true;
            }
        }

        if (saveData.getPlayerSkills().isEmpty() || update) {
            prepareSkillsList();
        }

        return saveData.getPlayerSkills();
    }

    public int getMasteryLevel(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Skill detected.");
        }
        BlockInfo sk = saveData.getChanges().getBlockInfo().get(blockStart);
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

        BlockInfo skillToRemove = saveData.getChanges().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = skillToRemove.getVariables().get(Constants.Save.SKILL_LEVEL).get(0);
        if (varSkillLevel.getVariableType() == VariableType.INTEGER) {
            int currentSkillPoints = getVariableValueInteger(Constants.Save.SKILL_POINTS);
            int currentSkillLevel = (int) varSkillLevel.getValue();
            saveData.getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            saveData.getChanges().removeBlock(blockStart);
            saveData.getChanges().setInt("max", getVariableValueInteger("max") - 1);

            if (saveData.getChanges().get(blockStart) != null
                    && saveData.getChanges().get(blockStart).length == 0) {
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
            saveData.getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + currentSkillLevel);
            saveData.getChanges().removeBlock(blockStart);
            saveData.getChanges().setInt("max", getVariableValueInteger("max") - 1);
        }

        if (saveData.getChanges().get(blockStart) != null
                && saveData.getChanges().get(blockStart).length == 0) {
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
            saveData.getChanges().setInt(Constants.Save.SKILL_POINTS, currentSkillPoints + (currentSkillLevel - 1));
            saveData.getChanges().setInt(blockStart, Constants.Save.SKILL_LEVEL, 1);
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
        List<Integer> temp = saveData.getChanges().getVariableLocation().get("temp");
        for (Integer blockStart : temp) {
            BlockInfo b = saveData.getChanges().getBlockInfo().get(blockStart);
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
                saveData.getChanges().setFloat(attrVar, val);
            } else if (attrVar.getVariableType() == VariableType.INTEGER) {
                saveData.getChanges().setInt(attrVar, val);
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
        return Math.round(getVariableValueInteger("modifierPoints"));
    }

    public void setModifierPoints(int val) {
        saveData.getChanges().setInt("modifierPoints", val);
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

    private int getVariableValueInteger(VariableInfo variableInfo) {
        return saveData.getChanges().getInt(variableInfo);
    }

    private int getVariableValueInteger(String variable) {
        return saveData.getChanges().getInt(variable);
    }

    private int getVariableValueInteger(int blockStart, String variable) {
        return saveData.getChanges().getInt(blockStart, variable);
    }

    private float getVariableValueFloat(VariableInfo variableInfo) {
        return saveData.getChanges().getFloat(variableInfo);
    }

    public String getPlayerClassName() {
        String charClass = saveData.getPlayerClassTag();
        if (StringUtils.isNotEmpty(charClass)) {
            return txt.getString(charClass);
        }
        return charClass;
    }

    public int getDifficulty() {
        return getTempAttr("difficulty");
    }

    public List<TeleportDifficulty> getTeleports() {
        List<TeleportDifficulty> ret = new ArrayList<>();

        Optional<BlockInfo> first = saveData.getChanges().getBlockInfo().values().stream().filter(
                f -> f.getBlockType() == PlayerBlockType.PLAYER_MAIN).findFirst();

        BlockInfo playerMain = null;
        if (first.isPresent()) {
            playerMain = first.get();
        }

        for (int i = 0; i <= getDifficulty(); i++) {
            ret.add(getTeleportUidFromDifficulty(i, playerMain));
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

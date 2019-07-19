/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqdatabase.Database;
import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.gui.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@Singleton
public class PlayerData {
    private static final Logger logger = Log.getLogger();

    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private SaveData saveData;

    @Inject
    private ChangesTable changes;

    private static PlayerData instance = null;
    private String playerName = null;
    private ByteBuffer buffer = null;
    private boolean customQuest = false;
    private final LinkedHashMap<String, PlayerSkill> playerSkills;

    public PlayerData() {
        playerSkills = new LinkedHashMap<>();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    ChangesTable getChanges() {
        return changes;
    }

    boolean isCustomQuest() {
        return customQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        this.customQuest = customQuest;
    }

    public String getPlayerClassTag() {
        if (saveData.getHeaderInfo() != null) {
            return saveData.getHeaderInfo().getPlayerClassTag();
        }
        return null;
    }

    public Path getPlayerChr() {
        return Util.playerChr(playerName, customQuest);
    }

    public boolean loadPlayer(String playerName) {
        try {
            reset();
            PlayerParser playerParser = new PlayerParser();
            this.playerName = playerName;
            buffer = playerParser.loadPlayer(playerName, customQuest);
            saveData.setBlockInfo(playerParser.getBlockInfo());
            saveData.setHeaderInfo(playerParser.getHeaderInfo());
            saveData.setVariableLocation(playerParser.getVariableLocation());
            prepareSkillsList();
        } catch (Exception e) {
            reset();
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            throw new RuntimeException(e);
        }
        return true;
    }

    void prepareSkillsList() {
        playerSkills.clear();
        for (String v : saveData.getVariableLocation().keySet()) {
            if (v.startsWith(Database.Variables.PREFIX_SKILL_NAME)) {
                for (int blockOffset : saveData.getVariableLocation().get(v)) {
                    int parent = saveData.getBlockInfo().get(blockOffset).getParentOffset();
                    BlockInfo b = saveData.getBlockInfo().get(blockOffset);
                    if (parent < 0 || !saveData.getBlockInfo().get(parent).getVariables().containsKey("max")
                        || (changes.get(b.getStart()) != null && changes.get(b.getStart()).length == 0)) {
                            //new block size is zero (was removed) or no parent
                        continue;
                    }

                    PlayerSkill sb = new PlayerSkill();
                    sb.setSkillName((String) b.getVariables().get(Constants.Save.SKILL_NAME).getValue());
                    sb.setSkillEnabled((Integer) b.getVariables().get(Constants.Save.SKILL_ENABLED).getValue());
                    sb.setSkillActive((Integer) b.getVariables().get(Constants.Save.SKILL_ACTIVE).getValue());
                    sb.setSkillSubLevel((Integer) b.getVariables().get(Constants.Save.SKILL_SUB_LEVEL).getValue());
                    sb.setSkillTransition((Integer) b.getVariables().get(Constants.Save.SKILL_TRANSITION).getValue());
                    sb.setSkillLevel(changes.getInt(b.getStart(), Constants.Save.SKILL_LEVEL));
                    sb.setBlockStart(b.getStart());
                    if (sb.getSkillName() != null) {
                        synchronized (playerSkills) {
                            playerSkills.put(Objects.requireNonNull(Util.normalizeRecordPath(sb.getSkillName())),
                                    sb);
                        }
                    }
                }
            }
        }
    }

    public boolean isCharacterLoaded() {
        return buffer != null;
    }

    public int getAvailableSkillPoints() {
        if (!isCharacterLoaded()) return 0;

        int block = saveData.getVariableLocation().get("skillPoints").get(0);
        BlockInfo statsBlock = saveData.getBlockInfo().get(block);
        return changes.getInt(statsBlock.getStart(), "skillPoints");
    }

    public Map<String, PlayerSkill> getPlayerSkills() {
        boolean update = false;

        for (PlayerSkill b : playerSkills.values()) {
            if (changes.get(b.getBlockStart()) != null
                    && changes.get(b.getBlockStart()).length == 0) {
                //new block size is zero, was removed, ignore
                update = true;
            }
        }

        if (playerSkills.isEmpty() || update) {
            prepareSkillsList();
        }

        return playerSkills;
    }

    public int getMasteryLevel(PlayerSkill sb) {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Skill detected.");
        }
        BlockInfo sk = saveData.getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = sk.getVariables().get("skillLevel");

        if (varSkillLevel.getVariableType() == VariableType.Integer) {
            return changes.getInt(blockStart, "skillLevel");
        }
        return -1;
    }

    public void reclaimSkillPoints(PlayerSkill sb) throws Exception {
        int blockStart = sb.getBlockStart();
        Skill skill = db.skills().getSkill(sb.getSkillName(), false);
        if (skill.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Mastery detected.");
        }

        BlockInfo skillToRemove = saveData.getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = skillToRemove.getVariables().get("skillLevel");
        if (varSkillLevel.getVariableType() == VariableType.Integer) {
            int currentSkillPoints = changes.getInt("skillPoints");
            int currentSkillLevel = (int) varSkillLevel.getValue();
            changes.setInt("skillPoints", currentSkillPoints + currentSkillLevel);
            changes.removeBlock(blockStart);
            changes.setInt("max", changes.getInt("max") - 1);

            if (changes.get(blockStart) != null
                    && changes.get(blockStart).length == 0) {
                prepareSkillsList();
            }
        }
    }

    public void removeMastery(PlayerSkill sb) throws Exception {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error removing mastery. Not a mastery.");
        }
        List<Skill> currentSkillsInMastery = getPlayerSkillsFromMastery(mastery);
        if (!currentSkillsInMastery.isEmpty()) {
            throw new IllegalStateException("Mastery have skills, aborting.");
        }

        int currentSkillPoints = changes.getInt("skillPoints");
        int currentSkillLevel = changes.getInt(blockStart, "skillLevel");

        if (currentSkillLevel > 0) {
            changes.setInt("skillPoints", currentSkillPoints + currentSkillLevel);
            changes.removeBlock(blockStart);
            changes.setInt("max", changes.getInt("max") - 1);
        }

        if (changes.get(blockStart) != null
                && changes.get(blockStart).length == 0) {
            prepareSkillsList();
        }
    }

    public void reclaimMasteryPoints(PlayerSkill sb) throws Exception {
        int blockStart = sb.getBlockStart();
        Skill mastery = db.skills().getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Not a mastery.");
        }

        int currentSkillPoints = changes.getInt("skillPoints");
        int currentSkillLevel = changes.getInt(blockStart, "skillLevel");
        if (currentSkillLevel > 1) {
            changes.setInt("skillPoints", currentSkillPoints + (currentSkillLevel - 1));
            changes.setInt(blockStart, "skillLevel", 1);
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
            if (skill != null && !skill.isMastery() && skill.getParentPath().equals(mastery.getRecordPath())) {
                ret.add(skill);
            }
        }
        return ret;
    }

    public int getStr() {
        return Math.round(changes.getFloat("str"));
    }

    public void setStr(int val) throws Exception {
        changes.setFloat("str", val);
    }

    public int getInt() {
        return Math.round(changes.getFloat("int"));
    }

    public void setInt(int val) throws Exception {
        changes.setFloat("int", val);
    }

    public int getDex() {
        return Math.round(changes.getFloat("dex"));
    }

    public void setDex(int val) throws Exception {
        changes.setFloat("dex", val);
    }

    public int getLife() {
        return Math.round(changes.getFloat("life"));
    }

    public void setLife(int val) throws Exception {
        changes.setFloat("life", val);
    }

    public int getMana() {
        return Math.round(changes.getFloat("mana"));
    }

    public void setMana(int val) throws Exception {
        changes.setFloat("mana", val);
    }

    public int getModifierPoints() {
        return Math.round(changes.getInt("modifierPoints"));
    }

    public void setModifierPoints(int val) throws Exception {
        changes.setInt("modifierPoints", val);
    }

    public int getXp() {
        return changes.getInt("currentStats.experiencePoints");
    }

    public int getLevel() {
        return changes.getInt("currentStats.charLevel");
    }

    public int getMoney() {
        return changes.getInt("money");
    }

    public String getPlayerClassName() {
        String charClass = getPlayerClassTag();
        if (StringUtils.isNotEmpty(charClass)) {
            return txt.getString(charClass);
        }
        return charClass;
    }

    public int getDifficulty() {
        return changes.getInt("difficulty");
    }

    public void reset() {
        this.buffer = null;
        this.playerName = null;
        this.changes.clear();
        State.get().setSaveInProgress(null);
        saveData.reset();
    }


}

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
import br.com.pinter.tqrespec.Constants;
import br.com.pinter.tqrespec.Util;
import br.com.pinter.tqrespec.tqdata.SkillUtils;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Objects;

@SuppressWarnings("unused")
public class PlayerData {
    private static PlayerData instance = null;
    private String playerName = null;
    private Path playerChr = null;
    private ByteBuffer buffer = null;
    private Hashtable<Integer, BlockInfo> blockInfo = null;
    private HeaderInfo headerInfo = null;
    private Hashtable<String, ArrayList<Integer>> variableLocation = null;
    private ChangesTable changes = null;
    private Boolean saveInProgress = null;
    private boolean isCustomQuest = false;
    private final LinkedHashMap<String, SkillBlock> skillBlocks;

    public PlayerData() {
        skillBlocks = new LinkedHashMap<>();
    }

    public static PlayerData getInstance() {
        if (instance == null) {
            synchronized (PlayerData.class) {
                if (instance == null) {
                    instance = new PlayerData();
                    instance.changes = new ChangesTable();
                    instance.variableLocation = new Hashtable<>();
                    instance.blockInfo = new Hashtable<>();
                }
            }
        }
        return instance;
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

    public Hashtable<Integer, BlockInfo> getBlockInfo() {
        return blockInfo;
    }

    public void setBlockInfo(Hashtable<Integer, BlockInfo> blockInfo) {
        this.blockInfo = blockInfo;
    }

    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }

    public Hashtable<String, ArrayList<Integer>> getVariableLocation() {
        return variableLocation;
    }

    public void setVariableLocation(Hashtable<String, ArrayList<Integer>> variableLocation) {
        this.variableLocation = variableLocation;
    }

    public ChangesTable getChanges() {
        return changes;
    }

    public Path getPlayerChr() {
        return playerChr;
    }

    public void setPlayerChr(Path playerChr) {
        this.playerChr = playerChr;
    }

    public Boolean getSaveInProgress() {
        return saveInProgress;
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress = saveInProgress;
    }

    public boolean isCustomQuest() {
        return isCustomQuest;
    }

    public void setCustomQuest(boolean customQuest) {
        isCustomQuest = customQuest;
    }

    public boolean loadPlayerData(String playerName) throws Exception {
        if (PlayerData.getInstance().getSaveInProgress() != null && PlayerData.getInstance().getSaveInProgress()) {
            return false;
        }
        new PlayerParser().player(playerName).parse();
        prepareSkillsList();
        return true;
    }

    private void prepareSkillsList() {
        skillBlocks.clear();
        for (String v : PlayerData.getInstance().getVariableLocation().keySet()) {
            if (v.startsWith(Database.Variables.PREFIX_SKILL_NAME)) {
                for (int blockOffset : PlayerData.getInstance().getVariableLocation().get(v)) {
                    BlockInfo b = PlayerData.getInstance().getBlockInfo().get(blockOffset);
                    if (PlayerData.getInstance().getChanges().get(b.getStart()) != null
                            && PlayerData.getInstance().getChanges().get(b.getStart()).length == 0) {
                        //new block size is zero, was removed, ignore
                        continue;
                    }

                    SkillBlock sb = new SkillBlock();
                    sb.setSkillName((String) b.getVariables().get(Constants.Save.SKILL_NAME).getValue());
                    sb.setSkillEnabled((Integer) b.getVariables().get(Constants.Save.SKILL_ENABLED).getValue());
                    sb.setSkillActive((Integer) b.getVariables().get(Constants.Save.SKILL_ACTIVE).getValue());
                    sb.setSkillSubLevel((Integer) b.getVariables().get(Constants.Save.SKILL_SUB_LEVEL).getValue());
                    sb.setSkillTransition((Integer) b.getVariables().get(Constants.Save.SKILL_TRANSITION).getValue());
                    sb.setSkillLevel(PlayerData.getInstance().getChanges().getInt(b.getStart(), Constants.Save.SKILL_LEVEL));
                    sb.setBlockStart(b.getStart());
                    if (sb.getSkillName() != null) {
                        synchronized (skillBlocks) {
                            skillBlocks.put(Objects.requireNonNull(Util.normalizeRecordPath(sb.getSkillName())),
                                    sb);
                        }
                    }
                }
            }
        }
    }

    public boolean isCharacterLoaded() {
        return PlayerData.getInstance().getBuffer() != null;
    }

    public int getAvailableSkillPoints() {
        if (!isCharacterLoaded()) return 0;

        int block = PlayerData.getInstance().getVariableLocation().get("skillPoints").get(0);
        BlockInfo statsBlock = PlayerData.getInstance().getBlockInfo().get(block);
        return PlayerData.getInstance().getChanges().getInt(statsBlock.getStart(), "skillPoints");
    }

    public LinkedHashMap<String, SkillBlock> getSkillBlocks() {
        boolean update = false;

        for (SkillBlock b : skillBlocks.values()) {
            if (PlayerData.getInstance().getChanges().get(b.getBlockStart()) != null
                    && PlayerData.getInstance().getChanges().get(b.getBlockStart()).length == 0) {
                //new block size is zero, was removed, ignore
                update = true;
            }
        }

        if (skillBlocks.isEmpty() || update) {
            prepareSkillsList();
        }

        return skillBlocks;
    }

    public void reclaimSkillPoints(SkillBlock sb) throws Exception {
        int blockStart = sb.getBlockStart();
        Skill skill = SkillUtils.getSkill(sb.getSkillName(), false);
        if (skill.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Mastery detected.");
        }

        BlockInfo skillToRemove = PlayerData.getInstance().getBlockInfo().get(blockStart);
        VariableInfo varSkillLevel = skillToRemove.getVariables().get("skillLevel");
        if (varSkillLevel.getVariableType() == VariableInfo.VariableType.Integer) {
            int currentSkillPoints = PlayerData.getInstance().getChanges().getInt("skillPoints");
            int currentSkillLevel = (int) varSkillLevel.getValue();
            PlayerData.getInstance().getChanges().setInt("skillPoints", currentSkillPoints + currentSkillLevel);
            PlayerData.getInstance().getChanges().removeBlock(blockStart);
            PlayerData.getInstance().getChanges().setInt("max", PlayerData.getInstance().getChanges().getInt("max") - 1);

            if (PlayerData.getInstance().getChanges().get(blockStart) != null
                    && PlayerData.getInstance().getChanges().get(blockStart).length == 0) {
                prepareSkillsList();
            }
        }
    }

    public void reclaimMasteryPoints(SkillBlock sb) throws Exception {
        int blockStart = sb.getBlockStart();
        Skill mastery = SkillUtils.getSkill(sb.getSkillName(), false);
        if (!mastery.isMastery()) {
            throw new IllegalStateException("Error reclaiming points. Not a mastery.");
        }

        int currentSkillPoints = PlayerData.getInstance().getChanges().getInt("skillPoints");
        int currentSkillLevel = PlayerData.getInstance().getChanges().getInt(blockStart, "skillLevel");
        if(currentSkillLevel > 1) {
            PlayerData.getInstance().getChanges().setInt("skillPoints", currentSkillPoints + (currentSkillLevel - 1));
            PlayerData.getInstance().getChanges().setInt(blockStart, "skillLevel", 1);
            prepareSkillsList();
        }
    }

    public void reset() {
        this.buffer = null;
        this.headerInfo = null;
        this.blockInfo = new Hashtable<>();
        this.playerName = null;
        this.variableLocation = new Hashtable<>();
        this.changes = new ChangesTable();
        this.playerChr = null;
        this.saveInProgress = null;
    }
}

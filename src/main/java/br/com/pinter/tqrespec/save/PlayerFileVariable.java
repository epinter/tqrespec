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

import java.util.HashMap;

public class PlayerFileVariable implements IFileVariable {
    private static HashMap<String, PlayerFileVariable> map = new HashMap<>();

    private final FileBlockType location;
    private final String var;
    private final VariableType type;

    static {
        map.put("headerVersion", new PlayerFileVariable("headerVersion", VariableType.INTEGER, FileBlockType.PLAYER_HEADER));
        map.put("playerCharacterClass", new PlayerFileVariable("playerCharacterClass", VariableType.STRING, FileBlockType.PLAYER_HEADER));
        map.put("uniqueId", new PlayerFileVariable("uniqueId", VariableType.UID, FileBlockType.PLAYER_HEADER));
        map.put("streamData", new PlayerFileVariable("streamData", VariableType.STREAM, FileBlockType.PLAYER_HEADER));
        map.put("playerClassTag", new PlayerFileVariable("playerClassTag", VariableType.STRING, FileBlockType.PLAYER_HEADER));
        map.put("playerVersion", new PlayerFileVariable("playerVersion", VariableType.INTEGER, FileBlockType.PLAYER_HEADER));
        map.put("playerLevel", new PlayerFileVariable("playerLevel", VariableType.INTEGER, FileBlockType.PLAYER_HEADER));
        map.put("controllerStreamed", new PlayerFileVariable("controllerStreamed", VariableType.INTEGER, FileBlockType.PLAYER_HEADER));
        map.put("description", new PlayerFileVariable("description", VariableType.STREAM, FileBlockType.PLAYER_HEADER));

        map.put("playerTexture", new PlayerFileVariable("playerTexture", VariableType.STRING, FileBlockType.PLAYER_MAIN));
        map.put("myPlayerName", new PlayerFileVariable("myPlayerName", VariableType.STRING_UTF_16_LE, FileBlockType.PLAYER_MAIN));
        map.put("isInMainQuest", new PlayerFileVariable("isInMainQuest", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("disableAutoPopV2", new PlayerFileVariable("disableAutoPopV2", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("numTutorialPagesV2", new PlayerFileVariable("numTutorialPagesV2", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("currentPageV2", new PlayerFileVariable("currentPageV2", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("teleportUIDsSize", new PlayerFileVariable("teleportUIDsSize", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("markerUIDsSize", new PlayerFileVariable("markerUIDsSize", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("respawnUIDsSize", new PlayerFileVariable("respawnUIDsSize", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("versionCheckRespawnInfo", new PlayerFileVariable("versionCheckRespawnInfo", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("versionCheckTeleportInfo", new PlayerFileVariable("versionCheckTeleportInfo", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("versionCheckMovementInfo", new PlayerFileVariable("versionCheckMovementInfo", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("compassState", new PlayerFileVariable("compassState", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("skillWindowShowHelp", new PlayerFileVariable("skillWindowShowHelp", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("alternateConfig", new PlayerFileVariable("alternateConfig", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("alternateConfigEnabled", new PlayerFileVariable("alternateConfigEnabled", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("itemsFoundOverLifetimeUniqueTotal", new PlayerFileVariable("itemsFoundOverLifetimeUniqueTotal", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("itemsFoundOverLifetimeRandomizedTotal", new PlayerFileVariable("itemsFoundOverLifetimeRandomizedTotal", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("hasBeenInGame", new PlayerFileVariable("hasBeenInGame", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("versionRespawnPoint", new PlayerFileVariable("versionRespawnPoint", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("money", new PlayerFileVariable("money", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
        map.put("teleportUID", new PlayerFileVariable("teleportUID", VariableType.UID, FileBlockType.PLAYER_MAIN));
        map.put("respawnUID", new PlayerFileVariable("respawnUID", VariableType.UID, FileBlockType.PLAYER_MAIN));
        map.put("markerUID", new PlayerFileVariable("markerUID", VariableType.UID, FileBlockType.PLAYER_MAIN));
        map.put("strategicMovementRespawnPoint", new PlayerFileVariable("strategicMovementRespawnPoint[i]", VariableType.UID, FileBlockType.PLAYER_MAIN));

        map.put("skillName", new PlayerFileVariable("skillName", VariableType.STRING, FileBlockType.BODY));//PLAYER_SKILLS + PLAYER_HOT_SLOT
        map.put("skillActive", new PlayerFileVariable("skillActive", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("skillLevel", new PlayerFileVariable("skillLevel", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("skillEnabled", new PlayerFileVariable("skillEnabled", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("skillSubLevel", new PlayerFileVariable("skillSubLevel", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("skillTransition", new PlayerFileVariable("skillTransition", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("max", new PlayerFileVariable("max", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("masteriesAllowed", new PlayerFileVariable("masteriesAllowed", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));
        map.put("skillReclamationPointsUsed", new PlayerFileVariable("skillReclamationPointsUsed", VariableType.INTEGER, FileBlockType.PLAYER_SKILLS));

        map.put("defaultText", new PlayerFileVariable("defaultText", VariableType.STRING_UTF_16_LE, FileBlockType.BODY));
        map.put("itemPositionsSavedAsGridCoords", new PlayerFileVariable("itemPositionsSavedAsGridCoords", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("numberOfSacks", new PlayerFileVariable("numberOfSacks", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("currentlyFocusedSackNumber", new PlayerFileVariable("currentlyFocusedSackNumber", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("currentlySelectedSackNumber", new PlayerFileVariable("currentlySelectedSackNumber", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("tempBool", new PlayerFileVariable("tempBool", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("size", new PlayerFileVariable("size", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("seed", new PlayerFileVariable("seed", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("var1", new PlayerFileVariable("var1", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("var2", new PlayerFileVariable("var2", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("pointX", new PlayerFileVariable("pointX", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("pointY", new PlayerFileVariable("pointY", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("baseName", new PlayerFileVariable("baseName", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("prefixName", new PlayerFileVariable("prefixName", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("suffixName", new PlayerFileVariable("suffixName", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("relicName", new PlayerFileVariable("relicName", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("relicBonus", new PlayerFileVariable("relicBonus", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("relicName2", new PlayerFileVariable("relicName2", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("relicBonus2", new PlayerFileVariable("relicBonus2", VariableType.STRING, FileBlockType.PLAYER_INVENTORY));
        map.put("useAlternate", new PlayerFileVariable("useAlternate", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("equipmentCtrlIOStreamVersion", new PlayerFileVariable("equipmentCtrlIOStreamVersion", VariableType.INTEGER, FileBlockType.PLAYER_INVENTORY));
        map.put("storedType", new PlayerFileVariable("storedType", VariableType.INTEGER, FileBlockType.PLAYER_HOT_SLOT));
        map.put("itemAttached", new PlayerFileVariable("itemAttached", VariableType.INTEGER, FileBlockType.PLAYER_HOT_SLOT));
        map.put("alternate", new PlayerFileVariable("alternate", VariableType.INTEGER, FileBlockType.PLAYER_HOT_SLOT));
        map.put("isItemSkill", new PlayerFileVariable("isItemSkill", VariableType.INTEGER, FileBlockType.PLAYER_HOT_SLOT));
        map.put("itemName", new PlayerFileVariable("itemName", VariableType.STRING, FileBlockType.PLAYER_HOT_SLOT));
        map.put("scrollName", new PlayerFileVariable("scrollName", VariableType.STRING, FileBlockType.PLAYER_HOT_SLOT));
        map.put("bitmapUpName", new PlayerFileVariable("bitmapUpName", VariableType.STRING, FileBlockType.PLAYER_HOT_SLOT));
        map.put("bitmapDownName", new PlayerFileVariable("bitmapDownName", VariableType.STRING, FileBlockType.PLAYER_HOT_SLOT));
        map.put("storedDefaultType", new PlayerFileVariable("storedDefaultType", VariableType.INTEGER, FileBlockType.PLAYER_HOT_SLOT));

        map.put("equipmentSelection", new PlayerFileVariable("equipmentSelection", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillActive1", new PlayerFileVariable("skillActive1", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillActive2", new PlayerFileVariable("skillActive2", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillActive3", new PlayerFileVariable("skillActive3", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillActive4", new PlayerFileVariable("skillActive4", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillActive5", new PlayerFileVariable("skillActive5", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill1", new PlayerFileVariable("primarySkill11", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill2", new PlayerFileVariable("primarySkill12", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill3", new PlayerFileVariable("primarySkill13", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill4", new PlayerFileVariable("primarySkill14", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill5", new PlayerFileVariable("primarySkill15", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill", new PlayerFileVariable("secondarySkill", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill1", new PlayerFileVariable("secondarySkill11", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill2", new PlayerFileVariable("secondarySkill12", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill3", new PlayerFileVariable("secondarySkill13", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill4", new PlayerFileVariable("secondarySkill14", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill5", new PlayerFileVariable("secondarySkill15", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillWindowSelection", new PlayerFileVariable("skillWindowSelection", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));
        map.put("skillSettingValid", new PlayerFileVariable("skillSettingValid", VariableType.INTEGER, FileBlockType.PLAYER_UI_SKILL));

        map.put("modifierPoints", new PlayerFileVariable("modifierPoints", VariableType.INTEGER, FileBlockType.PLAYER_LEVEL_POINTS));
        map.put("skillPoints", new PlayerFileVariable("skillPoints", VariableType.INTEGER, FileBlockType.PLAYER_LEVEL_POINTS));
        map.put("currentStats_experiencePoints", new PlayerFileVariable("currentStats.experiencePoints", VariableType.INTEGER, FileBlockType.PLAYER_LEVEL_POINTS));
        map.put("currentStats_charLevel", new PlayerFileVariable("currentStats.charLevel", VariableType.INTEGER, FileBlockType.PLAYER_LEVEL_POINTS));

        map.put("playTimeInSeconds", new PlayerFileVariable("playTimeInSeconds", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledName", new PlayerFileVariable("greatestMonsterKilledName", VariableType.STRING_UTF_16_LE, FileBlockType.PLAYER_STATS));
        map.put("numberOfDeaths", new PlayerFileVariable("numberOfDeaths", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("numberOfKills", new PlayerFileVariable("numberOfKills", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("experienceFromKills", new PlayerFileVariable("experienceFromKills", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("healthPotionsUsed", new PlayerFileVariable("healthPotionsUsed", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("manaPotionsUsed", new PlayerFileVariable("manaPotionsUsed", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("maxLevel", new PlayerFileVariable("maxLevel", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("numHitsReceived", new PlayerFileVariable("numHitsReceived", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("numHitsInflicted", new PlayerFileVariable("numHitsInflicted", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("greatestDamageInflicted", new PlayerFileVariable("greatestDamageInflicted", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledLevel", new PlayerFileVariable("(*greatestMonsterKilledLevel)[i]", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledLifeAndMana", new PlayerFileVariable("(*greatestMonsterKilledLifeAndMana)[i]", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("criticalHitsInflicted", new PlayerFileVariable("criticalHitsInflicted", VariableType.INTEGER, FileBlockType.PLAYER_STATS));
        map.put("criticalHitsReceived", new PlayerFileVariable("criticalHitsReceived", VariableType.INTEGER, FileBlockType.PLAYER_STATS));

        //repeated variables with different types, should be named (name)__(blockname)
        map.put("temp", new PlayerFileVariable("temp", VariableType.UNKNOWN, FileBlockType.MULTIPLE));
        map.put("temp__" + FileBlockType.PLAYER_ATTRIBUTES, new PlayerFileVariable("temp", VariableType.FLOAT, FileBlockType.PLAYER_ATTRIBUTES));
        map.put("temp__" + FileBlockType.PLAYER_MAIN, new PlayerFileVariable("temp", VariableType.INTEGER, FileBlockType.PLAYER_MAIN));
    }

    private PlayerFileVariable(String variable, VariableType type, FileBlockType location) {
        this.var = variable;
        this.type = type;
        this.location = location;
    }

    static PlayerFileVariable valueOf(String var) {
        return map.get(var);
    }

    @Override
    public String var() {
        return var;
    }

    @Override
    public VariableType type() {
        return type;
    }

    @Override
    public FileBlockType location() {
        return location;
    }
}

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

import br.com.pinter.tqrespec.save.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerFileVariable implements FileVariable {
    private static final Map<Platform, HashMap<String, PlayerFileVariable>> variablesMap = new EnumMap<>(Platform.class);

    static {
        HashMap<String, PlayerFileVariable> map = new HashMap<>();
        map.put("headerVersion", new PlayerFileVariable("headerVersion", VariableType.INTEGER, PlayerBlockType.PLAYER_HEADER));
        map.put("playerCharacterClass", new PlayerFileVariable("playerCharacterClass", VariableType.STRING, PlayerBlockType.PLAYER_HEADER));
        map.put("uniqueId", new PlayerFileVariable("uniqueId", VariableType.UID, PlayerBlockType.PLAYER_HEADER));
        map.put("streamData", new PlayerFileVariable("streamData", VariableType.STREAM, PlayerBlockType.PLAYER_HEADER));
        map.put("playerClassTag", new PlayerFileVariable("playerClassTag", VariableType.STRING, PlayerBlockType.PLAYER_HEADER));
        map.put("playerVersion", new PlayerFileVariable("playerVersion", VariableType.INTEGER, PlayerBlockType.PLAYER_HEADER));
        map.put("playerLevel", new PlayerFileVariable("playerLevel", VariableType.INTEGER, PlayerBlockType.PLAYER_HEADER));
        map.put("controllerStreamed", new PlayerFileVariable("controllerStreamed", VariableType.INTEGER, PlayerBlockType.PLAYER_HEADER));
        map.put("description", new PlayerFileVariable("description", VariableType.STREAM, PlayerBlockType.PLAYER_HEADER));

        map.put("playerTexture", new PlayerFileVariable("playerTexture", VariableType.STRING, PlayerBlockType.PLAYER_MAIN));
        map.put("myPlayerName", new PlayerFileVariable("myPlayerName", VariableType.STRING_UTF_16_LE, PlayerBlockType.PLAYER_MAIN));
        map.put("isInMainQuest", new PlayerFileVariable("isInMainQuest", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("disableAutoPopV2", new PlayerFileVariable("disableAutoPopV2", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("numTutorialPagesV2", new PlayerFileVariable("numTutorialPagesV2", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("currentPageV2", new PlayerFileVariable("currentPageV2", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("teleportUIDsSize", new PlayerFileVariable("teleportUIDsSize", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("markerUIDsSize", new PlayerFileVariable("markerUIDsSize", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("respawnUIDsSize", new PlayerFileVariable("respawnUIDsSize", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("versionCheckRespawnInfo", new PlayerFileVariable("versionCheckRespawnInfo", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("versionCheckTeleportInfo", new PlayerFileVariable("versionCheckTeleportInfo", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("versionCheckMovementInfo", new PlayerFileVariable("versionCheckMovementInfo", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("compassState", new PlayerFileVariable("compassState", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("skillWindowShowHelp", new PlayerFileVariable("skillWindowShowHelp", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("alternateConfig", new PlayerFileVariable("alternateConfig", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("alternateConfigEnabled", new PlayerFileVariable("alternateConfigEnabled", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("itemsFoundOverLifetimeUniqueTotal", new PlayerFileVariable("itemsFoundOverLifetimeUniqueTotal", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("itemsFoundOverLifetimeRandomizedTotal", new PlayerFileVariable("itemsFoundOverLifetimeRandomizedTotal", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("hasBeenInGame", new PlayerFileVariable("hasBeenInGame", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("boostedCharacterForX4", new PlayerFileVariable("boostedCharacterForX4", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("tartarusDefeatedCount", new PlayerFileVariable("tartarusDefeatedCount[i]", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("versionRespawnPoint", new PlayerFileVariable("versionRespawnPoint", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("money", new PlayerFileVariable("money", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("altMoney", new PlayerFileVariable("altMoney", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("teleportUID", new PlayerFileVariable("teleportUID", VariableType.UID, PlayerBlockType.PLAYER_MAIN));
        map.put("respawnUID", new PlayerFileVariable("respawnUID", VariableType.UID, PlayerBlockType.PLAYER_MAIN));
        map.put("markerUID", new PlayerFileVariable("markerUID", VariableType.UID, PlayerBlockType.PLAYER_MAIN));
        map.put("strategicMovementRespawnPoint", new PlayerFileVariable("strategicMovementRespawnPoint[i]", VariableType.UID, PlayerBlockType.PLAYER_MAIN));

        map.put("skillActive", new PlayerFileVariable("skillActive", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILL));
        map.put("skillLevel", new PlayerFileVariable("skillLevel", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILL));
        map.put("skillEnabled", new PlayerFileVariable("skillEnabled", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILL));
        map.put("skillSubLevel", new PlayerFileVariable("skillSubLevel", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILL));
        map.put("skillTransition", new PlayerFileVariable("skillTransition", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILL));
        map.put("max", new PlayerFileVariable("max", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));
        map.put("masteriesAllowed", new PlayerFileVariable("masteriesAllowed", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));
        map.put("skillReclamationPointsUsed", new PlayerFileVariable("skillReclamationPointsUsed", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));
        map.put("hasSkillServices", new PlayerFileVariable("hasSkillServices", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));
        map.put("version", new PlayerFileVariable("version", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));

        map.put("defaultText", new PlayerFileVariable("defaultText", VariableType.STRING_UTF_16_LE, FileBlockType.UNKNOWN));
        map.put("itemPositionsSavedAsGridCoords", new PlayerFileVariable("itemPositionsSavedAsGridCoords", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY));
        map.put("numberOfSacks", new PlayerFileVariable("numberOfSacks", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY));
        map.put("currentlyFocusedSackNumber", new PlayerFileVariable("currentlyFocusedSackNumber", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY));
        map.put("currentlySelectedSackNumber", new PlayerFileVariable("currentlySelectedSackNumber", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY));
        map.put("tempBool", new PlayerFileVariable("tempBool", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY_SACK));
        map.put("seed", new PlayerFileVariable("seed", VariableType.INTEGER, PlayerBlockType.PLAYER_ITEM));
        map.put("var1", new PlayerFileVariable("var1", VariableType.INTEGER, PlayerBlockType.PLAYER_ITEM));
        map.put("var2", new PlayerFileVariable("var2", VariableType.INTEGER, PlayerBlockType.PLAYER_ITEM));
        map.put("pointX", new PlayerFileVariable("pointX", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY_ITEMCONTAINER));
        map.put("pointY", new PlayerFileVariable("pointY", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY_ITEMCONTAINER));
        map.put("baseName", new PlayerFileVariable("baseName", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("prefixName", new PlayerFileVariable("prefixName", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("suffixName", new PlayerFileVariable("suffixName", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("relicName", new PlayerFileVariable("relicName", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("relicBonus", new PlayerFileVariable("relicBonus", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("relicName2", new PlayerFileVariable("relicName2", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("relicBonus2", new PlayerFileVariable("relicBonus2", VariableType.STRING, PlayerBlockType.PLAYER_ITEM));
        map.put("useAlternate", new PlayerFileVariable("useAlternate", VariableType.INTEGER, PlayerBlockType.PLAYER_EQUIPMENT));
        map.put("equipmentCtrlIOStreamVersion", new PlayerFileVariable("equipmentCtrlIOStreamVersion", VariableType.INTEGER, PlayerBlockType.PLAYER_EQUIPMENT));
        map.put("storedType", new PlayerFileVariable("storedType", VariableType.INTEGER, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("itemAttached", new PlayerFileVariable("itemAttached", VariableType.INTEGER, PlayerBlockType.PLAYER_EQUIPMENT));
        map.put("alternate", new PlayerFileVariable("alternate", VariableType.INTEGER, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("isItemSkill", new PlayerFileVariable("isItemSkill", VariableType.INTEGER, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("itemName", new PlayerFileVariable("itemName", VariableType.STRING, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("scrollName", new PlayerFileVariable("scrollName", VariableType.STRING, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("bitmapUpName", new PlayerFileVariable("bitmapUpName", VariableType.STRING, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("bitmapDownName", new PlayerFileVariable("bitmapDownName", VariableType.STRING, PlayerBlockType.PLAYER_HOT_SLOT));
        map.put("storedDefaultType", new PlayerFileVariable("storedDefaultType", VariableType.INTEGER, PlayerBlockType.PLAYER_HOT_SLOT));

        map.put("equipmentSelection", new PlayerFileVariable("equipmentSelection", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillActive1", new PlayerFileVariable("skillActive1", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillActive2", new PlayerFileVariable("skillActive2", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillActive3", new PlayerFileVariable("skillActive3", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillActive4", new PlayerFileVariable("skillActive4", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillActive5", new PlayerFileVariable("skillActive5", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill1", new PlayerFileVariable("primarySkill1", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill2", new PlayerFileVariable("primarySkill2", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill3", new PlayerFileVariable("primarySkill3", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill4", new PlayerFileVariable("primarySkill4", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("primarySkill5", new PlayerFileVariable("primarySkill5", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill", new PlayerFileVariable("secondarySkill", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill1", new PlayerFileVariable("secondarySkill1", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill2", new PlayerFileVariable("secondarySkill2", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill3", new PlayerFileVariable("secondarySkill3", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill4", new PlayerFileVariable("secondarySkill4", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("secondarySkill5", new PlayerFileVariable("secondarySkill5", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillWindowSelection", new PlayerFileVariable("skillWindowSelection", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));
        map.put("skillSettingValid", new PlayerFileVariable("skillSettingValid", VariableType.INTEGER, PlayerBlockType.PLAYER_UI_SKILL));

        map.put("modifierPoints", new PlayerFileVariable("modifierPoints", VariableType.INTEGER, PlayerBlockType.PLAYER_LEVEL_POINTS));
        map.put("skillPoints", new PlayerFileVariable("skillPoints", VariableType.INTEGER, PlayerBlockType.PLAYER_LEVEL_POINTS));
        map.put("currentStats_experiencePoints", new PlayerFileVariable("currentStats.experiencePoints", VariableType.INTEGER, PlayerBlockType.PLAYER_LEVEL_POINTS));
        map.put("currentStats_charLevel", new PlayerFileVariable("currentStats.charLevel", VariableType.INTEGER, PlayerBlockType.PLAYER_LEVEL_POINTS));

        map.put("playTimeInSeconds", new PlayerFileVariable("playTimeInSeconds", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledName", new PlayerFileVariable("(*greatestMonsterKilledName)[i]", VariableType.STRING_UTF_16_LE, PlayerBlockType.PLAYER_STATS));
        map.put("numberOfDeaths", new PlayerFileVariable("numberOfDeaths", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("numberOfKills", new PlayerFileVariable("numberOfKills", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("experienceFromKills", new PlayerFileVariable("experienceFromKills", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("healthPotionsUsed", new PlayerFileVariable("healthPotionsUsed", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("manaPotionsUsed", new PlayerFileVariable("manaPotionsUsed", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("maxLevel", new PlayerFileVariable("maxLevel", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("numHitsReceived", new PlayerFileVariable("numHitsReceived", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("numHitsInflicted", new PlayerFileVariable("numHitsInflicted", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("greatestDamageInflicted", new PlayerFileVariable("greatestDamageInflicted", VariableType.FLOAT, PlayerBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledLevel", new PlayerFileVariable("(*greatestMonsterKilledLevel)[i]", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("greatestMonsterKilledLifeAndMana", new PlayerFileVariable("(*greatestMonsterKilledLifeAndMana)[i]", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("criticalHitsInflicted", new PlayerFileVariable("criticalHitsInflicted", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));
        map.put("criticalHitsReceived", new PlayerFileVariable("criticalHitsReceived", VariableType.INTEGER, PlayerBlockType.PLAYER_STATS));

        //repeated variables with different types, should be named (name)__(blockname)
        map.put("temp", new PlayerFileVariable("temp", VariableType.UNKNOWN, FileBlockType.MULTIPLE));
        map.put("size", new PlayerFileVariable("size", VariableType.UNKNOWN, FileBlockType.MULTIPLE));
        map.put("skillName", new PlayerFileVariable("skillName", VariableType.UNKNOWN, FileBlockType.MULTIPLE));
        map.put("temp__" + PlayerBlockType.PLAYER_ATTRIBUTES, new PlayerFileVariable("temp", VariableType.FLOAT, PlayerBlockType.PLAYER_ATTRIBUTES));
        map.put("temp__" + PlayerBlockType.PLAYER_MAIN, new PlayerFileVariable("temp", VariableType.INTEGER, PlayerBlockType.PLAYER_MAIN));
        map.put("size__" + PlayerBlockType.PLAYER_SKILLSLIST, new PlayerFileVariable("size", VariableType.INTEGER, PlayerBlockType.PLAYER_SKILLSLIST));
        map.put("size__" + PlayerBlockType.PLAYER_INVENTORY_SACK, new PlayerFileVariable("size", VariableType.INTEGER, PlayerBlockType.PLAYER_INVENTORY_SACK));
        map.put("skillName__" + PlayerBlockType.PLAYER_SKILL, new PlayerFileVariable("skillName", VariableType.STRING, PlayerBlockType.PLAYER_SKILL));
        map.put("skillName__" + PlayerBlockType.PLAYER_HOT_SLOT, new PlayerFileVariable("skillName", VariableType.STRING, PlayerBlockType.PLAYER_HOT_SLOT));

        HashMap<String, PlayerFileVariable> mapMobile = new HashMap<>(map);
        mapMobile.put("myPlayerName", new PlayerFileVariable("myPlayerName", VariableType.STRING_UTF_32_LE, PlayerBlockType.PLAYER_MAIN));
        mapMobile.put("defaultText", new PlayerFileVariable("defaultText", VariableType.STRING_UTF_32_LE, FileBlockType.UNKNOWN));
        mapMobile.put("greatestMonsterKilledName", new PlayerFileVariable("(*greatestMonsterKilledName)[i]", VariableType.STRING_UTF_32_LE, PlayerBlockType.PLAYER_STATS));
        mapMobile.put("mySaveId", new PlayerFileVariable("mySaveId", VariableType.STRING, PlayerBlockType.PLAYER_MAIN));
        mapMobile.put("currentDifficulty", new PlayerFileVariable("currentDifficulty", VariableType.INTEGER, PlayerBlockType.PLAYER_HEADER));
        variablesMap.put(Platform.WINDOWS, map);
        variablesMap.put(Platform.MOBILE, mapMobile);
    }

    private final BlockType location;
    private final String var;
    private final VariableType type;

    private PlayerFileVariable(String variable, VariableType type, BlockType location) {
        this.var = variable;
        this.type = type;
        this.location = location;
    }

    /**
     * Returns the PlayerFileVariable from 'platform' that has a key equal to the parameter 'var'
     *
     * @param platform Platform to get the variable from
     * @param key Key that represents the variable in the map (may not be the actual variable name)
     * @return PlayerFileVariable instance
     */
    public static PlayerFileVariable valueOf(Platform platform, String key) {
        if (variablesMap.get(platform) == null || variablesMap.get(platform).get(key) == null) {
            throw new InvalidVariableException(String.format("variable '%s' not found for platform '%s'", key.replaceAll("[^a-zA-Z0-9-_\\[\\] ]*", ""), platform));
        }
        return variablesMap.get(platform).get(key);
    }

    /**
     * Returns a PlayerFileVariable from the 'platform' map
     *
     * @param platform Platform to get the variable from
     * @param variableName Must be a valid savegame variable
     * @return {@link PlayerFileVariable}
     */
    public static PlayerFileVariable getVar(Platform platform, String variableName) {
        PlayerFileVariable ret = null;
        if (variablesMap.get(platform) != null) {
            ret = variablesMap.get(platform).values().stream().filter((v) -> v.var.equals(variableName))
                    .findFirst().orElse(null);
        }

        if (ret != null) {
            return ret;
        }

        throw new InvalidVariableException(String.format("variable '%s' not found for platform '%s'", variableName.replaceAll("[^a-zA-Z0-9-_\\[\\] ]*", ""), platform));
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
    public BlockType location() {
        return location;
    }

    public static BlockType getBlockTypeFromParent(Platform platform, BlockType parent, String varName) {
        if (variablesMap.get(platform) != null) {
            for (Map.Entry<String, PlayerFileVariable> v : variablesMap.get(platform).entrySet()) {
                BlockType t = v.getValue().location;
                if (t.getParent() != null && varName.equals(v.getValue().var) && t.getParent().name().equals(parent.name())) {
                    return t;
                }
            }
        }

        return PlayerBlockType.UNKNOWN;
    }

    @Override
    public String toString() {
        return "PlayerFileVariable{" +
                "location=" + location +
                ", var='" + var + '\'' +
                ", type=" + type +
                '}';
    }
}

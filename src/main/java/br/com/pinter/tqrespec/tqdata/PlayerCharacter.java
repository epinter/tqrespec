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

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.save.SaveLocation;
import br.com.pinter.tqrespec.save.player.Gender;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCharacter {
    private String name;
    private Path path;
    private SaveLocation location = SaveLocation.MAIN;
    private int level;
    private Gender gender;
    private String characterClass;
    private int difficulty;
    private int experience;
    private int gold;
    private int statLife;
    private int statMana;
    private int statStr;
    private int statInt;
    private int statDex;
    private int statAvailableAttrPoints;
    private int statAvailableSkillPoints;
    private int playTimeInSeconds;
    private String greatestMonsterKilledName;
    private int numberOfDeaths;
    private int numberOfKills;
    private int experienceFromKills;
    private int healthPotionsUsed;
    private int manaPotionsUsed;
    private int numHitsReceived;
    private int numHitsInflicted;
    private int greatestDamageInflicted;
    private int greatestMonsterKilledLevel;
    private int greatestMonsterKilledLifeAndMana;
    private int criticalHitsInflicted;
    private List<Mastery> masteries;
    private Map<Integer, List<MapTeleport>> defaultMapTeleports = new ConcurrentHashMap<>();

    public String getName() {
        return name;
    }

    public SaveLocation getLocation() {
        return location;
    }

    public void setLocation(SaveLocation location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getCharacterClass() {
        return characterClass;
    }

    public void setCharacterClass(String characterClass) {
        this.characterClass = characterClass;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getStatLife() {
        return statLife;
    }

    public void setStatLife(int statLife) {
        this.statLife = statLife;
    }

    public int getStatMana() {
        return statMana;
    }

    public void setStatMana(int statMana) {
        this.statMana = statMana;
    }

    public int getStatStr() {
        return statStr;
    }

    public void setStatStr(int statStr) {
        this.statStr = statStr;
    }

    public int getStatInt() {
        return statInt;
    }

    public void setStatInt(int statInt) {
        this.statInt = statInt;
    }

    public int getStatDex() {
        return statDex;
    }

    public void setStatDex(int statDex) {
        this.statDex = statDex;
    }

    public int getStatAvailableAttrPoints() {
        return statAvailableAttrPoints;
    }

    public void setStatAvailableAttrPoints(int statAvailableAttrPoints) {
        this.statAvailableAttrPoints = statAvailableAttrPoints;
    }

    public List<Mastery> getMasteries() {
        return masteries;
    }

    public void setMasteries(List<Mastery> masteries) {
        this.masteries = masteries;
    }

    public int getStatAvailableSkillPoints() {
        return statAvailableSkillPoints;
    }

    public void setStatAvailableSkillPoints(int statAvailableSkillPoints) {
        this.statAvailableSkillPoints = statAvailableSkillPoints;
    }

    public int getPlayTimeInSeconds() {
        return playTimeInSeconds;
    }

    public void setPlayTimeInSeconds(int playTimeInSeconds) {
        this.playTimeInSeconds = playTimeInSeconds;
    }

    public String getGreatestMonsterKilledName() {
        return greatestMonsterKilledName;
    }

    public void setGreatestMonsterKilledName(String greatestMonsterKilledName) {
        this.greatestMonsterKilledName = greatestMonsterKilledName;
    }

    public int getNumberOfDeaths() {
        return numberOfDeaths;
    }

    public void setNumberOfDeaths(int numberOfDeaths) {
        this.numberOfDeaths = numberOfDeaths;
    }

    public int getNumberOfKills() {
        return numberOfKills;
    }

    public void setNumberOfKills(int numberOfKills) {
        this.numberOfKills = numberOfKills;
    }

    public int getExperienceFromKills() {
        return experienceFromKills;
    }

    public void setExperienceFromKills(int experienceFromKills) {
        this.experienceFromKills = experienceFromKills;
    }

    public int getHealthPotionsUsed() {
        return healthPotionsUsed;
    }

    public void setHealthPotionsUsed(int healthPotionsUsed) {
        this.healthPotionsUsed = healthPotionsUsed;
    }

    public int getManaPotionsUsed() {
        return manaPotionsUsed;
    }

    public void setManaPotionsUsed(int manaPotionsUsed) {
        this.manaPotionsUsed = manaPotionsUsed;
    }

    public int getNumHitsReceived() {
        return numHitsReceived;
    }

    public void setNumHitsReceived(int numHitsReceived) {
        this.numHitsReceived = numHitsReceived;
    }

    public int getNumHitsInflicted() {
        return numHitsInflicted;
    }

    public void setNumHitsInflicted(int numHitsInflicted) {
        this.numHitsInflicted = numHitsInflicted;
    }

    public int getGreatestDamageInflicted() {
        return greatestDamageInflicted;
    }

    public void setGreatestDamageInflicted(int greatestDamageInflicted) {
        this.greatestDamageInflicted = greatestDamageInflicted;
    }

    public int getGreatestMonsterKilledLevel() {
        return greatestMonsterKilledLevel;
    }

    public void setGreatestMonsterKilledLevel(int greatestMonsterKilledLevel) {
        this.greatestMonsterKilledLevel = greatestMonsterKilledLevel;
    }

    public int getGreatestMonsterKilledLifeAndMana() {
        return greatestMonsterKilledLifeAndMana;
    }

    public void setGreatestMonsterKilledLifeAndMana(int greatestMonsterKilledLifeAndMana) {
        this.greatestMonsterKilledLifeAndMana = greatestMonsterKilledLifeAndMana;
    }

    public int getCriticalHitsInflicted() {
        return criticalHitsInflicted;
    }

    public void setCriticalHitsInflicted(int criticalHitsInflicted) {
        this.criticalHitsInflicted = criticalHitsInflicted;
    }

    public Map<Integer, List<MapTeleport>> getDefaultMapTeleports() {
        return defaultMapTeleports;
    }

    public void setDefaultMapTeleports(Map<Integer, List<MapTeleport>> defaultMapTeleports) {
        this.defaultMapTeleports = defaultMapTeleports;
    }

    public boolean isArchived() {
        return SaveLocation.ARCHIVEMAIN.equals(location) || SaveLocation.ARCHIVEUSER.equals(location);
    }

    public boolean isArchivable() {
        return SaveLocation.MAIN.equals(location) || SaveLocation.USER.equals(location);
    }

    public MapTeleport getLastMapTeleport() {
        List<MapTeleport> mapTeleports = getDefaultMapTeleports().get(getDifficulty());

        if (!mapTeleports.isEmpty()) {
            return mapTeleports.get(mapTeleports.size() - 1);
        }
        return null;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }
}

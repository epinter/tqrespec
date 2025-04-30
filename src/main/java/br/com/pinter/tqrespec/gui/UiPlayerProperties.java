/*
 * Copyright (C) 2025 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.save.player.Player;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UiPlayerProperties {
    private final Player player;
    private IntegerProperty str = new SimpleIntegerProperty();
    private IntegerProperty intl = new SimpleIntegerProperty();
    private IntegerProperty skillAvailable = new SimpleIntegerProperty();
    private IntegerProperty dex = new SimpleIntegerProperty();
    private IntegerProperty life = new SimpleIntegerProperty();
    private IntegerProperty mana = new SimpleIntegerProperty();
    private IntegerProperty attrAvailable = new SimpleIntegerProperty();
    private IntegerProperty charLevel = new SimpleIntegerProperty();
    private IntegerProperty xp = new SimpleIntegerProperty();
    private IntegerProperty gold = new SimpleIntegerProperty();
    private IntegerProperty difficulty = new SimpleIntegerProperty();
    private IntegerProperty firstMasteryLevel = new SimpleIntegerProperty();
    private IntegerProperty secondMasteryLevel = new SimpleIntegerProperty();
    private IntegerProperty electrum = new SimpleIntegerProperty();
    private IntegerProperty boostedCharacterForX4 = new SimpleIntegerProperty();
    private IntegerProperty numberOfSacks = new SimpleIntegerProperty();
    private IntegerProperty currentlyFocusedSackNumber = new SimpleIntegerProperty();
    private IntegerProperty currentlySelectedSackNumber = new SimpleIntegerProperty();
    private final Map<TeleportItem, String> teleportChanges = new ConcurrentHashMap<>();
    private IntegerProperty statplaytime = new SimpleIntegerProperty();
    private StringProperty statmonsterkilledname = new SimpleStringProperty();
    private IntegerProperty statmonsterkilledlevel = new SimpleIntegerProperty();
    private IntegerProperty statkills = new SimpleIntegerProperty();
    private IntegerProperty statdeath = new SimpleIntegerProperty();
    private IntegerProperty stathealthpotionused = new SimpleIntegerProperty();
    private IntegerProperty statmanapotionused = new SimpleIntegerProperty();
    private IntegerProperty statxpfromkills = new SimpleIntegerProperty();
    private IntegerProperty stathitsreceived = new SimpleIntegerProperty();
    private IntegerProperty stathitsinflicted = new SimpleIntegerProperty();
    private IntegerProperty statcriticalreceived = new SimpleIntegerProperty();
    private IntegerProperty statcriticalinflicted = new SimpleIntegerProperty();
    private boolean markResetStat = false;


    public UiPlayerProperties(Player player) {
        this.player = player;
        str.set(player.getStr());
        intl.set(player.getInt());
        dex.set(player.getDex());
        life.set(player.getLife());
        mana.set(player.getMana());
        attrAvailable.set(player.getModifierPoints());
        xp.set(player.getXp());
        charLevel.set(player.getLevel());
        gold.set(player.getMoney());
        difficulty.set(player.getDifficulty());
        skillAvailable.set(player.getAvailableSkillPoints());
        firstMasteryLevel.set(Math.max(player.getMasteryLevel(0), 0));
        secondMasteryLevel.set(Math.max(player.getMasteryLevel(1), 0));
        electrum.set(player.getAltMoney());
        boostedCharacterForX4.set(player.getBoostedCharacterForX4());
        numberOfSacks.set(player.getNumberOfSacks());
        currentlyFocusedSackNumber.set(player.getCurrentlyFocusedSackNumber());
        currentlySelectedSackNumber.set(player.getCurrentlySelectedSackNumber());
        reloadStats();
    }

    public void reset() {
        str = new SimpleIntegerProperty();
        intl = new SimpleIntegerProperty();
        skillAvailable = new SimpleIntegerProperty();
        dex = new SimpleIntegerProperty();
        life = new SimpleIntegerProperty();
        mana = new SimpleIntegerProperty();
        attrAvailable = new SimpleIntegerProperty();
        charLevel = new SimpleIntegerProperty();
        xp = new SimpleIntegerProperty();
        gold = new SimpleIntegerProperty();
        difficulty = new SimpleIntegerProperty();
        firstMasteryLevel = new SimpleIntegerProperty();
        secondMasteryLevel = new SimpleIntegerProperty();
        electrum = new SimpleIntegerProperty();
        boostedCharacterForX4 = new SimpleIntegerProperty();
        numberOfSacks = new SimpleIntegerProperty();
        currentlyFocusedSackNumber = new SimpleIntegerProperty();
        currentlySelectedSackNumber = new SimpleIntegerProperty();
        statplaytime = new SimpleIntegerProperty();
        statmonsterkilledname = new SimpleStringProperty();
        statmonsterkilledlevel = new SimpleIntegerProperty();
        statkills = new SimpleIntegerProperty();
        statdeath = new SimpleIntegerProperty();
        stathealthpotionused = new SimpleIntegerProperty();
        statmanapotionused = new SimpleIntegerProperty();
        statxpfromkills = new SimpleIntegerProperty();
        stathitsreceived = new SimpleIntegerProperty();
        stathitsinflicted = new SimpleIntegerProperty();
        statcriticalreceived = new SimpleIntegerProperty();
        statcriticalinflicted = new SimpleIntegerProperty();

    }

    public int getStr() {
        return str.get();
    }

    public IntegerProperty strProperty() {
        return str;
    }

    public void setStr(int str) {
        this.str.set(str);
    }

    public int getIntl() {
        return intl.get();
    }

    public IntegerProperty intlProperty() {
        return intl;
    }

    public void setIntl(int intl) {
        this.intl.set(intl);
    }

    public int getSkillAvailable() {
        return skillAvailable.get();
    }

    public IntegerProperty skillAvailableProperty() {
        return skillAvailable;
    }

    public void setSkillAvailable(int skillAvailable) {
        this.skillAvailable.set(skillAvailable);
    }

    public int getDex() {
        return dex.get();
    }

    public IntegerProperty dexProperty() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex.set(dex);
    }

    public int getLife() {
        return life.get();
    }

    public IntegerProperty lifeProperty() {
        return life;
    }

    public void setLife(int life) {
        this.life.set(life);
    }

    public int getMana() {
        return mana.get();
    }

    public IntegerProperty manaProperty() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana.set(mana);
    }

    public int getAttrAvailable() {
        return attrAvailable.get();
    }

    public IntegerProperty attrAvailableProperty() {
        return attrAvailable;
    }

    public void setAttrAvailable(int attrAvailable) {
        this.attrAvailable.set(attrAvailable);
    }

    public int getCharLevel() {
        return charLevel.get();
    }

    public IntegerProperty charLevelProperty() {
        return charLevel;
    }

    public void setCharLevel(int charLevel) {
        this.charLevel.set(charLevel);
    }

    public int getXp() {
        return xp.get();
    }

    public IntegerProperty xpProperty() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp.set(xp);
    }

    public int getGold() {
        return gold.get();
    }

    public IntegerProperty goldProperty() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold.set(gold);
    }

    public int getDifficulty() {
        return difficulty.get();
    }

    public IntegerProperty difficultyProperty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty.set(difficulty);
    }

    public int getFirstMasteryLevel() {
        return firstMasteryLevel.get();
    }

    public IntegerProperty firstMasteryLevelProperty() {
        return firstMasteryLevel;
    }

    public void setFirstMasteryLevel(int firstMasteryLevel) {
        this.firstMasteryLevel.set(firstMasteryLevel);
    }

    public int getSecondMasteryLevel() {
        return secondMasteryLevel.get();
    }

    public IntegerProperty secondMasteryLevelProperty() {
        return secondMasteryLevel;
    }

    public void setSecondMasteryLevel(int secondMasteryLevel) {
        this.secondMasteryLevel.set(secondMasteryLevel);
    }

    public int getElectrum() {
        return electrum.get();
    }

    public IntegerProperty electrumProperty() {
        return electrum;
    }

    public void setElectrum(int electrum) {
        this.electrum.set(electrum);
    }

    public int getBoostedCharacterForX4() {
        return boostedCharacterForX4.get();
    }

    public IntegerProperty boostedCharacterForX4Property() {
        return boostedCharacterForX4;
    }

    public void setBoostedCharacterForX4(int boostedCharacterForX4) {
        this.boostedCharacterForX4.set(boostedCharacterForX4);
    }

    public int getNumberOfSacks() {
        return numberOfSacks.get();
    }

    public IntegerProperty numberOfSacksProperty() {
        return numberOfSacks;
    }

    public void setNumberOfSacks(int numberOfSacks) {
        this.numberOfSacks.set(numberOfSacks);
    }

    public int getCurrentlyFocusedSackNumber() {
        return currentlyFocusedSackNumber.get();
    }

    public IntegerProperty currentlyFocusedSackNumberProperty() {
        return currentlyFocusedSackNumber;
    }

    public void setCurrentlyFocusedSackNumber(int currentlyFocusedSackNumber) {
        this.currentlyFocusedSackNumber.set(currentlyFocusedSackNumber);
    }

    public int getCurrentlySelectedSackNumber() {
        return currentlySelectedSackNumber.get();
    }

    public IntegerProperty currentlySelectedSackNumberProperty() {
        return currentlySelectedSackNumber;
    }

    public void setCurrentlySelectedSackNumber(int currentlySelectedSackNumber) {
        this.currentlySelectedSackNumber.set(currentlySelectedSackNumber);
    }

    public int getStatplaytime() {
        return statplaytime.get();
    }

    public IntegerProperty statplaytimeProperty() {
        return statplaytime;
    }

    public void setStatplaytime(int statplaytime) {
        this.statplaytime.set(statplaytime);
    }

    public String getStatmonsterkilledname() {
        return statmonsterkilledname.get();
    }

    public StringProperty statmonsterkillednameProperty() {
        return statmonsterkilledname;
    }

    public void setStatmonsterkilledname(String statmonsterkilledname) {
        this.statmonsterkilledname.set(statmonsterkilledname);
    }

    public int getStatmonsterkilledlevel() {
        return statmonsterkilledlevel.get();
    }

    public IntegerProperty statmonsterkilledlevelProperty() {
        return statmonsterkilledlevel;
    }

    public void setStatmonsterkilledlevel(int statmonsterkilledlevel) {
        this.statmonsterkilledlevel.set(statmonsterkilledlevel);
    }

    public int getStatkills() {
        return statkills.get();
    }

    public IntegerProperty statkillsProperty() {
        return statkills;
    }

    public void setStatkills(int statkills) {
        this.statkills.set(statkills);
    }

    public int getStatdeath() {
        return statdeath.get();
    }

    public IntegerProperty statdeathProperty() {
        return statdeath;
    }

    public void setStatdeath(int statdeath) {
        this.statdeath.set(statdeath);
    }

    public int getStathealthpotionused() {
        return stathealthpotionused.get();
    }

    public IntegerProperty stathealthpotionusedProperty() {
        return stathealthpotionused;
    }

    public void setStathealthpotionused(int stathealthpotionused) {
        this.stathealthpotionused.set(stathealthpotionused);
    }

    public int getStatmanapotionused() {
        return statmanapotionused.get();
    }

    public IntegerProperty statmanapotionusedProperty() {
        return statmanapotionused;
    }

    public void setStatmanapotionused(int statmanapotionused) {
        this.statmanapotionused.set(statmanapotionused);
    }

    public int getStatxpfromkills() {
        return statxpfromkills.get();
    }

    public IntegerProperty statxpfromkillsProperty() {
        return statxpfromkills;
    }

    public void setStatxpfromkills(int statxpfromkills) {
        this.statxpfromkills.set(statxpfromkills);
    }

    public int getStathitsreceived() {
        return stathitsreceived.get();
    }

    public IntegerProperty stathitsreceivedProperty() {
        return stathitsreceived;
    }

    public void setStathitsreceived(int stathitsreceived) {
        this.stathitsreceived.set(stathitsreceived);
    }

    public int getStathitsinflicted() {
        return stathitsinflicted.get();
    }

    public IntegerProperty stathitsinflictedProperty() {
        return stathitsinflicted;
    }

    public void setStathitsinflicted(int stathitsinflicted) {
        this.stathitsinflicted.set(stathitsinflicted);
    }

    public int getStatcriticalreceived() {
        return statcriticalreceived.get();
    }

    public IntegerProperty statcriticalreceivedProperty() {
        return statcriticalreceived;
    }

    public void setStatcriticalreceived(int statcriticalreceived) {
        this.statcriticalreceived.set(statcriticalreceived);
    }

    public int getStatcriticalinflicted() {
        return statcriticalinflicted.get();
    }

    public IntegerProperty statcriticalinflictedProperty() {
        return statcriticalinflicted;
    }

    public void setStatcriticalinflicted(int statcriticalinflicted) {
        this.statcriticalinflicted.set(statcriticalinflicted);
    }

    public void setMarkResetStat(boolean reset) {
        this.markResetStat = reset;
    }

    public boolean isMarkResetStat() {
        return markResetStat;
    }

    public void resetStats() {
        this.markResetStat = true;
        statplaytime.set(0);
        statmonsterkilledname.set(null);
        statmonsterkilledlevel.set(0);
        statkills.set(0);
        statdeath.set(0);
        stathealthpotionused.set(0);
        statmanapotionused.set(0);
        statxpfromkills.set(0);
        stathitsreceived.set(0);
        stathitsinflicted.set(0);
        statcriticalreceived.set(0);
        statcriticalinflicted.set(0);
    }

    public void reloadStats() {
        statplaytime.set(player.getStatPlayTimeInSeconds());
        String monster;
        if (player.getStatGreatestMonsterKilledName() == null) {
            monster = "";
        } else {
            monster = String.valueOf(player.getStatGreatestMonsterKilledName()).replaceAll("\\{.*}", "");
        }
        statmonsterkilledname.set(monster);
        statmonsterkilledlevel.set(player.getStatGreatestMonsterKilledLevel());
        statkills.set(player.getStatNumberOfKills());
        statdeath.set(player.getStatNumberOfDeaths());
        stathealthpotionused.set(player.getStatHealthPotionsUsed());
        statmanapotionused.set(player.getStatManaPotionsUsed());
        statxpfromkills.set(player.getStatExperienceFromKills());
        stathitsreceived.set(player.getStatNumHitsReceived());
        stathitsinflicted.set(player.getStatNumHitsInflicted());
        statcriticalreceived.set(player.getStatCriticalHitsReceived());
        statcriticalinflicted.set(player.getStatCriticalHitsInflicted());
    }

    public void reloadMasteriesLevels() {
        firstMasteryLevel.set(Math.max(player.getMasteryLevel(0), 0));
        secondMasteryLevel.set(Math.max(player.getMasteryLevel(1), 0));
    }

    public void reloadAvailSkillPoints() {
        skillAvailable.set(player.getAvailableSkillPoints());
    }

    public void putTeleportChange(TeleportItem tp, TeleportItem.Ops op) {
        if (teleportChanges.get(tp) != null && !teleportChanges.get(tp).equals(op.name())) {
            teleportChanges.remove(tp);
        } else {
            teleportChanges.putIfAbsent(tp, op.name());
        }
    }

    public Map<TeleportItem, String> getTeleportChanges() {
        return teleportChanges;
    }
}
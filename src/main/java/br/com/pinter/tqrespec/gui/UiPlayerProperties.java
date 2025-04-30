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

    public void reloadMasteriesLevels() {
        firstMasteryLevel.set(Math.max(player.getMasteryLevel(0), 0));
        secondMasteryLevel.set(Math.max(player.getMasteryLevel(1), 0));
    }

    public void reloadAvailSkillPoints() {
        skillAvailable.set(player.getAvailableSkillPoints());
    }
}
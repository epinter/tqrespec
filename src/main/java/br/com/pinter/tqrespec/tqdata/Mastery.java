/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqdatabase.models.Skill;

public class Mastery {
    private String displayName;
    private int level;
    private Skill mastery;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Skill getMastery() {
        return mastery;
    }

    public void setMastery(Skill mastery) {
        this.mastery = mastery;
    }

    @Override
    public String toString() {
        return "Mastery{" +
                "name='" + displayName + '\'' +
                ", level=" + level +
                ", mastery=" + mastery +
                '}';
    }
}

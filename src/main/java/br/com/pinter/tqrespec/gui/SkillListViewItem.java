/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

public class SkillListViewItem {
    private String skillName;
    private int skillPoints;

    SkillListViewItem(String skillName, int skillPoints) {
        this.skillName = skillName;
        this.skillPoints = skillPoints;
    }

    String getSkillName() {
        return skillName;
    }

    int getSkillPoints() {
        return skillPoints;
    }

}
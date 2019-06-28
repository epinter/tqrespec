/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

public class SkillListViewItem {
    private String skillName;
    private int skillPoints;
    private String skillNameText;

    SkillListViewItem(String skillName, int skillPoints, String skillNameText) {
        this.skillName = skillName;
        this.skillPoints = skillPoints;
        this.skillNameText = skillNameText;
    }

    String getSkillName() {
        return skillName;
    }

    int getSkillPoints() {
        return skillPoints;
    }

    public String getSkillNameText() {
        return skillNameText;
    }
}
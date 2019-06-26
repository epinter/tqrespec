/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqdatabase.Database;

public class PlayerSkill {
    private String skillName;
    private Integer skillEnabled;
    private Integer skillActive;
    private Integer skillSubLevel;
    private Integer skillTransition;
    private Integer skillLevel;
    private int blockStart;

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Integer getSkillEnabled() {
        return skillEnabled;
    }

    public void setSkillEnabled(Integer skillEnabled) {
        this.skillEnabled = skillEnabled;
    }

    public Integer getSkillActive() {
        return skillActive;
    }

    public void setSkillActive(Integer skillActive) {
        this.skillActive = skillActive;
    }

    public Integer getSkillSubLevel() {
        return skillSubLevel;
    }

    public void setSkillSubLevel(Integer skillSubLevel) {
        this.skillSubLevel = skillSubLevel;
    }

    public Integer getSkillTransition() {
        return skillTransition;
    }

    public void setSkillTransition(Integer skillTransition) {
        this.skillTransition = skillTransition;
    }

    public Integer getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(Integer skillLevel) {
        this.skillLevel = skillLevel;
    }

    public int getBlockStart() {
        return blockStart;
    }

    public void setBlockStart(int blockStart) {
        this.blockStart = blockStart;
    }
}

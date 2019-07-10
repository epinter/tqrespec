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

    @Override
    public String toString() {
        return "PlayerSkill{" +
                "skillName='" + skillName + '\'' +
                ", skillEnabled=" + skillEnabled +
                ", skillActive=" + skillActive +
                ", skillSubLevel=" + skillSubLevel +
                ", skillTransition=" + skillTransition +
                ", skillLevel=" + skillLevel +
                ", blockStart=" + blockStart +
                '}';
    }
}

/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.save.SkillBlock;

import java.util.ArrayList;
import java.util.List;

public class SkillUtils {
    public static List<Skill> getPlayerMasteries() {
        List<Skill> ret = new ArrayList<>();
        for (SkillBlock sb : PlayerData.getInstance().getSkillBlocks().values()) {
            Skill skill = Data.db().getSkillDAO().getSkill(sb.getSkillName(), false);
            if (skill != null && skill.isMastery()) {
                ret.add(skill);
            }
        }
        return ret;
    }

    public static List<Skill> getPlayerSkillsFromMastery(Skill mastery) {
        List<Skill> ret = new ArrayList<>();
        for (SkillBlock sb : PlayerData.getInstance().getSkillBlocks().values()) {
            Skill skill = Data.db().getSkillDAO().getSkill(sb.getSkillName(), false);
            if (skill != null && !skill.isMastery() && skill.getParentPath().equals(mastery.getRecordPath())) {
                ret.add(skill);
            }
        }
        return ret;
    }

    public static Skill getSkill(String recordPath, boolean resolve) {
        return Data.db().getSkillDAO().getSkill(recordPath, resolve);
    }
}

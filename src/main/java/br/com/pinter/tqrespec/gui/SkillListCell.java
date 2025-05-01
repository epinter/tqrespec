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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.tqdata.Txt;
import com.google.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class SkillListCell extends ListCell<SkillListViewItem> {
    private final HBox container;
    private final Label skillName;
    private final Label skillPoints;

    @Inject
    private Txt txt;

    SkillListCell() {
        super();
        skillName = new Label();
        skillPoints = new Label();
        HBox containerName = new HBox(skillName);
        HBox containerPoints = new HBox(skillPoints);
        container = new HBox(containerName, containerPoints);
        HBox.setHgrow(containerName, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(SkillListViewItem s, boolean empty) {
        super.updateItem(s, empty);
        if (empty) {
            setGraphic(null);
            skillName.setText(null);
            skillPoints.setText(null);
        } else if (s != null) {
            skillName.setText(s.getSkillNameText());
            skillPoints.setText(String.valueOf(s.getSkillPoints()));
            setGraphic(container);
        }
    }

}

/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.google.inject.Inject;

public class SkillListCell extends ListCell<SkillListViewItem> {
    private HBox container;
    private Label skillName;
    private Label skillPoints;

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

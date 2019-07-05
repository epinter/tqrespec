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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.save.PlayerSkill;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SkillsPaneController implements Initializable {
    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private PlayerData playerData;

    @FXML
    private ListView<SkillListViewItem> firstMasteryListView;

    @FXML
    private ListView<SkillListViewItem> secondMasteryListView;

    @FXML
    private Label firstMasteryLabel;

    @FXML
    private Label secondMasteryLabel;

    @FXML
    private Button reclaimMasteryFirstButton;

    @FXML
    private Button reclaimMasterySecondButton;

    @FXML
    private Button reclaimSkillsFirstButton;

    @FXML
    private Button reclaimSkillsSecondButton;

    @FXML
    private Label freeSkillPointsLabel;

    private SimpleStringProperty currentSkillPoints = null;

    private BooleanProperty saveDisabled = new SimpleBooleanProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public boolean isSaveDisabled() {
        return saveDisabled.get();
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled.set(saveDisabled);
    }

    public void loadCharEventHandler() {
        currentSkillPoints = new SimpleStringProperty();

        freeSkillPointsLabel.textProperty().bind(
                Bindings.createStringBinding(() -> Util.getUIMessage("skills.availableSkillPoints",
                        currentSkillPoints.getValue()),
                        currentSkillPoints
                )
        );

        updateMasteries();
    }

    private void resetSkilltabControls() {
        firstMasteryLabel.setText(null);
        firstMasteryListView.getItems().clear();
        secondMasteryLabel.setText(null);
        secondMasteryListView.getItems().clear();
        firstMasteryListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        secondMasteryListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        disableControls(true);
    }

    protected void disableControls(boolean disable) {
        firstMasteryListView.setDisable(disable);
        secondMasteryListView.setDisable(disable);
        if (!disable && firstMasteryListView.getItems().size() > 0) {
            reclaimSkillsFirstButton.setDisable(false);
        } else {
            reclaimSkillsFirstButton.setDisable(true);
        }
        if (playerData.isCharacterLoaded() && (!disable && getMasteryLevel(0) > 1) && firstMasteryListView.getItems().size() == 0) {
            reclaimMasteryFirstButton.setDisable(false);
        } else {
            reclaimMasteryFirstButton.setDisable(true);
        }

        if (!disable && secondMasteryListView.getItems().size() > 0) {
            reclaimSkillsSecondButton.setDisable(false);
        } else {
            reclaimSkillsSecondButton.setDisable(true);
        }
        if (playerData.isCharacterLoaded() && (!disable && getMasteryLevel(1) > 1) && secondMasteryListView.getItems().size() == 0) {
            reclaimMasterySecondButton.setDisable(false);
        } else {
            reclaimMasterySecondButton.setDisable(true);
        }
    }

    protected void updateMasteries() {
        if (!playerData.isCharacterLoaded()) {
            return;
        }
        resetSkilltabControls();

        fillMastery(0);
        fillMastery(1);
        disableControls(false);


        currentSkillPoints.setValue(String.valueOf(playerData.getAvailableSkillPoints()));
    }

    private int getMasteryLevel(int i) {
        List<Skill> masteries = playerData.getPlayerMasteries();

        if (!(masteries.size() == 1 && i > 0) && masteries.size() > 0) {
            PlayerSkill sb = playerData.getPlayerSkills().get(masteries.get(i).getRecordPath());
            return playerData.getMasteryLevel(sb);
        } else {
            return -1;
        }
    }

    private boolean fillMastery(int i) {
        boolean ret = false;
        Label masteryLabel;
        ListView<SkillListViewItem> masteryListView;

        switch (i) {
            case 0:
                masteryLabel = firstMasteryLabel;
                masteryListView = firstMasteryListView;
                break;
            case 1:
                masteryLabel = secondMasteryLabel;
                masteryListView = secondMasteryListView;
                break;
            default:
                return false;
        }

        Skill mastery = null;

        if (!(playerData.getPlayerMasteries().size() == 1 && i > 0) && playerData.getPlayerMasteries().size() > 0) {
            mastery = playerData.getPlayerMasteries().get(i);
            masteryLabel.setText(
                    String.format("%s (%d)",
                            txt.getString(mastery.getSkillDisplayName()),
                            playerData.getPlayerSkills().get(mastery.getRecordPath()).getSkillLevel()
                    )
            );
            ret = true;
        }

        ObservableList<SkillListViewItem> observableSkills = createObservableListFromMastery(mastery);

        Callback<ListView<SkillListViewItem>, ListCell<SkillListViewItem>> listViewCallback
                = skillListView -> new SkillListCell();

        masteryListView.setItems(observableSkills);
        masteryListView.setCellFactory(listViewCallback);
        return ret;
    }

    private ObservableList<SkillListViewItem> createObservableListFromMastery(Skill mastery) {
        ObservableList<SkillListViewItem> ret = FXCollections.observableArrayList();

        if (mastery == null) return ret;

        for (Skill s : playerData.getPlayerSkillsFromMastery(mastery)) {
            Skill s1 = s;
            if (s.isPointsToPet() || s.isPointsToBuff()) {
                s1 = db.skills().getSkill(s.getRecordPath(), true);
            }

            PlayerSkill sb = playerData.getPlayerSkills().get(s.getRecordPath());
            if (sb == null || s1.getRecordPath() == null) continue;
            ret.add(new SkillListViewItem(s1.getSkillDisplayName(),
                    sb.getSkillLevel(), txt.getString(s1.getSkillDisplayName())));
        }
        return ret;
    }

    private void reclaimPointsFromSkills(Skill mastery) throws Exception {
        for (Skill s : playerData.getPlayerSkillsFromMastery(mastery)) {
            PlayerSkill sb = playerData.getPlayerSkills().get(s.getRecordPath());
            if (sb == null || s.getRecordPath() == null) continue;
            playerData.reclaimSkillPoints(sb);
        }
    }

    public void reclaimMasteryFirst(Event event) throws Exception {
        disableControls(true);
        Skill mastery = playerData.getPlayerMasteries().get(0);
        PlayerSkill sb = playerData.getPlayerSkills().get(mastery.getRecordPath());

        List<Skill> list = playerData.getPlayerSkillsFromMastery(mastery);
        if (list.size() > 0) {
            Util.showInformation(Util.getUIMessage("skills.removeSkillsBefore"), null);
            return;
        }

        playerData.reclaimMasteryPoints(sb);
        updateMasteries();
    }

    public void reclaimMasterySecond(Event event) throws Exception {
        disableControls(true);
        Skill mastery = playerData.getPlayerMasteries().get(1);
        PlayerSkill sb = playerData.getPlayerSkills().get(mastery.getRecordPath());

        List<Skill> list = playerData.getPlayerSkillsFromMastery(mastery);
        if (list.size() > 0) {
            Util.showInformation(Util.getUIMessage("skills.removeSkillsBefore"), null);
            return;
        }

        playerData.reclaimMasteryPoints(sb);
        updateMasteries();
    }

    public void reclaimSkillsFirst(Event event) throws Exception {
        disableControls(true);
        Skill mastery = playerData.getPlayerMasteries().get(0);
        reclaimPointsFromSkills(mastery);
        updateMasteries();
    }

    public void reclaimSkillsSecond(Event event) throws Exception {
        disableControls(true);
        Skill mastery = playerData.getPlayerMasteries().get(1);
        reclaimPointsFromSkills(mastery);
        updateMasteries();
    }
}

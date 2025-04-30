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

import br.com.pinter.tqdatabase.models.Skill;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.save.player.PlayerSkill;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SkillsPaneController implements Initializable {
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private MainController mc;
    @FXML
    public GridPane skillsGridPane;
    @Inject
    private Db db;
    @Inject
    private Txt txt;
    @Inject
    private Player player;
    @FXML
    private ListView<SkillListViewItem> firstMasteryListView;
    @FXML
    private ListView<SkillListViewItem> secondMasteryListView;
    @FXML
    private Label firstMasteryLabel;
    @FXML
    private Label secondMasteryLabel;
    @FXML
    private Button reclaimSkillsFirstButton;
    @FXML
    private Button reclaimSkillsSecondButton;
    @FXML
    private Label freeSkillPointsLabel;
    @FXML
    private MenuItem reclaimMasteryFirstItem;
    @FXML
    private MenuItem reclaimMasterySecondItem;
    @FXML
    private MenuItem removeMasteryFirstItem;
    @FXML
    private MenuItem removeMasterySecondItem;
    @FXML
    private MenuButton firstMasteryButton;
    @FXML
    private MenuButton secondMasteryButton;
    @Inject
    private UIUtils uiUtils;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reclaimSkillsFirstButton.setGraphic(Icon.FA_RECYCLE.create());
        reclaimSkillsSecondButton.setGraphic(Icon.FA_RECYCLE.create());
        firstMasteryButton.setGraphic(Icon.FA_HAT_WIZARD.create());
        secondMasteryButton.setGraphic(Icon.FA_HAT_WIZARD.create());
        reclaimMasteryFirstItem.setGraphic(Icon.FA_ANGLE_DOUBLE_DOWN.create());
        removeMasteryFirstItem.setGraphic(Icon.FA_TIMES.create());
        reclaimMasterySecondItem.setGraphic(Icon.FA_ANGLE_DOUBLE_DOWN.create());
        removeMasterySecondItem.setGraphic(Icon.FA_TIMES.create());
    }

    private UiPlayerProperties playerProps() {
        return mc.getPlayerProperties();
    }

    public boolean isSaveDisabled() {
        return saveDisabled.get();
    }

    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled.set(saveDisabled);
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    public void loadCharEventHandler() {
        if (player.isMissingSkills()) {
            Toast.show((Stage) skillsGridPane.getParent().getScene().getWindow(),
                    ResourceHelper.getMessage("alert.missingSkill_header"),
                    ResourceHelper.getMessage("alert.missingSkill_content", player.getPlayerSavegameName(), Constants.LOGFILE),
                    Constants.UI.TOAST_WARNING_TIMEOUT);
        }

        StringBinding freeSkillPointsBinding = Bindings.createStringBinding(() -> ResourceHelper.getMessage("skills.availableSkillPoints",
                playerProps().getSkillAvailable()),
                playerProps().skillAvailableProperty()
        );
        freeSkillPointsLabel.textProperty().bind(freeSkillPointsBinding);

        StringBinding reclaimMasteryFirstBinding = Bindings.createStringBinding(() -> ResourceHelper.getMessage("skills.reclaimMasteryPoints",
                        playerProps().getFirstMasteryLevel()-1),
                playerProps().firstMasteryLevelProperty()
        );
        reclaimMasteryFirstItem.textProperty().bind(reclaimMasteryFirstBinding);

        StringBinding reclaimMasterySecondBinding = Bindings.createStringBinding(() -> ResourceHelper.getMessage("skills.reclaimMasteryPoints",
                        playerProps().getSecondMasteryLevel()-1),
                playerProps().secondMasteryLevelProperty()
        );
        reclaimMasterySecondItem.textProperty().bind(reclaimMasterySecondBinding);

        updateMasteries();
    }

    public void clearProperties() {
        if (freeSkillPointsLabel.textProperty().isBound()) {
            freeSkillPointsLabel.textProperty().unbind();
        }
    }

    public void reset() {
        clearProperties();
        resetSkilltabControls();
    }

    public void resetSkilltabControls() {
        if (!firstMasteryLabel.textProperty().isBound()) {
            firstMasteryLabel.setText(null);
        }
        if (!secondMasteryLabel.textProperty().isBound()) {
            secondMasteryLabel.setText(null);
        }
        if (!freeSkillPointsLabel.textProperty().isBound()) {
            freeSkillPointsLabel.setText(null);
        }
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

        reclaimSkillsFirstButton.setDisable(disable || firstMasteryListView.getItems().isEmpty());

        int firstMasteryLevel = playerProps().getFirstMasteryLevel();
        if (player.isCharacterLoaded() && (!disable && firstMasteryLevel > 0) && firstMasteryListView.getItems().isEmpty()) {
            if (firstMasteryLevel > 1) {
                reclaimMasteryFirstItem.setDisable(false);
            }
            removeMasteryFirstItem.setDisable(false);
        } else {
            reclaimMasteryFirstItem.setDisable(true);
            removeMasteryFirstItem.setDisable(true);
            firstMasteryButton.setDisable(true);
        }

        reclaimSkillsSecondButton.setDisable(disable || secondMasteryListView.getItems().isEmpty());

        int secondMasteryLevel = playerProps().getSecondMasteryLevel();
        if (player.isCharacterLoaded() && (!disable && secondMasteryLevel > 0) && secondMasteryListView.getItems().isEmpty()) {
            if (secondMasteryLevel > 1) {
                reclaimMasterySecondItem.setDisable(false);
            }
            removeMasterySecondItem.setDisable(false);
        } else {
            reclaimMasterySecondItem.setDisable(true);
            removeMasterySecondItem.setDisable(true);
            secondMasteryButton.setDisable(true);
        }

        firstMasteryButton.setDisable(reclaimMasteryFirstItem.isDisable() && removeMasteryFirstItem.isDisable());
        secondMasteryButton.setDisable(reclaimMasterySecondItem.isDisable() && removeMasterySecondItem.isDisable());
    }

    protected void updateMasteries() {
        if (!player.isCharacterLoaded()) {
            return;
        }

        resetSkilltabControls();

        fillMastery(0);
        fillMastery(1);
        playerProps().reloadAvailSkillPoints();
        playerProps().reloadMasteriesLevels();

        disableControls(false);
    }

    private int getMasteryLevel(int i) {
        List<Skill> masteries = player.getPlayerMasteries();

        if (!(masteries.size() == 1 && i > 0) && !masteries.isEmpty()) {
            PlayerSkill sb = player.getPlayerSkills().get(masteries.get(i).getRecordPath());
            return player.getMasteryLevel(sb);
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

        if (!(player.getPlayerMasteries().size() == 1 && i > 0) && !player.getPlayerMasteries().isEmpty()) {
            mastery = player.getPlayerMasteries().get(i);
            masteryLabel.setText(
                    String.format("%s (%d)",
                            txt.getString(mastery.getSkillDisplayName()),
                            player.getPlayerSkills().get(mastery.getRecordPath()).getSkillLevel()
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

        for (Skill s : player.getPlayerSkillsFromMastery(mastery)) {
            Skill s1 = s;
            if (s.isPointsToPet() || s.isPointsToBuff()) {
                s1 = db.skills().getSkill(s.getRecordPath(), true);
            }

            PlayerSkill sb = player.getPlayerSkills().get(s.getRecordPath());
            if (sb == null || s1.getRecordPath() == null) continue;
            ret.add(new SkillListViewItem(s1.getSkillDisplayName(),
                    sb.getSkillLevel(), txt.getString(s1.getSkillDisplayName())));
        }
        return ret;
    }

    private void reclaimPointsFromSkills(Skill mastery) {
        for (Skill s : player.getPlayerSkillsFromMastery(mastery)) {
            PlayerSkill sb = player.getPlayerSkills().get(s.getRecordPath());
            if (sb == null || s.getRecordPath() == null) continue;
            player.reclaimSkillPoints(sb);
        }
    }

    @FXML
    public void reclaimMasteryFirst(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().getFirst();
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());

        if (!isMasteryEmpty(mastery)) {
            return;
        }

        player.reclaimMasteryPoints(sb);
        updateMasteries();
    }

    @FXML
    public void reclaimMasterySecond(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().get(1);
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());

        if (!isMasteryEmpty(mastery)) {
            return;
        }

        player.reclaimMasteryPoints(sb);
        updateMasteries();
    }

    @FXML
    public void reclaimSkillsFirst(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().getFirst();
        reclaimPointsFromSkills(mastery);
        updateMasteries();
    }

    @FXML
    public void reclaimSkillsSecond(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().get(1);
        reclaimPointsFromSkills(mastery);
        updateMasteries();
    }

    @FXML
    public void removeMasteryFirst(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().getFirst();
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());

        if (!isMasteryEmpty(mastery)) {
            return;
        }

        player.removeMastery(sb);
        updateMasteries();
    }

    @FXML
    public void removeMasterySecond(Event event) {
        disableControls(true);
        Skill mastery = player.getPlayerMasteries().get(1);
        PlayerSkill sb = player.getPlayerSkills().get(mastery.getRecordPath());

        if (!isMasteryEmpty(mastery)) {
            return;
        }

        player.removeMastery(sb);
        updateMasteries();
    }

    public boolean isMasteryEmpty(Skill mastery) {
        List<Skill> list = player.getPlayerSkillsFromMastery(mastery);
        if (!list.isEmpty()) {
            uiUtils.showInformation(ResourceHelper.getMessage("skills.removeSkillsBefore"), null);
            return false;
        }
        return true;
    }

    public void setMainController(MainController mainController) {
        this.mc = mainController;
    }
}

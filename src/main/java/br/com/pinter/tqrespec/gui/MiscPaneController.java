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

import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.core.MyEventHandler;
import br.com.pinter.tqrespec.core.MyTask;
import br.com.pinter.tqrespec.core.WorkerThread;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.exporter.Exporter;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.save.player.PlayerWriter;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.DefaultAct;
import br.com.pinter.tqrespec.tqdata.DefaultMapTeleport;
import br.com.pinter.tqrespec.tqdata.MapTeleport;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

public class MiscPaneController implements Initializable {
    private static final System.Logger logger = Log.getLogger(MiscPaneController.class.getName());
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private final BooleanProperty charNameBlankBlocked = new SimpleBooleanProperty(false);
    private MainController mainController;

    @Inject
    private Player player;
    @Inject
    private PlayerWriter playerWriter;
    @Inject
    private UIUtils uiUtils;
    @Inject
    private Db db;
    @Inject
    private Txt txt;

    @FXML
    private Button copyButton;
    @FXML
    private TextField copyCharInput;
    @FXML
    private ComboBox<CopyTarget> copyTargetCombo;
    @FXML
    private Button exportJsonButton;
    @FXML
    private CheckBox fullBackupCheckbox;
    @FXML
    private CheckBox unlockCheckbox;
    @FXML
    private CheckBox freeLvlCheckbox;
    @FXML
    private MenuButton teleportsMenuButton;
    @FXML
    private Button unlockBagsButton;
    @FXML
    private Button resetPlayerStatsButton;
    @FXML
    private Label statplaytimeText;
    @FXML
    private Label statmonsterkillednameText;
    @FXML
    private Label statmonsterkilledlevelText;
    @FXML
    private Label statkillsText;
    @FXML
    private Label statdeathText;
    @FXML
    private Label statxpfromkillsText;
    @FXML
    private Label stathealthpotionusedText;
    @FXML
    private Label statmanapotionusedText;
    @FXML
    private Label stathitsreceivedText;
    @FXML
    private Label stathitsinflictedText;
    @FXML
    private Label statcriticalreceivedText;
    @FXML
    private Label statcriticalinflictedText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        copyButton.setGraphic(Icon.FA_COPY.create());
        copyTargetCombo.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipCopyTarget")));
        copyTargetCombo.setItems(FXCollections.observableList(Arrays.asList(CopyTarget.values())));
        copyTargetCombo.getSelectionModel().select(CopyTarget.WINDOWS);
        copyTargetCombo.setCellFactory(f -> new ListCell<>() {
            @Override
            protected void updateItem(CopyTarget copyTarget, boolean empty) {
                super.updateItem(copyTarget, empty);
                setText(empty ? "" : ResourceHelper.getMessage("main.copyTarget." + copyTarget));
                setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipCopyTarget." + copyTarget)));
            }
        });
        copyTargetCombo.setButtonCell(new ListCell<>() {
            @Override
            public void updateIndex(int i) {
                super.updateIndex(i);
                CopyTarget platform = getListView().getItems().get(i);
                setText(ResourceHelper.getMessage("main.copyTarget." + platform));
            }
        });
        exportJsonButton.setGraphic(Icon.FA_FILE_EXPORT.create());
        exportJsonButton.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("misc.tooltipExportJson")));

        fullBackupCheckbox.setSelected(Settings.getAlwaysFullBackup());
        fullBackupCheckbox.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("misc.tooltip.options.fullbackup")));

        unlockCheckbox.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("misc.tooltip.enableedit")));
        freeLvlCheckbox.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("misc.tooltip.freeedit")));
        teleportsMenuButton.getItems().clear();
        unlockBagsButton.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("misc.tooltip.unlockbags")));
    }

    public BooleanProperty unlockCheckboxSelectedProperty() {
        return unlockCheckbox.selectedProperty();
    }

    public BooleanProperty freeLvlCheckboxSelectedProperty() {
        return freeLvlCheckbox.selectedProperty();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled.set(saveDisabled);
    }

    public void disableControls(boolean disable) {
        if (!disable) {
            copyButton.setDisable(copyCharInput.getText().isBlank() && charNameBlankBlocked.get());
        } else {
            copyButton.setDisable(true);
        }
        if (disable) {
            unlockBagsButton.setDisable(true);
        }
        copyCharInput.setDisable(disable);
        copyTargetCombo.setDisable(disable);
        exportJsonButton.setDisable(disable);
        unlockCheckbox.setDisable(disable);
        freeLvlCheckbox.setDisable(disable);
        teleportsMenuButton.setDisable(disable);
        resetPlayerStatsButton.setDisable(disable);
    }

    public void loadCharEventHandler() {
        checkCopyTarget();
        if (charNameBlankBlocked.get()) {
            copyCharInput.setDisable(true);
        }
        unlockCheckbox.setDisable(false);
        freeLvlCheckbox.setDisable(false);

        List<DifficultyItem> difficultyItems = new ArrayList<>();
        //load difficulty list from game, the tag id starts with 1, while the difficulty id in save game starts with 0
        for (int i = 0; i <= 2; i++) {
            String name = ResourceHelper.cleanTagString(txt.getString(String.format("%s%02d", Constants.UI.PREFIXTAG_DIFFICULTYLABEL, i + 1)));
            if (name != null && !name.isBlank()) {
                difficultyItems.add(new DifficultyItem(i, name));
            }
        }
        if (difficultyItems.isEmpty()) {
            for (int i = 0; i <= 2; i++) {
                difficultyItems.add(new DifficultyItem(
                        i, ResourceHelper.getMessage(String.format("difficulty.%d", i))
                ));
            }
        }

        if (mainController.isCharacterSelected()) {
            List<MenuItem> menus = new ArrayList<>();
            for (DifficultyItem difficulty : difficultyItems) {
                if (difficulty.getId() > playerProps().getDifficulty()) {
                    continue;
                }
                List<MapTeleport> playerTeleports = player.getDefaultMapTeleports(difficulty.getId());
                Menu md = new Menu(difficulty.getName());
                for (DefaultAct act : DefaultAct.values()) {
                    String actName;
                    if (txt.isTagStringValid(act.getTag())) {
                        actName = txt.getString(act.getTag());
                    } else {
                        actName = txt.getStringEn(act.getTag());
                    }
                    Menu ma = new Menu(actName);
                    ma.getStyleClass().add("tq-submenuteleport");
                    md.getItems().add(ma);
                    List<CustomMenuItem> items = new ArrayList<>();
                    for (MapTeleport tp : getAllTeleports().stream().filter(t -> t.getAct() == act.getValue()).toList()) {
                        CheckBox cb = new CheckBox(tp.getName());
                        cb.setUserData(new TeleportItem(tp, difficulty.getId()));
                        cb.getStyleClass().add("tq-menucheckbox");
                        cb.setOnAction(this::clickCheckboxTeleport);
                        cb.setPrefWidth(250);
                        CustomMenuItem i = new CustomMenuItem(cb);
                        i.setHideOnClick(false);
                        cb.setSelected(playerTeleports.contains(tp));
                        items.add(i);
                    }
                    ma.getItems().setAll(items);
                }
                menus.add(md);
            }
            teleportsMenuButton.getItems().setAll(menus);

            StringBinding playTimeBinding = Bindings.createStringBinding(() -> {
                        int h = playerProps().getStatplaytime() / 3600;
                        int m = (playerProps().getStatplaytime() % 3600) / 60;
                        int s = (playerProps().getStatplaytime() % 60) % 60;
                        return String.format("%2dh %2dm %2ds", h, m, s);
                    },
                    playerProps().statplaytimeProperty()
            );
            statplaytimeText.textProperty().bind(playTimeBinding);

            statmonsterkillednameText.textProperty().bind(playerProps().statmonsterkillednameProperty());
            statmonsterkilledlevelText.textProperty().bind(playerProps().statmonsterkilledlevelProperty().asString());
            statkillsText.textProperty().bind(playerProps().statkillsProperty().asString());
            statdeathText.textProperty().bind(playerProps().statdeathProperty().asString());
            stathealthpotionusedText.textProperty().bind(playerProps().stathealthpotionusedProperty().asString());
            statmanapotionusedText.textProperty().bind(playerProps().statmanapotionusedProperty().asString());
            statxpfromkillsText.textProperty().bind(playerProps().statxpfromkillsProperty().asString());
            stathitsreceivedText.textProperty().bind(playerProps().stathitsreceivedProperty().asString());
            stathitsinflictedText.textProperty().bind(playerProps().stathitsinflictedProperty().asString());
            statcriticalreceivedText.textProperty().bind(playerProps().statcriticalreceivedProperty().asString());
            statcriticalinflictedText.textProperty().bind(playerProps().statcriticalinflictedProperty().asString());
        }
        unlockBagsButton.setDisable(playerProps().getNumberOfSacks() == 4);
    }

    private UiPlayerProperties playerProps() {
        return mainController.playerProps();
    }

    private void clickCheckboxTeleport(ActionEvent evt) {
        CheckBox cb = (CheckBox) evt.getSource();
        TeleportItem tp = (TeleportItem) cb.getUserData();
        if (cb.isSelected()) {
            if (tp.getDifficulty() < 2 && player.getBoostedCharacterForX4() == 1) {
                logger.log(WARNING, "Removing 'Legendary Hero' flag for Character ''{0}''", player.getCharacterName());
                playerProps().setBoostedCharacterForX4(0);
            }
            playerProps().putTeleportChange(tp, TeleportItem.Ops.INSERT);
            logger.log(DEBUG, "Checkbox {0} is selected, difficulty:{1}; teleport:{2}", cb.getText(), tp.getDifficulty(), tp.getTeleport().getName());
        } else {
            playerProps().putTeleportChange(tp, TeleportItem.Ops.REMOVE);
            logger.log(DEBUG, "Checkbox {0} REMOVED, difficulty:{1}; teleport:{2}", cb.getText(), tp.getDifficulty(), tp.getTeleport().getName());
        }
    }

    private void filterCopyCharInput() {
        String str = copyCharInput.getText();
        copyCharInput.setText(StringUtils.stripAccents(str)
                .replaceAll("[\\\\/:*?\"<>|;]", "")
                .replaceAll("^(.{0,14}).*", "$1"));
    }

    @FXML
    public void copyCharInputChanged() {
        if (!player.isCharacterLoaded()) {
            return;
        }

        int caret = copyCharInput.getCaretPosition();

        filterCopyCharInput();

        copyCharInput.positionCaret(caret);
        if (copyCharInput.getText().isBlank()) {
            copyButton.setDisable(charNameBlankBlocked.get());
        } else {
            copyButton.setDisable(false);
        }
    }

    private void setAllControlsDisable(boolean disable) {
        mainController.setAllControlsDisable(disable);
    }

    @FXML
    public void copyChar() {
        filterCopyCharInput();

        if (mainController.gameRunningAlert()) {
            return;
        }
        String targetPlayerName;
        if (StringUtils.isBlank(copyCharInput.getText())) {
            if (charNameBlankBlocked.get()) {
                throw new IllegalStateException("character name can't be empty when target is same as origin");
            }
            targetPlayerName = player.getCharacterName();
        } else {
            targetPlayerName = copyCharInput.getText();
        }

        mainController.commitChanges();

        CopyTarget selectedTarget = copyTargetCombo.getSelectionModel().getSelectedItem();
        Platform current = player.getSaveData().getPlatform();
        Platform conversionTarget = null;
        File selectedFile = null;

        if (!selectedTarget.equals(CopyTarget.BACKUP) && !selectedTarget.getPlatform().equals(current)) {
            //needs conversion
            conversionTarget = selectedTarget.getPlatform();
        }

        if (selectedTarget.equals(CopyTarget.MOBILE) || selectedTarget.equals(CopyTarget.BACKUP)) {
            FileChooser zipChooser = new FileChooser();
            zipChooser.setTitle(ResourceHelper.getMessage("misc.copyFileChooserTitle"));
            zipChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP", "*.zip"));
            zipChooser.setInitialFileName(String.format("%s-%s-%s.zip",
                    targetPlayerName,
                    new SimpleDateFormat("yyyyMMdd").format(new Date()),
                    selectedTarget.name()
            ));
            selectedFile = zipChooser.showSaveDialog(copyCharInput.getScene().getWindow());

            if (selectedFile == null || selectedFile.exists()) {
                uiUtils.showError("Error copying character", "Aborted");
                mainController.reset();
                return;
            }
        }

        Path zipPath = null;
        if (selectedFile != null) {
            zipPath = selectedFile.toPath();
        }

        setAllControlsDisable(true);

        final Path finalZipPath = zipPath;
        final Platform finalConversionTarget = conversionTarget;
        MyTask<Integer> copyCharTask = new MyTask<>() {
            @Override
            protected Integer call() {
                try {
                    //both conversionTarget and zipPath are never null at the same time
                    if (finalConversionTarget != null) {
                        playerWriter.copyCurrentSave(targetPlayerName, finalConversionTarget, finalZipPath);
                    } else if (finalZipPath != null) {
                        playerWriter.copyCurrentSave(targetPlayerName, Platform.UNDEFINED, finalZipPath);
                    } else {
                        playerWriter.copyCurrentSave(targetPlayerName);
                    }
                    return 2;
                } catch (FileAlreadyExistsException e) {
                    return 3;
                } catch (IOException e) {
                    return 0;
                }
            }
        };

        //noinspection Convert2Lambda
        copyCharTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if (copyCharTask.getValue() == 2) {
                    player.reset();
                    reset();
                    setAllControlsDisable(false);
                    mainController.addCharactersToCombo();
                    mainController.reset();
                } else if (copyCharTask.getValue() == 3) {
                    uiUtils.showError("Target Directory already exists!",
                            String.format("The specified target directory already exists. Aborting the copy to character '%s'",
                                    targetPlayerName));
                    mainController.reset();
                } else {
                    uiUtils.showError(ResourceHelper.getMessage("alert.errorcopying_header"),
                            ResourceHelper.getMessage("alert.errorcopying_content", targetPlayerName));
                    mainController.reset();
                }
            }
        });
        mainController.setCursorWaitOnTask(copyCharTask);
        new WorkerThread(copyCharTask).start();
    }

    public void exportJson() {
        mainController.commitChanges();

        FileChooser jsonChooser = new FileChooser();
        jsonChooser.setTitle(ResourceHelper.getMessage("misc.exportJsonFileChooserTitle"));
        jsonChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        jsonChooser.setInitialFileName(String.format("%s-%s.json",
                player.getCharacterName(),
                new SimpleDateFormat("yyyyMMdd").format(new Date())
        ));
        File selectedFile = jsonChooser.showSaveDialog(exportJsonButton.getScene().getWindow());
        if (selectedFile == null) {
            uiUtils.showError("Error exporting json", "Aborted");
            mainController.reset();
            return;
        }

        MyTask<Integer> exportJsonTask = new MyTask<>() {
            @Override
            protected Integer call() {
                try {
                    new Exporter(selectedFile, player.getSaveData().getDataMap()).writeJson();
                    return 1;
                } catch (IOException e) {
                    return 0;
                }
            }
        };

        mainController.setCursorWaitOnTask(exportJsonTask);
        new WorkerThread(exportJsonTask).start();
    }

    public void copyTargetSelected() {
        if (!player.isCharacterLoaded()) {
            return;
        }
        checkCopyTarget();
    }

    private void checkCopyTarget() {
        CopyTarget copyTarget = copyTargetCombo.getSelectionModel().getSelectedItem();
        charNameBlankBlocked.set(copyTarget.getPlatform() != null && copyTarget.getPlatform().equals(player.getSaveData().getPlatform()));
        copyButton.setDisable(charNameBlankBlocked.get());
    }

    public void reset() {
        exportJsonButton.setDisable(true);
        copyCharInput.clear();
        copyCharInput.setDisable(true);
        charNameBlankBlocked.set(false);
        if (copyTargetCombo != null && copyTargetCombo.getSelectionModel() != null) {
            copyTargetCombo.getSelectionModel().select(CopyTarget.WINDOWS);
        }
        statplaytimeText.textProperty().unbind();
        statplaytimeText.setText("");
        statmonsterkillednameText.textProperty().unbind();
        statmonsterkillednameText.setText("");
        statmonsterkilledlevelText.textProperty().unbind();
        statmonsterkilledlevelText.setText("");
        statkillsText.textProperty().unbind();
        statkillsText.setText("");
        statdeathText.textProperty().unbind();
        statdeathText.setText("");
        stathealthpotionusedText.textProperty().unbind();
        stathealthpotionusedText.setText("");
        statmanapotionusedText.textProperty().unbind();
        statmanapotionusedText.setText("");
        statxpfromkillsText.textProperty().unbind();
        statxpfromkillsText.setText("");
        stathitsreceivedText.textProperty().unbind();
        stathitsreceivedText.setText("");
        stathitsinflictedText.textProperty().unbind();
        stathitsinflictedText.setText("");
        statcriticalreceivedText.textProperty().unbind();
        statcriticalreceivedText.setText("");
        statcriticalinflictedText.textProperty().unbind();
        statcriticalinflictedText.setText("");
    }

    private List<MapTeleport> getAllTeleports() {
        List<MapTeleport> ret = new ArrayList<>();
        for (MapTeleport m : DefaultMapTeleport.getAll()) {
            String name = db.teleports().getTeleport(m.getRecordId()).getDescription();
            if (txt.isTagStringValid(m.getTag())) {
                name = txt.getString(m.getTag());
            } else if (txt.getStringEn(m.getTag()) != null) {
                name = txt.getStringEn(m.getTag());
            } else if (txt.getString(name) != null) {
                name = txt.getString(name);
            }
            m.setName(name);
            ret.add(m);
        }

        return ret;
    }

    @FXML
    public void fullbackupToggled(ActionEvent event) {
        Settings.setAlwaysFullBackup(fullBackupCheckbox.isSelected());
    }

    @FXML
    public void resetPlayerStats(ActionEvent event) {
        playerProps().resetStats();
    }

    @FXML
    public void unlockBags(ActionEvent event) {
        if (playerProps().getNumberOfSacks() < 4) {
            playerProps().setNumberOfSacks(4);
            playerProps().setCurrentlyFocusedSackNumber(1);
            playerProps().setCurrentlySelectedSackNumber(1);
        }

        unlockBagsButton.setDisable(true);
    }
}

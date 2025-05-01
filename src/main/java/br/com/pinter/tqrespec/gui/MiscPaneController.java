/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.core.MyEventHandler;
import br.com.pinter.tqrespec.core.MyTask;
import br.com.pinter.tqrespec.core.WorkerThread;
import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.exporter.Exporter;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.save.player.PlayerWriter;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class MiscPaneController implements Initializable {
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private final BooleanProperty charNameBlankBlocked = new SimpleBooleanProperty(false);
    private MainController mainController;
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
    @Inject
    private Player player;
    @Inject
    private PlayerWriter playerWriter;
    @Inject
    private UIUtils uiUtils;

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
        copyCharInput.setDisable(disable);
        copyTargetCombo.setDisable(disable);
        exportJsonButton.setDisable(disable);
    }

    public void loadCharEventHandler() {
        checkCopyTarget();
        if (charNameBlankBlocked.get()) {
            copyCharInput.setDisable(true);
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
    }

    @FXML
    public void fullbackupToggled(ActionEvent event) {
        Settings.setAlwaysFullBackup(fullBackupCheckbox.isSelected());
    }
}

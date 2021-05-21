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

import br.com.pinter.tqrespec.core.*;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.save.player.PlayerWriter;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.PlayerCharacterFile;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("unused")
public class MainController implements Initializable {
    public static final BooleanProperty mainFormInitialized = new SimpleBooleanProperty();
    private static final System.Logger logger = Log.getLogger(MainController.class.getName());
    public final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    @FXML
    public GridPane pointsPane;
    @FXML
    public AttributesPaneController pointsPaneController;
    @FXML
    public GridPane skillsPane;
    @FXML
    public SkillsPaneController skillsPaneController;
    @FXML
    public GridPane miscPane;
    @FXML
    public MiscPaneController miscPaneController;
    @FXML
    public Button resetButton;
    @FXML
    public Button charactersButton;
    @FXML
    public Tab skillsTab;
    @FXML
    public Tab attributesTab;
    @FXML
    public Tab miscTab;
    @Inject
    private FXMLLoader fxmlLoaderAbout;
    @Inject
    private FXMLLoader fxmlLoaderCharacter;
    @Inject
    private Player player;
    @Inject
    private PlayerWriter playerWriter;
    @Inject
    private HostServices hostServices;
    @Inject
    private GameInfo gameInfo;
    @FXML
    private VBox rootelement;
    @FXML
    private ComboBox<PlayerCharacterFile> characterCombo;
    @FXML
    private Button saveButton;
    @FXML
    private Label mainFormTitle;
    @FXML
    private Hyperlink versionCheck;
    @FXML
    private TabPane tabPane;
    @Inject
    private Db db;
    private double dragX;
    private double dragY;
    private boolean isMoving = false;
    @Inject
    private Txt txt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.log(System.Logger.Level.DEBUG, "isLocaleLanguageEmpty: " + resources.getLocale().getLanguage().isEmpty());
        if (!State.get().getLocale().equals(Locale.ENGLISH) && resources.getLocale().getLanguage().isEmpty()) {
            Util.tryTagText(txt, attributesTab, Constants.UI.TAG_ATTRIBUTESTAB, false, true);
            Util.tryTagText(txt, skillsTab, Constants.UI.TAG_SKILLSTAB, false, true);
        }
        mainFormTitle.setText(String.format("%s v%s", Util.getBuildTitle(), Util.getBuildVersion()));
        mainFormInitialized.addListener(((observable, oldValue, newValue) -> new WorkerThread(new MyTask<>() {
            @Override
            protected Void call() {
                windowShownHandler();
                return null;
            }
        }).start()));

        //initialize properties and bind them to respective properties in the tab controllers
        saveDisabled.setValue(saveButton.isDisable());
        miscPaneController.setMainController(this);
        pointsPaneController.setSaveDisabled(saveButton.isDisabled());
        skillsPaneController.setSaveDisabled(saveButton.isDisabled());
        miscPaneController.setSaveDisabled(saveButton.isDisabled());
        saveButton.disableProperty().bindBidirectional(saveDisabled);
        saveDisabled.bindBidirectional(pointsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(skillsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(miscPaneController.saveDisabledProperty());

        //set icons
        resetButton.setGraphic(Icon.FA_UNDO.create(1.4));
        resetButton.setTooltip(Util.simpleTooltip(Util.getUIMessage("main.resetButtonTooltip")));
        saveButton.setGraphic(Icon.FA_SAVE.create());
        charactersButton.setGraphic(Icon.FA_USERS.create(1.4));
        charactersButton.setTooltip(Util.simpleTooltip(Util.getUIMessage("main.charactersButtonTooltip")));

        State.get().gameRunningProperty().addListener((value, oldV, newV) -> {
            if (BooleanUtils.isTrue(newV)) {
                Platform.runLater(() -> {
                    reset();
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            Util.getUIMessage("alert.errorgamerunning_header"),
                            Util.getUIMessage("alert.errorgamerunning_content"),
                            8000);
                });
            }
        });

        new CheckVersionService(Util.getBuildVersion(), Constants.VERSION_CHECK_URL, versionCheck).start();
    }

    public void addCharactersToCombo() {
        try {
            characterCombo.getSelectionModel().clearSelection();
            characterCombo.getItems().setAll(gameInfo.getPlayerCharacterList());
            characterCombo.getItems().sort(Comparator.comparing(PlayerCharacterFile::getPlayerName));
        } catch (ClassCastException | UnsupportedOperationException | IllegalArgumentException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            throw new UnhandledRuntimeException(Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    public void setCharacterCombo(PlayerCharacterFile character) {
        if (characterCombo.getItems().contains(character)) {
            characterCombo.setValue(character);
        }
    }

    private void windowShownHandler() {
        assert characterCombo == null : "fx:id=\"characterCombo\" not found in FXML.";
        addCharactersToCombo();
        tabPane.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            if (State.get().getLastCursorWaitTask() != null && State.get().getLastCursorWaitTask().isRunning()) {
                tabPane.setCursor(Cursor.WAIT);
            }
        });
    }

    @FXML
    public void close(MouseEvent evt) {
        Util.closeApplication();
    }

    @FXML
    public void openAboutWindow(MouseEvent evt) throws IOException {
        if (State.get().getSaveInProgress()) {
            return;
        }

        Parent root;
        if (fxmlLoaderAbout.getRoot() == null) {
            fxmlLoaderAbout.setLocation(getClass().getResource(Constants.UI.ABOUT_FXML));
            fxmlLoaderAbout.setResources(ResourceBundle.getBundle("i18n.UI", State.get().getLocale()));
            fxmlLoaderAbout.load();
        } else {
            root = fxmlLoaderAbout.getRoot();
            ((Stage) root.getScene().getWindow()).show();
        }
    }

    @FXML
    public void openCharactersWindow(ActionEvent evt) throws IOException {
        if (State.get().getSaveInProgress()) {
            return;
        }

        reset();
        Parent root;
        if (fxmlLoaderCharacter.getRoot() == null) {
            fxmlLoaderCharacter.setLocation(getClass().getResource("/fxml/characters.fxml"));
            fxmlLoaderCharacter.setResources(ResourceBundle.getBundle("i18n.UI", State.get().getLocale()));
            fxmlLoaderCharacter.load();
        } else {
            root = fxmlLoaderCharacter.getRoot();
            ((Stage) root.getScene().getWindow()).show();
        }
    }

    @FXML
    public void resetButtonClicked(ActionEvent event) {
        if (!State.get().getSaveInProgress()) {
            reset();
        }
    }

    public void reset() {
        pointsPaneController.clearProperties();
        skillsPaneController.resetSkilltabControls();
        miscPaneController.reset();
        player.reset();
        characterCombo.setValue(null);
        characterCombo.getItems().clear();
        addCharactersToCombo();
        setAllControlsDisable(true);
        characterCombo.setDisable(false);
        Toast.cancel();
        restoreDefaultCursor();
        tabPane.getSelectionModel().select(attributesTab);
    }

    public void setCursorWaitOnTask(MyTask<Integer> task) {
        tabPane.setCursor(Cursor.WAIT);
        State.get().setLastCursorWaitTask(task);
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> restoreDefaultCursor());
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, e -> restoreDefaultCursor());
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, e -> restoreDefaultCursor());
    }

    private void restoreDefaultCursor() {
        tabPane.setCursor(Cursor.DEFAULT);
    }

    public boolean gameRunningAlert() {
        if (BooleanUtils.isTrue(State.get().getGameRunning())) {
            Util.showError(Util.getUIMessage("alert.errorgamerunning_header"),
                    Util.getUIMessage("alert.errorgamerunning_content"));
            return true;
        }
        return false;
    }

    @FXML
    public void saveChar(ActionEvent evt) {
        if (gameRunningAlert()) {
            return;
        }

        MyTask<Integer> backupSaveGameTask = new MyTask<>() {
            @Override
            protected Integer call() {
                setAllControlsDisable(true);
                try {
                    return playerWriter.backupCurrent() ? 2 : 0;
                } catch (IOException e) {
                    throw new UnhandledRuntimeException("Error starting backup", e);
                }
            }
        };
        MyTask<Integer> saveGameTask = new MyTask<>() {
            @Override
            protected Integer call() {
                pointsPaneController.saveCharHandler();
                return playerWriter.save() ? 2 : 0;
            }
        };

        //noinspection Convert2Lambda
        backupSaveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if ((int) backupSaveGameTask.getValue() == 2) {
                    setCursorWaitOnTask(saveGameTask);
                    new WorkerThread(saveGameTask).start();
                } else {
                    Util.showError(Util.getUIMessage("alert.errorbackup_header"),
                            Util.getUIMessage("alert.errorbackup_content", Constants.BACKUP_DIRECTORY));
                    setAllControlsDisable(false);
                }
            }
        });

        //noinspection Convert2Lambda
        saveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if ((int) saveGameTask.getValue() != 2) {
                    Util.showError(Util.getUIMessage("alert.errorsaving_header"),
                            Util.getUIMessage("alert.errorsaving_content", Constants.BACKUP_DIRECTORY));
                }
                setAllControlsDisable(false);
                reset();
            }
        });
        setCursorWaitOnTask(backupSaveGameTask);
        new WorkerThread(backupSaveGameTask).start();
    }

    public void setAllControlsDisable(boolean disable) {
        saveDisabled.set(disable);

        characterCombo.setDisable(disable);
        pointsPaneController.disableControls(disable);
        skillsPaneController.disableControls(disable);
        miscPaneController.disableControls(disable);
    }

    @FXML
    public void characterSelected(ActionEvent evt) {
        Toast.cancel();

        if (BooleanUtils.isTrue(State.get().getGameRunning())) {
            reset();
            return;
        }

        if (!(evt.getSource() instanceof ComboBox)) {
            return;
        }

        saveDisabled.set(true);
        characterCombo.setDisable(true);
        ComboBox<?> character = (ComboBox<?>) evt.getSource();

        PlayerCharacterFile playerCharacterFile = (PlayerCharacterFile) character.getSelectionModel().getSelectedItem();
        if (playerCharacterFile == null || StringUtils.isEmpty((playerCharacterFile.getPlayerName()))) {
            return;
        }

        pointsPaneController.disableControls(false);
        miscPaneController.reset();
        miscPaneController.disableControls(false);

        MyTask<Boolean> loadTask = new MyTask<>() {
            @Override
            protected Boolean call() {
                return player.loadPlayer(playerCharacterFile.getPlayerName(), playerCharacterFile.isExternal());
            }
        };

        //noinspection Convert2Lambda
        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                pointsPaneController.loadCharHandler();
                miscPaneController.loadCharEventHandler();
                if (pointsPaneController.getCurrentAvail() >= 0) {
                    saveDisabled.set(false);
                }
                characterCombo.setDisable(false);
                miscPaneController.disableControls(false);
                tabPane.getSelectionModel().select(attributesTab);
            }
        });

        //noinspection Convert2Lambda
        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                skillsPaneController.loadCharEventHandler();
            }
        });

        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if (player.getSaveData().getPlatform().equals(br.com.pinter.tqrespec.save.Platform.MOBILE)
                        && !db.getPlatform().equals(Db.Platform.MOBILE)) {
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            Util.getUIMessage("main.mobileSavegameToast_header"),
                            Util.getUIMessage("main.mobileSavegameToast_content"),
                            5000);
                } else if (player.getSaveData().getPlatform().equals(br.com.pinter.tqrespec.save.Platform.WINDOWS)
                        && !db.getPlatform().equals(Db.Platform.WINDOWS)) {
                    reset();
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            Util.getUIMessage("main.mobileDatabaseToast_header"),
                            Util.getUIMessage("main.mobileDatabaseToast_content"),
                            12000);
                }

            }
        });

        new WorkerThread(loadTask).start();
    }

    @FXML
    public void startMoveWindow(MouseEvent evt) {
        if (evt.getButton() == MouseButton.PRIMARY) {
            isMoving = true;
            Window w = rootelement.getScene().getWindow();
            rootelement.getScene().setCursor(Cursor.CLOSED_HAND);
            dragX = w.getX() - evt.getScreenX();
            dragY = w.getY() - evt.getScreenY();
        }
    }

    @FXML
    public void endMoveWindow(MouseEvent evt) {
        if (isMoving) {
            rootelement.getScene().setCursor(Cursor.DEFAULT);
            isMoving = false;
        }
    }

    @FXML
    public void moveWindow(MouseEvent evt) {
        if (isMoving) {
            Window w = rootelement.getScene().getWindow();
            w.setX(dragX + evt.getScreenX());
            w.setY(dragY + evt.getScreenY());
        }
    }
}

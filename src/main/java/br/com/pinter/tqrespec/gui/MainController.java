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

import br.com.pinter.tqrespec.core.MyEventHandler;
import br.com.pinter.tqrespec.core.MyTask;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.core.WorkerThread;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.save.PlayerWriter;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import br.com.pinter.tqrespec.util.Version;
import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class MainController implements Initializable {
    private static final Logger logger = Log.getLogger();

    @Inject
    private FXMLLoader fxmlLoaderAbout;

    @Inject
    private PlayerData playerData;

    @Inject
    private PlayerWriter playerWriter;

    @Inject
    private HostServices hostServices;

    @FXML
    private VBox rootelement;

    @FXML
    private ComboBox<String> characterCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Label mainFormTitle;

    @FXML
    private Hyperlink versionCheck;

    @FXML
    private Button copyButton;

    @FXML
    private TextField copyCharInput;

    private double dragX;
    private double dragY;
    private boolean isMoving = false;

    public static final BooleanProperty mainFormInitialized = new SimpleBooleanProperty();

    @FXML
    public GridPane pointsPane;

    @FXML
    public AttributesPaneController pointsPaneController;

    @FXML
    public GridPane skillsPane;

    @FXML
    public SkillsPaneController skillsPaneController;

    @FXML
    public Button resetButton;

    public final BooleanProperty saveDisabled = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainFormTitle.setText(String.format("%s v%s", Util.getBuildTitle(), Util.getBuildVersion()));
        mainFormInitialized.addListener(((observable, oldValue, newValue) -> {
            MyTask windowShownTask = new MyTask() {
                @Override
                protected Void call() {
                    windowShownHandler();
                    return null;
                }
            };
            new WorkerThread(windowShownTask).start();
        }));

        Task<Version> taskCheckVersion = new Task<>() {
            @Override
            protected Version call() {
                Version version = new Version(Util.getBuildVersion());
                version.checkNewerVersion(Constants.VERSION_CHECK_URL);
                //new version available (-1 our version is less than remote, 0 equal, 1 greater, -2 error checking
                return version;
            }
        };

        taskCheckVersion.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent e) -> {
            Version version = taskCheckVersion.getValue();
            if (version == null || version.getLastCheck() != -1 || StringUtils.isEmpty(version.getUrlPage())) {
                versionCheck.setDisable(true);
                return;
            }
            versionCheck.setOnAction(event -> {
                final Task<Void> openUrl = new Task<>() {
                    @Override
                    public Void call() {
                        try {
                            hostServices.showDocument(new URI(version.getUrlPage()).toString());
                        } catch (URISyntaxException e) {
                            //ignored
                        }
                        return null;
                    }
                };
                new Thread(openUrl).start();
            });
            versionCheck.setText(Util.getUIMessage("about.newversion"));
        });
        //initialize properties and bind them to respective properties in the tab controllers
        saveDisabled.setValue(saveButton.isDisable());
        pointsPaneController.setSaveDisabled(saveButton.isDisabled());
        skillsPaneController.setSaveDisabled(saveButton.isDisabled());
        saveButton.disableProperty().bindBidirectional(saveDisabled);
        saveDisabled.bindBidirectional(pointsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(skillsPaneController.saveDisabledProperty());

        //set icons
        resetButton.setGraphic(Icon.FA_UNDO.create(1.4));
        saveButton.setGraphic(Icon.FA_SAVE.create());
        copyButton.setGraphic(Icon.FA_COPY.create());

        State.get().gameRunningProperty().addListener((value, oldV, newV) -> {
            if (newV) {
                Platform.runLater(() -> {
                    reset();
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            Util.getUIMessage("alert.errorgamerunning_header"),
                            Util.getUIMessage("alert.errorgamerunning_content"),
                            8000);
                });
            }
        });

        new Thread(taskCheckVersion).start();
    }

    private void addCharactersToCombo() {
        try {
            characterCombo.getSelectionModel().clearSelection();
            characterCombo.getItems().setAll(GameInfo.getInstance().getPlayerListMain());
            characterCombo.getItems().sort(String::compareTo);
        } catch (Exception e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    private void windowShownHandler() {
        assert characterCombo == null : "fx:id=\"characterCombo\" not found in FXML.";
        addCharactersToCombo();
    }

    @FXML
    public void close(MouseEvent evt) {
        Util.closeApplication();
    }

    @FXML
    public void openAboutWindow(MouseEvent evt) {
        Parent root;
        try {
            if (fxmlLoaderAbout.getRoot() == null) {
                fxmlLoaderAbout.setLocation(getClass().getResource("/fxml/about.fxml"));
                fxmlLoaderAbout.setResources(ResourceBundle.getBundle("i18n.UI"));
                root = fxmlLoaderAbout.load();
            } else {
                root = fxmlLoaderAbout.getRoot();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
            return;
        }

        Stage stage = new Stage();
        //remove default window decoration

        if (SystemUtils.IS_OS_WINDOWS) {
            stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            stage.initStyle(StageStyle.UNDECORATED);
        }

        //disable maximize
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.getIcons().addAll(new Image("icon/icon64.png"), new Image("icon/icon32.png"), new Image("icon/icon16.png"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(Util.getUIMessage("about.title", Util.getBuildTitle()));
        Scene scene;
        if (root.getScene() == null) {
            scene = new Scene(root);
        } else {
            scene = root.getScene();
        }
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setMinHeight(root.minHeight(-1));
        stage.setMinWidth(root.minWidth(-1));
        stage.setMaxHeight(root.maxHeight(-1));
        stage.setMaxWidth(root.maxWidth(-1));

        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        }));

        stage.show();
    }

    @FXML
    public void copyCharInputChanged(KeyEvent event) {
        if (characterCombo.getValue() == null || characterCombo.getValue().isEmpty()) {
            return;
        }
        String str = copyCharInput.getText();
        int caret = copyCharInput.getCaretPosition();
        StringBuilder newStr = new StringBuilder();

        for (char c : str.toCharArray()) {
            //all characters above 0xFF needs to have accents stripped
            if (c > 0xFF) {
                newStr.append(StringUtils.stripAccents(Character.toString(c)).toCharArray()[0]);
            } else {
                //noinspection RegExpSingleCharAlternation
                newStr.append(Character.toString(c).replaceAll("\\\\|/|:|\\*|\\?|\"|<|>|\\||;", ""));
            }
        }
        copyCharInput.setText(newStr.toString());
        copyCharInput.positionCaret(caret);
        copyButton.setDisable(copyCharInput.getText().isBlank());
    }

    @FXML
    public void resetButtonClicked(ActionEvent event) {
        reset();
    }

    public void reset() {
        pointsPaneController.clearProperties();
        skillsPaneController.resetSkilltabControls();
        playerData.reset();
        copyCharInput.clear();
        copyCharInput.setDisable(true);
        characterCombo.setValue(null);
        characterCombo.getItems().clear();
        addCharactersToCombo();
        characterCombo.setDisable(false);
        setAllControlsDisable(true);
        skillsPaneController.resetSkilltabControls();
        pointsPaneController.clearProperties();
    }

    @FXML
    public void copyChar(ActionEvent evt) {
        if (gameRunningAlert()) {
            return;
        }
        String targetPlayerName = copyCharInput.getText();
        setAllControlsDisable(true);

        MyTask<Integer> copyCharTask = new MyTask<>() {
            @Override
            protected Integer call() {
                try {
                    playerWriter.copyCurrentSave(targetPlayerName);
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
                if ((int) copyCharTask.getValue() == 2) {
                    playerData.reset();
                    reset();
                    setAllControlsDisable(false);
                    addCharactersToCombo();
                    if (characterCombo.getItems().contains(targetPlayerName)) {
                        characterCombo.setValue(targetPlayerName);
                    }
                } else if ((int) copyCharTask.getValue() == 3) {
                    Util.showError("Target Directory already exists!",
                            String.format("The specified target directory already exists. Aborting the copy to character '%s'",
                                    targetPlayerName));
                } else {
                    Util.showError(Util.getUIMessage("alert.errorcopying_header"),
                            Util.getUIMessage("alert.errorcopying_content", targetPlayerName));
                }
                setAllControlsDisable(false);
            }
        });

        new WorkerThread(copyCharTask).start();

    }

    private boolean gameRunningAlert() {
        if (State.get().getGameRunning()) {
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
                try {
                    pointsPaneController.saveCharHandler();
                    return playerWriter.saveCurrent() ? 2 : 0;
                } catch (Exception e) {
                    throw new UnhandledRuntimeException("Error saving character", e);
                }
            }
        };

        //noinspection Convert2Lambda
        backupSaveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if ((int) backupSaveGameTask.getValue() == 2) {
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

            }
        });

        new WorkerThread(backupSaveGameTask).start();
    }

    private void setAllControlsDisable(boolean disable) {
        saveDisabled.set(disable);
        if (copyCharInput.getText().length() > 0 || disable) {
            copyButton.setDisable(disable);
        }

        pointsPaneController.setSpinnersDisable(disable);
        skillsPaneController.disableControls(disable);
    }

    @FXML
    public void characterSelected(ActionEvent evt) {
        if (State.get().getGameRunning()) {
            reset();
            return;
        }

        saveDisabled.set(true);
        copyButton.setDisable(true);
        characterCombo.setDisable(true);
        copyCharInput.clear();
        ComboBox character = (ComboBox) evt.getSource();
        String playerName = (String) character.getSelectionModel().getSelectedItem();
        if (StringUtils.isEmpty((playerName))) {
            return;
        }

        pointsPaneController.setSpinnersDisable(false);

        MyTask<Boolean> loadTask = new MyTask<>() {
            @Override
            protected Boolean call() {
                return playerData.loadPlayer(playerName);
            }
        };

        //noinspection Convert2Lambda
        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                pointsPaneController.loadCharHandler();
                if (pointsPaneController.getCurrentAvail() >= 0) {
                    saveDisabled.set(false);
                }
                characterCombo.setDisable(false);
                copyCharInput.setDisable(false);
            }
        });

        //noinspection Convert2Lambda
        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                skillsPaneController.loadCharEventHandler();
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

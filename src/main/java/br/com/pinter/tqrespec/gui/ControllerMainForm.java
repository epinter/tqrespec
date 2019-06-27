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

import br.com.pinter.tqrespec.*;
import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.save.PlayerParser;
import br.com.pinter.tqrespec.save.PlayerWriter;
import br.com.pinter.tqrespec.Constants;
import br.com.pinter.tqrespec.save.SaveData;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import org.jboss.weld.environment.se.contexts.ThreadScoped;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.ResourceBundle;

@SuppressWarnings({"RedundantThrows", "unused"})
public class ControllerMainForm implements Initializable {
    private static final boolean DBG = false;

    @Inject
    private PlayerData playerData;

    @Inject
    private PlayerParser playerParser;

    @Inject
    private PlayerWriter playerWriter;

    @FXML
    private VBox rootelement;

    @FXML
    private ComboBox characterCombo;

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

    public static BooleanProperty mainFormInitialized = new SimpleBooleanProperty();

    @FXML
    public GridPane pointsPane;

    @FXML
    public PointsPaneController pointsPaneController;

    @FXML
    public GridPane skillsPane;

    @FXML
    public SkillsPaneController skillsPaneController;

    public BooleanProperty saveDisabled = new SimpleBooleanProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainFormTitle.setText(String.format("%s v%s", Util.getBuildTitle(), Util.getBuildVersion()));
        mainFormInitialized.addListener(((observable, oldValue, newValue) -> {
            TaskWithException windowShownTask = new TaskWithException() {
                @Override
                protected Void call() throws Exception {
                    windowShownHandler();
                    return null;
                }
            };
            new WorkerThread(windowShownTask).start();
        }));

        Task<Version> taskCheckVersion = new Task<>() {
            @Override
            protected Version call() throws Exception {
                Version version = new Version(Util.getBuildVersion());
                version.checkNewerVersion(Constants.VERSION_CHECK_URL);
                //new version available (-1 our version is less than remote, 0 equal, 1 greater, -2 error checking
                return version;
            }
        };

        taskCheckVersion.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent e) -> {
            Version version = taskCheckVersion.getValue();
            if (version != null && version.getLastCheck() == -1) {
                versionCheck.setOnAction(event -> {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
                        final Task<Void> openUrl = new Task<>() {
                            @Override
                            public Void call() throws Exception {
                                if (StringUtils.isNotEmpty(version.getUrlPage()))
                                    Desktop.getDesktop().browse(new URI(version.getUrlPage()));
                                return null;
                            }
                        };
                        new Thread(openUrl).start();
                    }
                });
                versionCheck.setText(Util.getUIMessage("about.newversion"));
            } else {
                versionCheck.setDisable(true);
            }
        });
        //initialize properties and bind them to respective properties in the tab controllers
        saveDisabled.setValue(saveButton.isDisable());
        pointsPaneController.setSaveDisabled(saveButton.isDisabled());
        skillsPaneController.setSaveDisabled(saveButton.isDisabled());
        saveButton.disableProperty().bindBidirectional(saveDisabled);
        saveDisabled.bindBidirectional(pointsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(skillsPaneController.saveDisabledProperty());

        new Thread(taskCheckVersion).start();
    }

    private void addCharactersToCombo() {
        try {
            characterCombo.getSelectionModel().clearSelection();
            characterCombo.getItems().clear();
            for (String playerName : GameInfo.getInstance().getPlayerListMain()) {
                //noinspection unchecked
                characterCombo.getItems().add(playerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void windowShownHandler() throws Exception {
        assert characterCombo == null : "fx:id=\"characterCombo\" not found in FXML.";
        addCharactersToCombo();
    }

    public void close(MouseEvent evt) {
        Util.closeApplication();
    }

    public void openAboutWindow(MouseEvent evt) {
        Parent root;
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.UI");
        try {
            root = FXMLLoader.load(getClass().getResource("/fxml/about.fxml"), bundle);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage stage = new Stage();


        //remove default window decoration
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            stage.initStyle(StageStyle.UNDECORATED);
        }

        //disable maximize
        stage.resizableProperty().setValue(Boolean.FALSE);
        stage.getIcons().addAll(new Image("icon/icon64.png"), new Image("icon/icon32.png"), new Image("icon/icon16.png"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(Util.getUIMessage("about.title", Util.getBuildTitle()));
        Scene scene = new Scene(root);
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
        if (copyCharInput.getText().length() > 0) {
            copyButton.setDisable(false);
        } else {
            copyButton.setDisable(true);
        }
    }

    @FXML
    public void copyChar(ActionEvent evt) {
        String targetPlayerName = copyCharInput.getText();
        setAllControlsDisable(true);

        TaskWithException<Integer> copyCharTask = new TaskWithException<>() {
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

        copyCharTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            if ((int) copyCharTask.getValue() == 2) {
                playerData.reset();
                setAllControlsDisable(false);
                addCharactersToCombo();
                if (characterCombo.getItems().contains(targetPlayerName)) {
                    //noinspection unchecked
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
        });

        new WorkerThread(copyCharTask).start();

    }

    @FXML
    public void saveChar(ActionEvent evt) {
        SimpleIntegerProperty backupCreated = new SimpleIntegerProperty();
        SimpleIntegerProperty characterSaved = new SimpleIntegerProperty();

        TaskWithException<Integer> backupSaveGameTask = new TaskWithException<>() {
            @Override
            protected Integer call() throws Exception {
                if (DBG) System.out.println("starting backup task");
                setAllControlsDisable(true);
                return playerWriter.backupCurrent() ? 2 : 0;
            }
        };
        TaskWithException<Integer> saveGameTask = new TaskWithException<>() {
            @Override
            protected Integer call() throws Exception {
                pointsPaneController.saveCharHandler();
                return playerWriter.saveCurrent() ? 2 : 0;
            }
        };

        backupSaveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            if (DBG) System.out.println("starting backup listener");

            if ((int) backupSaveGameTask.getValue() == 2) {
                if (DBG) System.out.println("backupcreated==" + backupCreated.get());
                new WorkerThread(saveGameTask).start();
            } else {
                if (DBG) System.out.println("backupcreated==+=" + backupCreated.get());
                Util.showError(Util.getUIMessage("alert.errorbackup_header"),
                        Util.getUIMessage("alert.errorbackup_content", Constants.BACKUP_DIRECTORY));
                setAllControlsDisable(false);

            }
        });

        saveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            if ((int) saveGameTask.getValue() != 2) {
                Util.showError(Util.getUIMessage("alert.errorsaving_header"),
                        Util.getUIMessage("alert.errorsaving_content", Constants.BACKUP_DIRECTORY));
            } else {
                if (DBG) System.out.println("character saved==" + saveGameTask.getValue());
            }
            setAllControlsDisable(false);

        });

        new WorkerThread(backupSaveGameTask).start();

    }

    private void setAllControlsDisable(boolean disable) {
        saveDisabled.set(disable);
        if (copyCharInput.getText().length() > 0 || disable) {
            copyButton.setDisable(disable);
        }

        pointsPaneController.setSpinnersDisable(disable);
        skillsPaneController.disableControls();
    }

    @FXML
    public void characterSelected(ActionEvent evt) throws Exception {
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

        TaskWithException<Boolean> loadTask = new TaskWithException<>() {
            @Override
            protected Boolean call() throws Exception {
                return playerParser.loadPlayer(playerName);
            }
        };

        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            pointsPaneController.loadCharHandler();
            if (pointsPaneController.getCurrentAvail() >= 0) {
                saveDisabled.set(false);
            }
            characterCombo.setDisable(false);
        });

        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            skillsPaneController.loadCharEventHandler();
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

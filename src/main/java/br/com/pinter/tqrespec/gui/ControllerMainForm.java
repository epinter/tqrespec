/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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
import br.com.pinter.tqrespec.save.PlayerWriter;
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
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.text.NumberFormat;
import java.util.ResourceBundle;

@SuppressWarnings({"RedundantThrows", "unused"})
public class ControllerMainForm implements Initializable {
    private static final boolean DBG = false;
    @FXML
    private VBox rootelement;

    @FXML
    private ComboBox characterCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Spinner strSpinner;

    @FXML
    private Spinner intSpinner;

    @FXML
    private Spinner dexSpinner;

    @FXML
    private Spinner lifeSpinner;

    @FXML
    private Spinner manaSpinner;

    @FXML
    private Label availPointsText;

    @FXML
    private Label mainFormTitle;

    @FXML
    private Label experienceText;

    @FXML
    private Label charLevelText;

    @FXML
    private Label charClassText;

    @FXML
    private Label goldText;

    @FXML
    private Label difficultyText;

    @FXML
    private Hyperlink versionCheck;

    @FXML
    private Button copyButton;

    @FXML
    private TextField copyCharInput;

    private SimpleIntegerProperty currentStr = null;
    private SimpleIntegerProperty currentInt = null;
    private SimpleIntegerProperty currentDex = null;
    private SimpleIntegerProperty currentLife = null;
    private SimpleIntegerProperty currentMana = null;
    private SimpleIntegerProperty currentAvail = null;

    private double dragX;
    private double dragY;
    private boolean isMoving = false;

    public static BooleanProperty mainFormInitialized = new SimpleBooleanProperty();

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
                    new PlayerWriter().copyCurrentSave(targetPlayerName);
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
                PlayerData.getInstance().reset();
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
                if (DBG) System.out.println("returning backup task");
                return new PlayerWriter().backupCurrent() ? 2 : 0;
            }
        };
        TaskWithException<Integer> saveGameTask = new TaskWithException<>() {
            @Override
            protected Integer call() throws Exception {
                if (DBG) System.out.println("starting savegame task");

                int strOld = Math.round(PlayerData.getInstance().getChanges().getFloat("str"));
                int intOld = Math.round(PlayerData.getInstance().getChanges().getFloat("int"));
                int dexOld = Math.round(PlayerData.getInstance().getChanges().getFloat("dex"));
                int lifeOld = Math.round(PlayerData.getInstance().getChanges().getFloat("life"));
                int manaOld = Math.round(PlayerData.getInstance().getChanges().getFloat("mana"));
                int modifierOld = PlayerData.getInstance().getChanges().getInt("modifierPoints");

                if (strOld != currentStr.get() && currentStr.get() > 0) {
                    PlayerData.getInstance().getChanges().setFloat("str", currentStr.get());
                }
                if (intOld != currentInt.get() && currentInt.get() > 0) {
                    PlayerData.getInstance().getChanges().setFloat("int", currentInt.get());
                }
                if (dexOld != currentDex.get() && currentDex.get() > 0) {
                    PlayerData.getInstance().getChanges().setFloat("dex", currentDex.get());
                }
                if (lifeOld != currentLife.get() && currentLife.get() > 0) {
                    PlayerData.getInstance().getChanges().setFloat("life", currentLife.get());
                }
                if (manaOld != currentMana.get() && currentMana.get() > 0) {
                    PlayerData.getInstance().getChanges().setFloat("mana", currentMana.get());
                }
                if (modifierOld != currentAvail.get() && currentAvail.get() > 0) {
                    PlayerData.getInstance().getChanges().setInt("modifierPoints", currentAvail.get());
                }
                if (DBG) System.out.println("returning savegame task");
                return new PlayerWriter().saveCurrent() ? 2 : 0;
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
        saveButton.setDisable(disable);
        if (copyCharInput.getText().length() > 0 || disable) {
            copyButton.setDisable(disable);
        }

        setSpinnersDisable(disable);
    }

    private void setSpinnersDisable(boolean disable) {
        lifeSpinner.setDisable(disable);
        manaSpinner.setDisable(disable);
        strSpinner.setDisable(disable);
        intSpinner.setDisable(disable);
        dexSpinner.setDisable(disable);
    }

    @FXML
    public void characterSelected(ActionEvent evt) throws Exception {
        saveButton.setDisable(true);
        copyButton.setDisable(true);
        characterCombo.setDisable(true);
        copyCharInput.clear();
        ComboBox character = (ComboBox) evt.getSource();
        String playerName = (String) character.getSelectionModel().getSelectedItem();
        if (StringUtils.isEmpty((playerName))) {
            return;
        }

        setSpinnersDisable(false);

        TaskWithException<Boolean> loadTask = new TaskWithException<>() {
            @Override
            protected Boolean call() throws Exception {
                return PlayerData.getInstance().loadPlayerData(playerName);
            }
        };

        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (e) -> {
            clearProperties();
            int str = Math.round(PlayerData.getInstance().getChanges().getFloat("str"));
            int inl = Math.round(PlayerData.getInstance().getChanges().getFloat("int"));
            int dex = Math.round(PlayerData.getInstance().getChanges().getFloat("dex"));
            int life = Math.round(PlayerData.getInstance().getChanges().getFloat("life"));
            int mana = Math.round(PlayerData.getInstance().getChanges().getFloat("mana"));
            int modifier = PlayerData.getInstance().getChanges().getInt("modifierPoints");
            if (modifier < 0 || str < 0 || dex < 0 || inl < 0 || life < 0 || mana < 0) {
                Util.showError(Util.getUIMessage("alert.errorloadingchar_header"),
                        Util.getUIMessage("alert.errorloadingchar_content", life, mana, str, inl, dex));
                clearProperties();
                return;
            }
            this.setAvailablePointsField(modifier);
            this.setStrField(str);
            this.setIntField(inl);
            this.setDexField(dex);
            this.setLifeField(life);
            this.setManaField(mana);
            int xp = PlayerData.getInstance().getChanges().getInt("currentStats.experiencePoints");
            int level = PlayerData.getInstance().getChanges().getInt("currentStats.charLevel");
            int gold = PlayerData.getInstance().getChanges().getInt("money");
            String charClass = PlayerData.getInstance().getHeaderInfo().getPlayerClassTag();

            if (StringUtils.isNotEmpty(charClass)) {
                charClassText.setText(Util.getUIMessage("classtags." + charClass));
            }
            int difficulty = PlayerData.getInstance().getChanges().getInt("difficulty");
            difficultyText.setText(Util.getUIMessage(String.format("difficulty.%d", difficulty)));
            experienceText.setText(NumberFormat.getInstance().format(xp));
            charLevelText.setText(String.valueOf(level));
            goldText.setText(NumberFormat.getInstance().format(gold));
            if (currentAvail.get() >= 0) {
                saveButton.setDisable(false);
            }
            characterCombo.setDisable(false);
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

    private void setStrField(int value) {
        if (value < 50) {
            currentAvail.set(currentAvail.get() - ((Constants.STR_ATTR_MIN - value) / Constants.STR_ATTR_STEP));
            value = 50;
        }
        currentStr = new SimpleIntegerProperty(value);
        AttrIntegerSpinnerValueFactory strFactory = new AttrIntegerSpinnerValueFactory(50, currentStr.get(), currentStr.get(), Constants.STR_ATTR_STEP, currentAvail);
        //noinspection unchecked
        strSpinner.setValueFactory(strFactory);
        //noinspection unchecked
        strSpinner.getValueFactory().valueProperty().bindBidirectional(currentStr);
        currentStr.addListener(((observable, oldValue, newValue) -> attributesChanged("str", (int) oldValue, (int) newValue)));


    }

    private void setIntField(int value) {
        if (value < 50 && (value - 50) % Constants.INT_ATTR_STEP == 0) {
            currentAvail.set(currentAvail.get() - ((Constants.INT_ATTR_MIN - value) / Constants.INT_ATTR_STEP));
            value = 50;
        }
        currentInt = new SimpleIntegerProperty(value);
        AttrIntegerSpinnerValueFactory intFactory = new AttrIntegerSpinnerValueFactory(50, currentInt.get(), currentInt.get(), Constants.INT_ATTR_STEP, currentAvail);
        //noinspection unchecked
        intSpinner.setValueFactory(intFactory);
        //noinspection unchecked
        intSpinner.getValueFactory().valueProperty().bindBidirectional(currentInt);
        currentInt.addListener(((observable, oldValue, newValue) -> attributesChanged("int", (int) oldValue, (int) newValue)));
    }

    private void setDexField(int value) {
        if (value < 50 && (value - 50) % Constants.DEX_ATTR_STEP == 0) {
            currentAvail.set(currentAvail.get() - ((Constants.DEX_ATTR_MIN - value) / Constants.DEX_ATTR_STEP));
            value = 50;
        }
        currentDex = new SimpleIntegerProperty(value);
        AttrIntegerSpinnerValueFactory dexFactory = new AttrIntegerSpinnerValueFactory(50, currentDex.get(), currentDex.get(), Constants.DEX_ATTR_STEP, currentAvail);
        //noinspection unchecked
        dexSpinner.setValueFactory(dexFactory);
        //noinspection unchecked
        dexSpinner.getValueFactory().valueProperty().bindBidirectional(currentDex);
        currentDex.addListener(((observable, oldValue, newValue) -> attributesChanged("dex", (int) oldValue, (int) newValue)));
    }

    private void setLifeField(int value) {
        if (value < 300 && (value - 300) % Constants.LIFE_ATTR_STEP == 0) {
            currentAvail.set(currentAvail.get() - ((Constants.LIFE_ATTR_MIN - value) / Constants.LIFE_ATTR_STEP));
            value = 300;
        }
        currentLife = new SimpleIntegerProperty(value);
        AttrIntegerSpinnerValueFactory lifeFactory = new AttrIntegerSpinnerValueFactory(Constants.LIFE_ATTR_MIN, currentLife.get(), currentLife.get(), Constants.LIFE_ATTR_STEP, currentAvail);
        //noinspection unchecked
        lifeSpinner.setValueFactory(lifeFactory);
        //noinspection unchecked
        lifeSpinner.getValueFactory().valueProperty().bindBidirectional(currentLife);
        currentLife.addListener(((observable, oldValue, newValue) -> attributesChanged("life", (int) oldValue, (int) newValue)));
    }

    private void setManaField(int value) {
        if (value < 300 && (value - 300) % Constants.MANA_ATTR_STEP == 0) {
            currentAvail.set(currentAvail.get() - ((Constants.MANA_ATTR_MIN - value) / Constants.MANA_ATTR_STEP));
            value = 300;
        }
        currentMana = new SimpleIntegerProperty(value);
        AttrIntegerSpinnerValueFactory manaFactory = new AttrIntegerSpinnerValueFactory(Constants.MANA_ATTR_MIN, currentMana.get(), currentMana.get(), Constants.MANA_ATTR_STEP, currentAvail);
        //noinspection unchecked
        manaSpinner.setValueFactory(manaFactory);
        //noinspection unchecked
        manaSpinner.getValueFactory().valueProperty().bindBidirectional(currentMana);
        currentMana.addListener(((observable, oldValue, newValue) -> attributesChanged("mana", (int) oldValue, (int) newValue)));
    }

    private void attributesChanged(String attr, int oldValue, int newValue) {
        if (newValue > oldValue && currentAvail.get() > 0) {
            int diff = newValue - oldValue;
            if (attr.equals("str")) {
                if (diff < Constants.STR_ATTR_STEP) return;
                currentStr.set(newValue);
                currentAvail.set(currentAvail.get() - (diff / Constants.STR_ATTR_STEP));
            }
            if (attr.equals("int")) {
                if (diff < Constants.INT_ATTR_STEP) return;
                currentInt.set(newValue);
                currentAvail.set(currentAvail.get() - (diff / Constants.INT_ATTR_STEP));

            }
            if (attr.equals("dex")) {
                if (diff < Constants.DEX_ATTR_STEP) return;
                currentDex.set(newValue);
                currentAvail.set(currentAvail.get() - (diff / Constants.DEX_ATTR_STEP));
            }
            if (attr.equals("life")) {
                if (diff < Constants.LIFE_ATTR_STEP) return;
                currentLife.set(newValue);
                currentAvail.set(currentAvail.get() - (diff / Constants.LIFE_ATTR_STEP));
            }
            if (attr.equals("mana")) {
                if (diff < Constants.MANA_ATTR_STEP) return;
                currentMana.set(newValue);
                currentAvail.set(currentAvail.get() - (diff / Constants.MANA_ATTR_STEP));
            }
        }
        if (newValue < oldValue) {
            int diff = oldValue - newValue;
            if (attr.equals("str")) {
                if (diff < Constants.STR_ATTR_STEP) return;
                currentStr.set(newValue);
                currentAvail.set(currentAvail.get() + (diff / Constants.STR_ATTR_STEP));
            }
            if (attr.equals("int")) {
                if (diff < Constants.INT_ATTR_STEP) return;
                currentInt.set(newValue);
                currentAvail.set(currentAvail.get() + (diff / Constants.INT_ATTR_STEP));
            }
            if (attr.equals("dex")) {
                if (diff < Constants.DEX_ATTR_STEP) return;
                currentDex.set(newValue);
                currentAvail.set(currentAvail.get() + (diff / Constants.DEX_ATTR_STEP));
            }
            if (attr.equals("life")) {
                if (diff < Constants.LIFE_ATTR_STEP) return;
                currentLife.set(newValue);
                currentAvail.set(currentAvail.get() + (diff / Constants.LIFE_ATTR_STEP));
            }
            if (attr.equals("mana")) {
                if (diff < Constants.MANA_ATTR_STEP) return;
                currentMana.set(newValue);
                currentAvail.set(currentAvail.get() + (diff / Constants.MANA_ATTR_STEP));
            }
        }
        if (saveButton.isDisabled() && currentAvail.get() >= 0) {
            saveButton.setDisable(false);
        }
    }

    private void setAvailablePointsField(int value) {
        this.availPointsText.setText(String.valueOf(value));
        currentAvail = new SimpleIntegerProperty(value);
        availPointsText.textProperty().bindBidirectional(currentAvail, new NumberStringConverter());
    }

    private void clearProperties() {
        if (availPointsText.textProperty().isBound())
            availPointsText.textProperty().unbindBidirectional(currentAvail);
        if (strSpinner.getValueFactory() != null && strSpinner.getValueFactory().valueProperty().isBound())
            //noinspection unchecked
            strSpinner.getValueFactory().valueProperty().unbindBidirectional(currentStr);
        if (intSpinner.getValueFactory() != null && intSpinner.getValueFactory().valueProperty().isBound())
            //noinspection unchecked
            intSpinner.getValueFactory().valueProperty().unbindBidirectional(currentInt);
        if (dexSpinner.getValueFactory() != null && dexSpinner.getValueFactory().valueProperty().isBound())
            //noinspection unchecked
            dexSpinner.getValueFactory().valueProperty().unbindBidirectional(currentDex);
        if (lifeSpinner.getValueFactory() != null && lifeSpinner.getValueFactory().valueProperty().isBound())
            //noinspection unchecked
            lifeSpinner.getValueFactory().valueProperty().unbindBidirectional(currentLife);
        if (manaSpinner.getValueFactory() != null && manaSpinner.getValueFactory().valueProperty().isBound())
            //noinspection unchecked
            manaSpinner.getValueFactory().valueProperty().unbindBidirectional(currentMana);
        //noinspection unchecked
        strSpinner.setValueFactory(null);
        //noinspection unchecked
        intSpinner.setValueFactory(null);
        //noinspection unchecked
        dexSpinner.setValueFactory(null);
        //noinspection unchecked
        lifeSpinner.setValueFactory(null);
        //noinspection unchecked
        manaSpinner.setValueFactory(null);
        currentAvail = null;
        currentStr = null;
        currentInt = null;
        currentDex = null;
        currentLife = null;
        currentMana = null;
        if (experienceText != null)
            experienceText.setText("");
        if (charLevelText != null)
            charLevelText.setText("");
        if (goldText != null)
            goldText.setText("");
        if (charClassText != null)
            charClassText.setText("");

    }

}

class AttrIntegerSpinnerValueFactory extends SpinnerValueFactory.IntegerSpinnerValueFactory {
    private SimpleIntegerProperty available;
    private int originalMax = -1;

    AttrIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy, SimpleIntegerProperty available) {
        super(min, max, initialValue, amountToStepBy);
        this.available = available;
    }

    @Override
    public void decrement(int v) {
        if (this.originalMax == -1) {
            this.originalMax = this.getMax();
        }
        int oldValue = this.getValue();
        int step = v * this.getAmountToStepBy();
        int newValue = oldValue - step;
        if (oldValue - step >= this.getMin())
            this.setValue(newValue);
    }

    @Override
    public void increment(int v) {
        if (this.originalMax == -1) {
            this.originalMax = this.getMax();
        }
        int oldValue = this.getValue();
        int step = v * this.getAmountToStepBy();
        int newValue = oldValue + step;
        int pointsAvail = available.get() * step;
        if (oldValue == this.getMax() && available.get() <= 0)
            return;
        this.setMax(oldValue + pointsAvail);
        if (newValue <= this.getMax())
            this.setValue(newValue);
    }
}

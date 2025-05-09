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

import br.com.pinter.tqrespec.core.MyEventHandler;
import br.com.pinter.tqrespec.core.MyTask;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.core.WorkerThread;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.SaveLocation;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.save.player.PlayerWriter;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.DefaultMapTeleport;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.MapTeleport;
import br.com.pinter.tqrespec.tqdata.PlayerCharacterFile;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Build;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

@SuppressWarnings("unused")
public class MainController implements Initializable {
    private static final System.Logger logger = Log.getLogger(MainController.class);
    public static final BooleanProperty mainFormInitialized = new SimpleBooleanProperty();
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private UiPlayerProperties playerProperties;
    private final BooleanProperty unlockedEdit = new SimpleBooleanProperty(false);
    private final BooleanProperty freeLvl = new SimpleBooleanProperty(false);

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
    private CheckVersionService checkVersionService;
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
    @Inject
    private UIUtils uiUtils;
    private double dragX;
    private double dragY;
    private boolean isMoving = false;
    @Inject
    private Txt txt;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.log(DEBUG, "isLocaleLanguageEmpty: " + resources.getLocale().getLanguage().isEmpty());
        if (!State.get().getLocale().equals(Locale.ENGLISH) && resources.getLocale().getLanguage().isEmpty()) {
            ResourceHelper.tryTagText(txt, attributesTab, Constants.UI.TAG_ATTRIBUTESTAB, false, true);
            ResourceHelper.tryTagText(txt, skillsTab, Constants.UI.TAG_SKILLSTAB, false, true);
        }
        mainFormTitle.setText(String.format("%s v%s", Build.title(), Build.version()));
        mainFormInitialized.addListener(((observable, oldValue, newValue) -> new WorkerThread(new MyTask<>() {
            @Override
            protected Void call() {
                windowShownHandler();
                pointsPaneController.windowShownHandler();
                return null;
            }
        }).start()));

        //initialize properties and bind them to respective properties in the tab controllers
        saveDisabled.setValue(saveButton.isDisable());
        miscPaneController.setMainController(this);
        pointsPaneController.setMainController(this);
        skillsPaneController.setMainController(this);
        pointsPaneController.setSaveDisabled(saveButton.isDisabled());
        skillsPaneController.setSaveDisabled(saveButton.isDisabled());
        miscPaneController.setSaveDisabled(saveButton.isDisabled());
        saveButton.disableProperty().bindBidirectional(saveDisabled);
        saveDisabled.bindBidirectional(pointsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(skillsPaneController.saveDisabledProperty());
        saveDisabled.bindBidirectional(miscPaneController.saveDisabledProperty());

        //set icons
        resetButton.setGraphic(Icon.FA_UNDO.create(1.4));
        resetButton.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.resetButtonTooltip")));
        saveButton.setGraphic(Icon.FA_SAVE.create());
        charactersButton.setGraphic(Icon.FA_USERS.create(1.4));
        charactersButton.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.charactersButtonTooltip")));

        State.get().gameRunningProperty().addListener((value, oldV, newV) -> {
            if (BooleanUtils.isTrue(newV)) {
                Platform.runLater(() -> {
                    Parent rootCharWindow = fxmlLoaderCharacter.getRoot();
                    if (rootCharWindow != null) {
                        Stage charactersWindow = (Stage) rootCharWindow.getScene().getWindow();
                        charactersWindow.close();
                    }
                });

                Platform.runLater(() -> {
                    reset();
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            ResourceHelper.getMessage("alert.errorgamerunning_header"),
                            ResourceHelper.getMessage("alert.errorgamerunning_content"),
                            30000);
                });
            }
        });

        checkVersionService.withControl(versionCheck).start();

        miscPaneController.unlockCheckboxSelectedProperty().bindBidirectional(unlockedEdit);
        miscPaneController.freeLvlCheckboxSelectedProperty().bindBidirectional(freeLvl);
        pointsPaneController.onMainInitialized();
    }

    public void addCharactersToCombo() {
        try {
            characterCombo.getSelectionModel().clearSelection();
            characterCombo.getItems().setAll(gameInfo.getPlayerCharacterList(SaveLocation.MAIN, SaveLocation.EXTERNAL));
            characterCombo.setCellFactory(p -> new CharacterListCell());
            characterCombo.getItems().sort(Comparator.comparing(PlayerCharacterFile::getPlayerName));
        } catch (ClassCastException | UnsupportedOperationException | IllegalArgumentException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
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
        uiUtils.closeApplication();
    }

    @FXML
    public void openAboutWindow(MouseEvent evt) throws IOException {
        if (State.get().isSaveInProgress()) {
            return;
        }

        Parent root;
        if (fxmlLoaderAbout.getRoot() == null) {
            fxmlLoaderAbout.setLocation(ResourceHelper.getResourceUrl(Constants.UI.ABOUT_FXML));
            fxmlLoaderAbout.setResources(ResourceBundle.getBundle("i18n.UI", State.get().getLocale()));
            fxmlLoaderAbout.load();
        } else {
            root = fxmlLoaderAbout.getRoot();
            ((Stage) root.getScene().getWindow()).show();
        }
    }

    @FXML
    public void openCharactersWindow(ActionEvent evt) throws IOException {
        if (State.get().isSaveInProgress()) {
            return;
        }

        reset();
        Parent root;
        if (fxmlLoaderCharacter.getRoot() == null) {
            fxmlLoaderCharacter.setLocation(ResourceHelper.getResourceUrl("/fxml/characters.fxml"));
            fxmlLoaderCharacter.setResources(ResourceBundle.getBundle("i18n.UI", State.get().getLocale()));
            fxmlLoaderCharacter.load();
            Parent r = fxmlLoaderCharacter.getRoot();
            Stage charactersWindow = (Stage) r.getScene().getWindow();
            charactersWindow.setOnHiding(e -> reset());
        } else {
            root = fxmlLoaderCharacter.getRoot();
            Stage charactersWindow = (Stage) root.getScene().getWindow();
            charactersWindow.setOnHiding(e -> reset());
            charactersWindow.show();
        }
    }

    @FXML
    public void resetButtonClicked(ActionEvent event) {
        if (!State.get().isSaveInProgress()) {
            reset();
        }
    }

    public void reset() {
        pointsPaneController.clearProperties();
        skillsPaneController.reset();
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
        unlockedEdit.set(false);
        freeLvl.set(false);
        playerProperties = null;
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
        if (BooleanUtils.isTrue(State.get().isGameRunning())) {
            uiUtils.showError(ResourceHelper.getMessage("alert.errorgamerunning_header"),
                    ResourceHelper.getMessage("alert.errorgamerunning_content"));
            return true;
        }
        return false;
    }

    UiPlayerProperties playerProps() {
        return playerProperties;
    }

    public void commitChanges() {
        skillsPaneController.saveHandler();

        int strOld = player.getStr();
        int intOld = player.getInt();
        int dexOld = player.getDex();
        int lifeOld = player.getLife();
        int manaOld = player.getMana();
        int modifierOld = player.getModifierPoints();
        int skillOld = player.getAvailableSkillPoints();
        int altMoneyOld = player.getAltMoney();
        int charLevelOld = player.getLevel();
        int goldOld = player.getMoney();
        int diffOld = player.getDifficulty();
        int boostedX4Old = player.getBoostedCharacterForX4();
        int sacksOld = player.getNumberOfSacks();
        int sackFocOld = player.getCurrentlyFocusedSackNumber();
        int sackSelOld = player.getCurrentlySelectedSackNumber();

        if (strOld != playerProps().getStr() && playerProps().getStr() > 0) {
            player.setStr(playerProps().getStr());
        }
        if (intOld != playerProps().getIntl() && playerProps().getIntl() > 0) {
            player.setInt(playerProps().getIntl());
        }
        if (dexOld != playerProps().getDex() && playerProps().getDex() > 0) {
            player.setDex(playerProps().getDex());
        }
        if (lifeOld != playerProps().getLife() && playerProps().getLife() > 0) {
            player.setLife(playerProps().getLife());
        }
        if (manaOld != playerProps().getMana() && playerProps().getMana() > 0) {
            player.setMana(playerProps().getMana());
        }
        if (modifierOld != playerProps().getAttrAvailable() && playerProps().getAttrAvailable() >= 0) {
            player.setModifierPoints(playerProps().getAttrAvailable());
        }
        if (altMoneyOld != playerProps().getElectrum() && playerProps().getElectrum() >= 0) {
            player.setAltMoney(playerProps().getElectrum());
        }
        if (charLevelOld != playerProps().getCharLevel() && playerProps().getCharLevel() > 0) {
            player.setCharLevel(playerProps().getCharLevel());
        }
        if (goldOld != playerProps().getGold() && playerProps().getGold() >= 0) {
            player.setMoney(playerProps().getGold());
        }
        if (skillOld != playerProps().getSkillAvailable() && playerProps().getSkillAvailable() >= 0) {
            player.setAvailableSkillPoints(playerProps().getSkillAvailable());
        }
        if (diffOld != playerProps().getDifficulty() && playerProps().getDifficulty() >= 0) {
            player.setDifficulty(playerProps().getDifficulty());
            adjustTeleportsForDifficulty();
        }
        if (boostedX4Old != playerProps().getBoostedCharacterForX4()
                && playerProps().getBoostedCharacterForX4() >= 0 && playerProps().getBoostedCharacterForX4() <= 1) {
            player.setBoostedCharacterForX4(playerProps().getBoostedCharacterForX4());
            adjustTeleportsForDifficulty();
        }
        if(sacksOld != playerProps().getNumberOfSacks()) {
            player.setNumberOfSacks(playerProps().getNumberOfSacks());
            player.addEmptyPlayerSacks();
            if(sackFocOld != playerProps().getCurrentlyFocusedSackNumber()) {
                player.setCurrentlyFocusedSackNumber(playerProps().getCurrentlyFocusedSackNumber());
            }
            if(sackSelOld != playerProps().getCurrentlySelectedSackNumber()) {
                player.setCurrentlySelectedSackNumber(playerProps().getCurrentlySelectedSackNumber());
            }
        }

        if (playerProps().isMarkResetStat()) {
            if (player.getStatPlayTimeInSeconds() != playerProps().getStatplaytime()) {
                player.setStatPlayTimeInSeconds(playerProps().getStatplaytime());
            }
            if (player.getStatGreatestMonsterKilledName() != null
                    && !player.getStatGreatestMonsterKilledName().equals(playerProps().getStatmonsterkilledname())) {
                player.resetStatGreatestMonsterKilledName();
            }
            if (player.getStatGreatestMonsterKilledLevel() != playerProps().getStatmonsterkilledlevel()) {
                player.resetStatGreatestMonsterKilledLevel();
            }
            if (player.getStatNumberOfKills() != playerProps().getStatkills()) {
                player.setStatNumberOfKills(playerProps().getStatkills());
            }
            if (player.getStatNumberOfDeaths() != playerProps().getStatdeath()) {
                player.setStatNumberOfDeaths(playerProps().getStatdeath());
            }
            if (player.getStatNumberOfDeaths() != playerProps().getStatdeath()) {
                player.setStatNumberOfDeaths(playerProps().getStatdeath());
            }
            if (player.getStatHealthPotionsUsed() != playerProps().getStathealthpotionused()) {
                player.setStatHealthPotionsUsed(playerProps().getStathealthpotionused());
            }
            if (player.getStatManaPotionsUsed() != playerProps().getStatmanapotionused()) {
                player.setStatManaPotionsUsed(playerProps().getStatmanapotionused());
            }
            if (player.getStatExperienceFromKills() != playerProps().getStatxpfromkills()) {
                player.setStatExperienceFromKills(playerProps().getStatxpfromkills());
            }
            if (player.getStatNumHitsReceived() != playerProps().getStathitsreceived()) {
                player.setStatNumHitsReceived(playerProps().getStathitsreceived());
            }
            if (player.getStatNumHitsInflicted() != playerProps().getStathitsinflicted()) {
                player.setStatNumHitsInflicted(playerProps().getStathitsinflicted());
            }
            if (player.getStatCriticalHitsReceived() != playerProps().getStatcriticalreceived()) {
                player.setStatCriticalHitsReceived(playerProps().getStatcriticalreceived());
            }
            if (player.getStatCriticalHitsInflicted() != playerProps().getStatcriticalinflicted()) {
                player.setStatCriticalHitsInflicted(playerProps().getStatcriticalinflicted());
            }
        }

        //keep after any teleport operation like difficulty and boostedCharacterForX4
        for (Map.Entry<TeleportItem, String> e : playerProps().getTeleportChanges().entrySet()) {
            if (e.getValue().equals(TeleportItem.Ops.INSERT.name())) {
                player.insertTeleport(e.getKey().getDifficulty(), e.getKey().getTeleport().getUid());
                logger.log(DEBUG, "INSERT TELEPORT: " + e.getKey());
            } else if (e.getValue().equals(TeleportItem.Ops.REMOVE.name())) {
                player.removeTeleport(e.getKey().getDifficulty(), e.getKey().getTeleport().getUid());
                logger.log(DEBUG, "REMOVE TELEPORT: " + e.getKey());
            }
        }
        if (!playerProps().getTeleportChanges().isEmpty()) {
            player.setTeleportUIDsSize();
        }
    }

    private void adjustTeleportsForDifficulty() {
        for (int i = 0; i <= playerProps().getDifficulty(); i++) {
            List<MapTeleport> teleports = player.getDefaultMapTeleports(i);
            MapTeleport tp;
            try {
                tp = DefaultMapTeleport.get(0);
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Invalid map teleport data, order 0", e);
            }
            if (!teleports.contains(tp)) {
                logger.log(WARNING, "Savegame doesn't have the portal '{0}' open on difficulty '{1}', adding.",
                        tp.getName() != null ? tp.getName() : tp.getUid(), i);
                playerProps().putTeleportChange(new TeleportItem(tp, i), TeleportItem.Ops.INSERT);
            }
        }
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
                commitChanges();
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
                    uiUtils.showError(ResourceHelper.getMessage("alert.errorbackup_header"),
                            ResourceHelper.getMessage("alert.errorbackup_content", Constants.BACKUP_DIRECTORY));
                    setAllControlsDisable(false);
                }
            }
        });

        //noinspection Convert2Lambda
        saveGameTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                if ((int) saveGameTask.getValue() != 2) {
                    uiUtils.showError(ResourceHelper.getMessage("alert.errorsaving_header"),
                            ResourceHelper.getMessage("alert.errorsaving_content", Constants.BACKUP_DIRECTORY));
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

    public boolean isCharacterSelected() {
        return !characterCombo.getSelectionModel().isEmpty();
    }

    @FXML
    public void characterSelected(ActionEvent evt) {
        if (State.get().isGameRunning()) {
            return;
        }

        Toast.cancel();

        if (!(evt.getSource() instanceof ComboBox<?> character)) {
            return;
        }

        unlockedEdit.set(false);
        freeLvl.set(false);
        saveDisabled.set(true);
        characterCombo.setDisable(true);

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
                boolean loaded = player.loadPlayer(playerCharacterFile.getPlayerName(), playerCharacterFile.getLocation());
                if (player.getHasBeenInGame() == 0) {
                    Platform.runLater(() -> {
                        reset();
                        Toast.show((Stage) rootelement.getScene().getWindow(),
                                ResourceHelper.getMessage("main.savenotstartedToast_header"),
                                ResourceHelper.getMessage("main.savenotstartedToast_content"),
                                10000);
                    });
                    return cancel();
                }
                return loaded;
            }
        };

        //noinspection Convert2Lambda
        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new MyEventHandler<>() {
            @Override
            public void handleEvent(WorkerStateEvent workerStateEvent) {
                playerProperties = new UiPlayerProperties(player);
                pointsPaneController.loadCharHandler();
                miscPaneController.loadCharEventHandler();
                if (playerProperties.getAttrAvailable() >= 0) {
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
                            ResourceHelper.getMessage("main.mobileSavegameToast_header"),
                            ResourceHelper.getMessage("main.mobileSavegameToast_content"),
                            5000);
                } else if (player.getSaveData().getPlatform().equals(br.com.pinter.tqrespec.save.Platform.WINDOWS)
                        && !db.getPlatform().equals(Db.Platform.WINDOWS)) {
                    reset();
                    Toast.show((Stage) rootelement.getScene().getWindow(),
                            ResourceHelper.getMessage("main.mobileDatabaseToast_header"),
                            ResourceHelper.getMessage("main.mobileDatabaseToast_content"),
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

    public boolean isUnlockedEdit() {
        return unlockedEdit.get();
    }

    public BooleanProperty unlockedEditProperty() {
        return unlockedEdit;
    }

    public boolean isFreeLvl() {
        return freeLvl.get();
    }

    public BooleanProperty freeLvlProperty() {
        return freeLvl;
    }
}
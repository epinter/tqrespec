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

import br.com.pinter.tqrespec.core.MyTask;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.core.WorkerThread;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.player.PlayerLoader;
import br.com.pinter.tqrespec.tqdata.*;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.util.Callback;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CharactersViewController implements Initializable {
    private static final System.Logger logger = Log.getLogger(CharactersViewController.class.getName());
    private double dragX;
    private double dragY;
    private boolean isMoving = false;

    @FXML
    private VBox rootElement;

    @FXML
    private Label charFormTitle;

    @Inject
    private GameInfo gameInfo;

    @Inject
    private PlayerLoader player;

    @Inject
    private Txt txt;

    @FXML
    private Button exportButton;

    @FXML
    private TableView<PlayerCharacter> charactersTable;

    @FXML
    private TableColumn<PlayerCharacter, String> colName;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colLevel;

    @FXML
    private TableColumn<PlayerCharacter, String> colGender;

    @FXML
    private TableColumn<PlayerCharacter, String> colClass;

    @FXML
    private TableColumn<PlayerCharacter, String> colDifficulty;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colExperience;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colGold;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colLife;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colMana;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colStr;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colInt;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colDex;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colAvailableAttr;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colAvailableSkill;

    @FXML
    private TableColumn<PlayerCharacter, String> colMasteryOne;

    @FXML
    private TableColumn<PlayerCharacter, String> colMasteryTwo;

    @FXML
    private TableColumn<PlayerCharacter, String> colPlayTimeInSeconds;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colNumberOfDeaths;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colNumberOfKills;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colGreatestDamageInflicted;

    @FXML
    private TableColumn<PlayerCharacter, String> colGreatestMonsterKilled;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colExperienceFromKills;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colHealthPotionsUsed;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colManaPotionsUsed;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colNumHitsReceived;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colNumHitsInflicted;

    @FXML
    private TableColumn<PlayerCharacter, Integer> colCriticalHitsInflicted;

    @FXML
    private TableColumn<PlayerCharacter, String> colLastTeleport;

    private List<PlayerCharacter> characters;

    private AtomicBoolean loadingCharacters = new AtomicBoolean(false);

    @FXML
    public void closeWindow(@SuppressWarnings("unused") MouseEvent evt) {
        if(loadingCharacters.get()) {
            return;
        }
        charactersTable.scrollToColumnIndex(0);
        charactersTable.scrollTo(0);
        charactersTable.getItems().clear();
        charactersTable.getSortOrder().clear();
        Stage stage = (Stage) rootElement.getScene().getWindow();
        player.reset();
        characters = null;
        stage.close();
        stage.setWidth(stage.getMinWidth());
        stage.setHeight(stage.getMinHeight());
    }

    @FXML
    public void startMoveWindow(@SuppressWarnings("unused") MouseEvent evt) {
        if (evt.getButton() == MouseButton.PRIMARY) {
            isMoving = true;
            Window w = rootElement.getScene().getWindow();
            rootElement.getScene().setCursor(Cursor.CLOSED_HAND);
            dragX = w.getX() - evt.getScreenX();
            dragY = w.getY() - evt.getScreenY();
        }
    }

    @FXML
    public void endMoveWindow(@SuppressWarnings("unused") MouseEvent evt) {
        if (isMoving) {
            rootElement.getScene().setCursor(Cursor.DEFAULT);
            isMoving = false;
        }
    }

    @FXML
    public void moveWindow(MouseEvent evt) {
        if (isMoving) {
            Window w = rootElement.getScene().getWindow();
            w.setX(dragX + evt.getScreenX());
            w.setY(dragY + evt.getScreenY());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        player.reset();

        Scene scene = new Scene(rootElement);
        Stage stage = new Stage();

        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.getIcons().addAll(IconHelper.getAppIcons());

        //disable maximize
        stage.resizableProperty().setValue(Boolean.FALSE);

        ResizeListener listener = new ResizeListener(stage);
        listener.setScale(false);
        listener.setKeepAspect(false);
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, listener);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, listener);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, listener);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, listener);


        // min* and max* set to -1 will force javafx to use values defined on root element
        stage.setMinHeight(rootElement.minHeight(-1));
        stage.setMinWidth(rootElement.minWidth(-1));
        stage.setMaxHeight(rootElement.maxHeight(-1));
        stage.setMaxWidth(rootElement.maxWidth(-1));

        //remove default window decoration
        if (SystemUtils.IS_OS_WINDOWS) {
            stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            stage.initStyle(StageStyle.UNDECORATED);
        }

        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        }));
        stage.setTitle(Util.getUIMessage("characters.title", Util.getBuildTitle()));
        charFormTitle.setText(Util.getUIMessage("characters.title", Util.getBuildTitle()));

        stage.addEventHandler(WindowEvent.WINDOW_SHOWING, e -> new WorkerThread(new MyTask<>() {
            @Override
            protected Void call() {
                loadingCharacters.set(true);

                Platform.runLater(() -> {
                    charactersTable.setPlaceholder(new Label(Util.getUIMessage("characters.loadingPlaceholder")));
                    rootElement.getScene().setCursor(Cursor.WAIT);

                });

                characters = new ArrayList<>();
                for (String p : gameInfo.getPlayerListMain()) {
                    try {
                        player.loadPlayer(p);
                    } catch (RuntimeException e) {
                        logger.log(System.Logger.Level.ERROR, String.format("Error loading character '%s'", p));
                        continue;
                    }
                    characters.add(player.getCharacter());
                }

                Platform.runLater(() -> {
                    setupTable();
                    charactersTable.setPlaceholder(new Label(""));
                    Platform.runLater(() -> rootElement.getScene().setCursor(Cursor.DEFAULT));
                    loadingCharacters.set(false);
                });
                return null;
            }
        }).start());

        exportButton.setGraphic(Icon.FA_FILE_EXPORT.create());
        stage.show();
    }

    private void setupTable() {
        //setup tableview
        setupTableColumnString(colName, Util.getUIMessage("characters.characterName"), "name");
        setupTableColumnInteger(colLevel, Util.getUIMessage("main.charlevel"), "level");
        setupTableColumnString(colGender, Util.getUIMessage("main.gender"), null);
        colGender.setCellValueFactory(f -> new SimpleStringProperty(
                Util.getUIMessage("main.gender." + f.getValue().getGender().name().toLowerCase())));

        setupTableColumnString(colClass, Util.getUIMessage("main.charclass"), "characterClass");
        setupTableColumnString(colDifficulty, Util.getUIMessage("main.difficulty"), null);
        setupTableColumnInteger(colExperience, Util.getUIMessage("main.experience"), "experience");
        setupTableColumnInteger(colGold, Util.getUIMessage("main.gold"), "gold");
        setupTableColumnInteger(colLife, Util.getUIMessage("main.health"), "statLife");
        setupTableColumnInteger(colMana, Util.getUIMessage("main.energy"), "statMana");
        setupTableColumnInteger(colStr, Util.getUIMessage("main.strength"), "statStr");
        setupTableColumnInteger(colInt, Util.getUIMessage("main.intelligence"), "statInt");
        setupTableColumnInteger(colDex, Util.getUIMessage("main.dexterity"), "statDex");
        setupTableColumnInteger(colAvailableAttr, Util.getUIMessage("characters.attributePoints"), "statAvailableAttrPoints");
        setupTableColumnInteger(colAvailableSkill, Util.getUIMessage("characters.skillPoints"), "statAvailableSkillPoints");
        setupTableColumnString(colMasteryOne, Util.getUIMessage(Constants.Msg.CHARACTERS_TITLE_MASTERY), null);
        setupTableColumnString(colMasteryTwo, Util.getUIMessage(Constants.Msg.CHARACTERS_TITLE_MASTERY), null);
        setupTableColumnString(colLastTeleport, Util.getUIMessage("characters.lastTeleport"), null);

        colMasteryOne.setCellValueFactory(f -> {
            if (f.getValue().getMasteries() != null && !f.getValue().getMasteries().isEmpty()) {
                Mastery mastery = f.getValue().getMasteries().get(0);
                String s = txt.getCapitalizedString(mastery.getDisplayName());
                return new SimpleStringProperty(String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, s, mastery.getLevel()));
            }
            return null;
        });

        colMasteryTwo.setCellValueFactory(f -> {
            if (f.getValue().getMasteries() != null && f.getValue().getMasteries().size() > 1) {
                Mastery mastery = f.getValue().getMasteries().get(1);
                String s = txt.getCapitalizedString(mastery.getDisplayName());
                return new SimpleStringProperty(String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, s, mastery.getLevel()));
            }
            return null;
        });

        colDifficulty.setCellValueFactory(f -> {
            String difficultyText;
            String difficultyTextValue = String.format("%s%02d",
                    Constants.UI.PREFIXTAG_DIFFICULTYLABEL, f.getValue().getDifficulty() + 1);
            if (txt.isTagStringValid(difficultyTextValue)) {
                difficultyText = Util.cleanTagString(txt.getString(difficultyTextValue));
            } else {
                difficultyText = Util.getUIMessage(String.format("difficulty.%d", f.getValue().getDifficulty()));
            }
            return new SimpleStringProperty(difficultyText);
        });

        setupTableColumnString(colPlayTimeInSeconds, txt.getString(Constants.UI.TAG_STAT_ELAPSEDTIME).replace(":", ""), null);
        colPlayTimeInSeconds.setCellValueFactory(f -> {
            int days = f.getValue().getPlayTimeInSeconds() / 86400;
            int hours = (f.getValue().getPlayTimeInSeconds() % 86400) / 3600;
            int minutes = ((f.getValue().getPlayTimeInSeconds() % 86400) % 3600) / 60;
            return new SimpleStringProperty(String.format("%02d:%02d:%02d", days, hours, minutes));
        });

        setupTableColumnInteger(colNumberOfDeaths, txt.getString(Constants.UI.TAG_STAT_TOTALDEATHS).replace(":", ""), "numberOfDeaths");
        setupTableColumnInteger(colNumberOfKills, txt.getString(Constants.UI.TAG_STAT_MONSTERSKILLED).replace(":", ""), "numberOfKills");
        setupTableColumnInteger(colGreatestDamageInflicted, txt.getString(Constants.UI.TAG_STAT_GREATESTDAMAGE).replace(":", ""), "greatestDamageInflicted");
        setupTableColumnString(colGreatestMonsterKilled, txt.getString(Constants.UI.TAG_STAT_GREATESTMONSTER).replace(":", ""), null);

        colGreatestMonsterKilled.setCellValueFactory(f -> {
            if (f.getValue().getGreatestMonsterKilledName() != null) {
                String name = f.getValue().getGreatestMonsterKilledName().replaceAll("^\\{.*}", "");
                return new SimpleStringProperty(String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, name, f.getValue().getGreatestMonsterKilledLevel()));
            }
            return null;
        });

        setupTableColumnInteger(colExperienceFromKills, Util.getUIMessage("characters.experienceFromKills"), "experienceFromKills");
        setupTableColumnInteger(colHealthPotionsUsed, Util.getUIMessage("characters.healthPotionsUsed"), "healthPotionsUsed");
        setupTableColumnInteger(colManaPotionsUsed, Util.getUIMessage("characters.manaPotionsUsed"), "manaPotionsUsed");
        setupTableColumnInteger(colNumHitsReceived, Util.getUIMessage("characters.numHitsReceived"), "numHitsReceived");
        setupTableColumnInteger(colNumHitsInflicted, Util.getUIMessage("characters.numHitsInflicted"), "numHitsInflicted");
        setupTableColumnInteger(colCriticalHitsInflicted, Util.getUIMessage("characters.criticalHitsInflicted"), "criticalHitsInflicted");

        colLastTeleport.setCellValueFactory(f -> {
            MapTeleport mapTeleport = f.getValue().getLastMapTeleport();
            if (mapTeleport != null) {
                return new SimpleStringProperty(txt.getString(mapTeleport.getName()));
            }
            return null;
        });

        charactersTable.getItems().addAll(characters);

        resizeCharactersTable();
    }

    private void resizeCharactersTable() {
        for (TableColumn<?, ?> column : charactersTable.getColumns()) {
            double maxWidth = new Text(column.getText()).getLayoutBounds().getWidth();
            for (int i = 0; i < charactersTable.getItems().size(); i++) {
                if (column.getCellData(i) != null) {
                    Text text = new Text(column.getCellData(i).toString());
                    if (text.getLayoutBounds().getWidth() > maxWidth) {
                        maxWidth = text.getLayoutBounds().getWidth();
                    }
                }
            }
            column.setPrefWidth(15 + maxWidth);
        }
    }

    private void setupTableColumnInteger(TableColumn<PlayerCharacter, Integer> col, String name, String propertyName) {
        col.setText(name);
        if (propertyName != null) {
            col.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
        col.setCellFactory(new IntCellValueFactory<>());
        col.setPrefWidth(new Text(col.getText()).getLayoutBounds().getWidth() + 20);
    }

    private void setupTableColumnString(TableColumn<PlayerCharacter, String> col, String name, String propertyName) {
        col.setText(name);
        if (propertyName != null) {
            col.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }
        col.setPrefWidth(new Text(col.getText()).getLayoutBounds().getWidth() + 20);
    }

    public void exportCsv() {
        FileChooser csvChooser = new FileChooser();
        csvChooser.setTitle(Util.getUIMessage("characters.fileChooserTitle"));
        csvChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File csvFile = csvChooser.showSaveDialog(rootElement.getScene().getWindow());

        if (csvFile == null) {
            return;
        }

        List<String[]> csvRows = new ArrayList<>();

        String[] header = new String[]{
                Util.getUIMessage("characters.characterName"),
                Util.getUIMessage("main.charlevel"),
                Util.getUIMessage("main.gender"),
                Util.getUIMessage("main.charclass"),
                Util.getUIMessage("main.difficulty"),
                Util.getUIMessage("main.experience"),
                Util.getUIMessage("main.gold"),
                Util.getUIMessage("main.health"),
                Util.getUIMessage("main.energy"),
                Util.getUIMessage("main.strength"),
                Util.getUIMessage("main.intelligence"),
                Util.getUIMessage("main.dexterity"),
                Util.getUIMessage("characters.attributePoints"),
                Util.getUIMessage("characters.skillPoints"),
                Util.getUIMessage(Constants.Msg.CHARACTERS_TITLE_MASTERY),
                Util.getUIMessage(Constants.Msg.CHARACTERS_TITLE_MASTERY),
                Util.getUIMessage("characters.lastTeleport"),
                txt.getString(Constants.UI.TAG_STAT_ELAPSEDTIME),
                txt.getString(Constants.UI.TAG_STAT_TOTALDEATHS),
                txt.getString(Constants.UI.TAG_STAT_MONSTERSKILLED),
                txt.getString(Constants.UI.TAG_STAT_GREATESTDAMAGE),
                txt.getString(Constants.UI.TAG_STAT_GREATESTMONSTER),
                Util.getUIMessage("characters.experienceFromKills"),
                Util.getUIMessage("characters.healthPotionsUsed"),
                Util.getUIMessage("characters.manaPotionsUsed"),
                Util.getUIMessage("characters.numHitsReceived"),
                Util.getUIMessage("characters.numHitsInflicted"),
                Util.getUIMessage("characters.criticalHitsInflicted")
        };
        csvRows.add(header);

        for (PlayerCharacter p : characters) {
            String gender = Util.getUIMessage("main.gender." + p.getGender().name().toLowerCase());
            String difficultyText;
            String difficultyTextValue = String.format("%s%02d",
                    Constants.UI.PREFIXTAG_DIFFICULTYLABEL, p.getDifficulty() + 1);
            if (txt.isTagStringValid(difficultyTextValue)) {
                difficultyText = Util.cleanTagString(txt.getString(difficultyTextValue));
            } else {
                difficultyText = Util.getUIMessage(String.format("difficulty.%d", p.getDifficulty()));
            }

            String lastTeleport = "";
            MapTeleport mapTeleport = p.getLastMapTeleport();
            if (mapTeleport != null) {
                lastTeleport = txt.getString(mapTeleport.getName());
            }

            String masteryOne = "";
            String masteryTwo = "";
            if (p.getMasteries() != null && !p.getMasteries().isEmpty()) {
                Mastery mastery = p.getMasteries().get(0);
                String s = txt.getCapitalizedString(mastery.getDisplayName());
                masteryOne = String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, s, mastery.getLevel());
            }

            if (p.getMasteries() != null && p.getMasteries().size() > 1) {
                Mastery mastery = p.getMasteries().get(1);
                String s = txt.getCapitalizedString(mastery.getDisplayName());
                masteryTwo = String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, s, mastery.getLevel());
            }

            String greatestMonsterKilled = "";
            if (p.getGreatestMonsterKilledName() != null) {
                String name = p.getGreatestMonsterKilledName().replaceAll("^\\{.*}", "");
                greatestMonsterKilled = String.format(Constants.Msg.CHARACTERS_NAMENUMBER_FORMAT, name, p.getGreatestMonsterKilledLevel());
            }

            String[] row = new String[]{
                    p.getName(),
                    String.valueOf(p.getLevel()),
                    gender,
                    p.getCharacterClass(),
                    difficultyText,
                    String.valueOf(p.getExperience()),
                    String.valueOf(p.getGold()),
                    String.valueOf(p.getStatLife()),
                    String.valueOf(p.getStatMana()),
                    String.valueOf(p.getStatStr()),
                    String.valueOf(p.getStatInt()),
                    String.valueOf(p.getStatDex()),
                    String.valueOf(p.getStatAvailableAttrPoints()),
                    String.valueOf(p.getStatAvailableSkillPoints()),
                    masteryOne,
                    masteryTwo,
                    lastTeleport,
                    String.valueOf(p.getPlayTimeInSeconds()),
                    String.valueOf(p.getNumberOfDeaths()),
                    String.valueOf(p.getNumberOfKills()),
                    String.valueOf(p.getGreatestDamageInflicted()),
                    greatestMonsterKilled,
                    String.valueOf(p.getExperienceFromKills()),
                    String.valueOf(p.getHealthPotionsUsed()),
                    String.valueOf(p.getManaPotionsUsed()),
                    String.valueOf(p.getNumHitsReceived()),
                    String.valueOf(p.getNumHitsInflicted()),
                    String.valueOf(p.getCriticalHitsInflicted()),
            };
            csvRows.add(row);
        }

        try (PrintWriter writer = new PrintWriter(csvFile, StandardCharsets.UTF_8)) {
            csvRows.stream().map(r -> {
                List<String> fields = Arrays.stream(r).map(f -> {
                    String d = "";
                    if (f != null) {
                        d = f.replace("\r", "");
                        if (d.matches(".*[,\"']*.*")) {
                            d = d.replace("\"", "\"\"");
                            d = "\"" + d + "\"";
                        }
                    }
                    return d;
                }).collect(Collectors.toList());
                return String.join(",", fields);
            }).forEach(writer::println);
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, "Error saving csv file", e);
            throw new UnhandledRuntimeException("Error saving csv file", e);
        }
    }

    private static class IntCellValueFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
        private final DecimalFormat formatter;

        public IntCellValueFactory() {
            this.formatter = new DecimalFormat();
        }

        @Override
        public TableCell<S, T> call(TableColumn<S, T> stTableColumn) {
            return new TableCell<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        setText(formatter.format(item));
                    }
                }
            };
        }
    }
}

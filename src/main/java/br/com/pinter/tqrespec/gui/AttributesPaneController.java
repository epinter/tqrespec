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

import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.player.Gender;
import br.com.pinter.tqrespec.save.player.Player;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.System.Logger.Level.INFO;

@SuppressWarnings("unused")
public class AttributesPaneController implements Initializable {
    private static final System.Logger logger = Log.getLogger(AttributesPaneController.class);
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private MainController mc;

    private ObjectProperty<Integer> strProperty;
    private ObjectProperty<Integer> intProperty;
    private ObjectProperty<Integer> dexProperty;
    private ObjectProperty<Integer> lifeProperty;
    private ObjectProperty<Integer> manaProperty;
    private ObjectProperty<Integer> availProperty;
    private ObjectProperty<Integer> skillProperty;
    private ObjectProperty<Integer> charLevelProperty;
    private int strStep;
    private int strMin;
    private int intStep;
    private int intMin;
    private int dexStep;
    private int dexMin;
    private int lifeStep;
    private int lifeMin;
    private int manaStep;
    private int manaMin;
    private int attrPointsStep;
    private int skillPointsStep;
    private int maxLevel;
    private boolean characterIsLoading = false;

    @Inject
    private Db db;
    @Inject
    private Txt txt;
    @Inject
    private Player player;
    @Inject
    private UIUtils uiUtils;

    @FXML
    private Label strengthLabel;
    @FXML
    private Label intelligenceLabel;
    @FXML
    private Label dexterityLabel;
    @FXML
    private Label energyLabel;
    @FXML
    private Label healthLabel;
    @FXML
    private Label availPointsLabel;
    @FXML
    private Label experienceLabel;
    @FXML
    private Label charLevelLabel;
    @FXML
    private Label goldLabel;
    @FXML
    private Label charClassLabel;
    @FXML
    private Label difficultyLabel;
    @FXML
    private Spinner<Integer> strSpinner;
    @FXML
    private Spinner<Integer> intSpinner;
    @FXML
    private Spinner<Integer> dexSpinner;
    @FXML
    private Spinner<Integer> lifeSpinner;
    @FXML
    private Spinner<Integer> manaSpinner;
    @FXML
    private Spinner<Integer> skillSpinner;
    @FXML
    private Label skillLabel;
    @FXML
    private Label availPointsText;
    @FXML
    private Label experienceText;
    @FXML
    private Spinner<Integer> charLevelSpinner;
    @FXML
    private Label charClassText;
    @FXML
    private TextField goldText;
    @FXML
    private ComboBox<DifficultyItem> difficulty;
    @FXML
    private ComboBox<String> gender;
    @FXML
    private TextField electrumText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!State.get().getLocale().equals(Locale.ENGLISH) && resourceBundle.getLocale().getLanguage().isEmpty()) {
            ResourceHelper.tryTagText(txt, strengthLabel, Constants.UI.TAG_STRLABEL, true, false);
            ResourceHelper.tryTagText(txt, intelligenceLabel, Constants.UI.TAG_INTLABEL, true, false);
            ResourceHelper.tryTagText(txt, dexterityLabel, Constants.UI.TAG_DEXLABEL, true, false);
            ResourceHelper.tryTagText(txt, energyLabel, Constants.UI.TAG_ENERGYLABEL, true, false);
            ResourceHelper.tryTagText(txt, healthLabel, Constants.UI.TAG_HEALTHLABEL, true, false);
            ResourceHelper.tryTagText(txt, experienceLabel, Constants.UI.TAG_XPLABEL, true, false);
            ResourceHelper.tryTagText(txt, goldLabel, Constants.UI.TAG_GOLDLABEL, true, false);
            ResourceHelper.tryTagText(txt, charLevelLabel, Constants.UI.TAG_CHARLEVELLABEL, true, false);
            ResourceHelper.tryTagText(txt, charClassLabel, Constants.UI.TAG_CLASSLABEL, true, false);
            ResourceHelper.tryTagText(txt, difficultyLabel, Constants.UI.TAG_DIFFICULTYLABEL, true, false);
            ResourceHelper.tryTagText(txt, charClassLabel, Constants.UI.TAG_CLASSLABEL, true, false);
            ResourceHelper.tryTagText(txt, availPointsLabel, Constants.UI.TAG_AVAILPOINTSLABEL, true, true);
        }

        lifeSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipLifeSpinner")));
        manaSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipManaSpinner")));
        strSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipStrSpinner")));
        intSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipIntSpinner")));
        dexSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipDexSpinner")));
        skillSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltip.skillSpinner")));
        skillLabel.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltip.skillSpinner")));
        charLevelSpinner.setTooltip(uiUtils.simpleTooltip(ResourceHelper.getMessage("main.tooltipCharLevelSpinner")));

        goldText.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                int res = processNumberTextField(oldValue, newValue, goldText);
                if (res != Integer.MIN_VALUE) {
                    playerProps().setGold(res);
                }
            });
        });

        electrumText.textProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                int res = processNumberTextField(oldValue, newValue, electrumText);
                if (res != Integer.MIN_VALUE) {
                    playerProps().setElectrum(res);
                }
            });
        });

    }

    private int processNumberTextField(String oldValue, String newValue, TextField textField) {
        int res = Integer.MIN_VALUE;
        if (newValue.matches(".*\\D.*")) {
            String valueStr;
            int c = textField.getCaretPosition();
            try {
                res = Integer.parseInt(newValue.replaceAll("\\D", ""));
            } catch (NumberFormatException ignored) {
                try {
                    res = Integer.parseInt(oldValue.replaceAll("\\D", ""));
                } catch (NumberFormatException ignored2) {
                }
            }
            valueStr = String.valueOf(res);
            textField.setText(valueStr);
            textField.positionCaret(c - (newValue.length() - valueStr.length()));
        } else if (newValue.matches("^\\d+$")) {
            try {
                res = Integer.parseInt(newValue);
            } catch (NumberFormatException e) {
                String old = oldValue.replaceAll("\\D", "");
                textField.setText(old);
                textField.positionCaret(old.length());
            }
        }
        return res;
    }

    public void onMainInitialized() {
        mc.unlockedEditProperty().addListener((o, ov, nv) -> {
            goldText.setEditable(nv);
            goldText.setStyle("border: 1px solid");
            electrumText.setEditable(nv);
            electrumText.setStyle("border: 1px solid");
            charLevelSpinner.setDisable(!nv);
            skillSpinner.setDisable(!nv);
        });
    }

    private UiPlayerProperties playerProps() {
        return mc.playerProps();
    }

    public void windowShownHandler() {
        logger.log(INFO, "Font applied: name:''{0}'', family:''{1}''",
                healthLabel.getFont().getName(), healthLabel.getFont().getFamily());
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

    public void setSpinnersDisable(boolean disable) {
        lifeSpinner.setDisable(disable);
        manaSpinner.setDisable(disable);
        strSpinner.setDisable(disable);
        intSpinner.setDisable(disable);
        dexSpinner.setDisable(disable);
        if (mc.isUnlockedEdit()) {
            charLevelSpinner.setDisable(disable);
            skillSpinner.setDisable(disable);
        }
        if (mc.isCharacterSelected()) {
            charLevelSpinner.getStyleClass().add("charLevelSpinnerInUse");
            skillSpinner.getStyleClass().add("skillSpinnerInUse");
        } else {
            charLevelSpinner.getStyleClass().remove("charLevelSpinnerInUse");
            skillSpinner.getStyleClass().remove("skillSpinnerInUse");
        }
    }

    public void disableControls(boolean disable) {
        setSpinnersDisable(disable);
        gender.setDisable(disable);
        availPointsText.setDisable(disable);
        charClassText.setDisable(disable);
        difficulty.setDisable(disable);
        experienceText.setDisable(disable);
        goldText.setDisable(disable);
        electrumText.setDisable(disable);
    }

    private void setStrField(int value) {
        if (value < strMin && (value - strMin) % strStep == 0) {
            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - ((strMin - value) / strStep));
            playerProps().setStr(strMin);
        }
        strProperty = playerProps().strProperty().asObject();
        AttrIntegerSpinnerValueFactory strFactory = new AttrIntegerSpinnerValueFactory(
                strMin, playerProps().getStr(), playerProps().getStr(), strStep, playerProps().attrAvailableProperty());
        strSpinner.setValueFactory(strFactory);
        strSpinner.getValueFactory().valueProperty().bindBidirectional(strProperty);
        playerProps().strProperty().addListener(((observable, oldValue, newValue) ->
                attributesChanged((int) oldValue, (int) newValue, strStep, playerProps().strProperty())));
    }

    private void setIntField(int value) {
        if (value < intMin && (value - intMin) % intStep == 0) {
            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - ((intMin - value) / intStep));
            playerProps().setIntl(intMin);
        }
        intProperty = playerProps().intlProperty().asObject();
        AttrIntegerSpinnerValueFactory intFactory = new AttrIntegerSpinnerValueFactory
                (intMin, playerProps().getIntl(), playerProps().getIntl(), intStep, playerProps().attrAvailableProperty());
        intSpinner.setValueFactory(intFactory);
        intSpinner.getValueFactory().valueProperty().bindBidirectional(intProperty);
        playerProps().intlProperty().addListener(((observable, oldValue, newValue) ->
                attributesChanged((int) oldValue, (int) newValue, intStep, playerProps().intlProperty())));
    }

    private void setDexField(int value) {
        if (value < dexMin && (value - dexMin) % dexStep == 0) {
            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - ((dexMin - value) / dexStep));
            playerProps().setDex(dexMin);
        }
        dexProperty = playerProps().dexProperty().asObject();
        AttrIntegerSpinnerValueFactory dexFactory = new AttrIntegerSpinnerValueFactory(
                dexMin, playerProps().getDex(), playerProps().getDex(), dexStep, playerProps().attrAvailableProperty());
        dexSpinner.setValueFactory(dexFactory);
        dexSpinner.getValueFactory().valueProperty().bindBidirectional(dexProperty);
        playerProps().dexProperty().addListener(((observable, oldValue, newValue) ->
                attributesChanged((int) oldValue, (int) newValue, dexStep, playerProps().dexProperty())));
    }

    private void setLifeField(int value) {
        if (value < lifeMin && (value - lifeMin) % lifeStep == 0) {
            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - ((lifeMin - value) / lifeStep));
            playerProps().setLife(lifeMin);
        }
        lifeProperty = playerProps().lifeProperty().asObject();
        AttrIntegerSpinnerValueFactory lifeFactory = new AttrIntegerSpinnerValueFactory(
                lifeMin, playerProps().getLife(), playerProps().getLife(), lifeStep, playerProps().attrAvailableProperty());
        lifeSpinner.setValueFactory(lifeFactory);
        lifeSpinner.getValueFactory().valueProperty().bindBidirectional(lifeProperty);
        playerProps().lifeProperty().addListener(((observable, oldValue, newValue) ->
                attributesChanged((int) oldValue, (int) newValue, lifeStep, playerProps().lifeProperty())));
    }

    private void setManaField(int value) {
        if (value < manaMin && (value - manaMin) % manaStep == 0) {
            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - ((manaMin - value) / manaStep));
            playerProps().setMana(manaMin);
        }
        manaProperty = playerProps().manaProperty().asObject();
        AttrIntegerSpinnerValueFactory manaFactory = new AttrIntegerSpinnerValueFactory(
                manaMin, playerProps().getMana(), playerProps().getMana(), manaStep, playerProps().attrAvailableProperty());
        manaSpinner.setValueFactory(manaFactory);
        manaSpinner.getValueFactory().valueProperty().bindBidirectional(manaProperty);
        playerProps().manaProperty().addListener(((observable, oldValue, newValue) ->
                attributesChanged((int) oldValue, (int) newValue, manaStep, playerProps().manaProperty())));
    }

    private void setSkillField(int value) {
        skillProperty = playerProps().skillAvailableProperty().asObject();
        SkillIntegerSpinnerValueFactory skillFactory = new SkillIntegerSpinnerValueFactory(
                0, Integer.MAX_VALUE, playerProps().getSkillAvailable(), 1);
        skillSpinner.setValueFactory(skillFactory);
        skillSpinner.getValueFactory().valueProperty().bindBidirectional(skillProperty);
        playerProps().skillAvailableProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() > 0) {
                playerProps().setSkillAvailable(newValue.intValue());
            }
        }));
    }

    private void setCharLevelField(int value) {
        if (value < 1 || value > maxLevel) {
            return;
        }
        charLevelProperty = playerProps().charLevelProperty().asObject();
        LevelIntegerSpinnerValueFactory charLevelFactory = new LevelIntegerSpinnerValueFactory(
                1, maxLevel, playerProps().getAttrAvailable(), playerProps().attrAvailableProperty(), playerProps().skillAvailableProperty());
        charLevelSpinner.setValueFactory(charLevelFactory);
        charLevelSpinner.getValueFactory().valueProperty().bindBidirectional(charLevelProperty);
        playerProps().charLevelProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.intValue() > oldValue.intValue()) {
                        playerProps().setAttrAvailable(playerProps().getAttrAvailable() + attrPointsStep);
                        playerProps().setSkillAvailable(playerProps().getSkillAvailable() + skillPointsStep);
                    }

                    if (newValue.intValue() < oldValue.intValue()) {
                        if (mc.isFreeLvl() && (playerProps().getAttrAvailable() - attrPointsStep) >= 0 || !mc.isFreeLvl()) {
                            playerProps().setAttrAvailable(playerProps().getAttrAvailable() - attrPointsStep);
                        }
                        if (mc.isFreeLvl() && (playerProps().getSkillAvailable() - skillPointsStep) >= 0 || !mc.isFreeLvl()) {
                            playerProps().setSkillAvailable(playerProps().getSkillAvailable() - skillPointsStep);
                        }
                    }
                    playerProps().setCharLevel(newValue.intValue());
                    player.setAvailableSkillPoints(playerProps().getSkillAvailable());
                    player.setCharLevel(playerProps().getCharLevel());
                    experienceText.setText(String.valueOf(player.getXpLevelMin(playerProps().getCharLevel())));
                });
    }

    private void attributesChanged(int oldValue, int newValue, int step, IntegerProperty currentAttr) {
        if (mc.isFreeLvl()) {
            currentAttr.set(newValue);
        } else {
            if (newValue > oldValue && playerProps().getAttrAvailable() > 0) {
                int diff = newValue - oldValue;
                if (diff < step) return;
                currentAttr.set(newValue);
                playerProps().setAttrAvailable(playerProps().getAttrAvailable() - (diff / step));
            }

            if (newValue < oldValue) {
                int diff = oldValue - newValue;
                if (diff < step) return;
                currentAttr.set(newValue);
                playerProps().setAttrAvailable(playerProps().getAttrAvailable() + (diff / step));
            }
        }
        if (playerProps().getAttrAvailable() > 0) {
            saveDisabled.set(false);
        }
    }

    private void setAvailablePointsField(int value) {
        availPointsText.setText(String.valueOf(value));
        playerProps().setAttrAvailable(value);
        availPointsText.textProperty().bindBidirectional(playerProps().attrAvailableProperty(), new NumberStringConverter());
    }

    public void clearProperties() {
        if (playerProps() != null) {
            availPointsText.textProperty().unbindBidirectional(playerProps().attrAvailableProperty());
        }
        availPointsText.setText("");
        experienceText.setText("");
        goldText.setText("");
        electrumText.setText("");
        charClassText.setText("");
        if (difficulty.getSelectionModel() != null) {
            difficulty.getSelectionModel().clearSelection();
        }

        if (strSpinner.getValueFactory() != null && strSpinner.getValueFactory().valueProperty().isBound()) {
            strSpinner.getValueFactory().valueProperty().unbindBidirectional(strProperty);
        }
        if (intSpinner.getValueFactory() != null && intSpinner.getValueFactory().valueProperty().isBound()) {
            intSpinner.getValueFactory().valueProperty().unbindBidirectional(intProperty);
        }
        if (dexSpinner.getValueFactory() != null && dexSpinner.getValueFactory().valueProperty().isBound()) {
            dexSpinner.getValueFactory().valueProperty().unbindBidirectional(dexProperty);
        }
        if (lifeSpinner.getValueFactory() != null && lifeSpinner.getValueFactory().valueProperty().isBound()) {
            lifeSpinner.getValueFactory().valueProperty().unbindBidirectional(lifeProperty);
        }
        if (manaSpinner.getValueFactory() != null && manaSpinner.getValueFactory().valueProperty().isBound()) {
            manaSpinner.getValueFactory().valueProperty().unbindBidirectional(manaProperty);
        }
        if (skillSpinner.getValueFactory() != null && skillSpinner.getValueFactory().valueProperty().isBound()) {
            skillSpinner.getValueFactory().valueProperty().unbindBidirectional(skillProperty);
        }
        if (charLevelSpinner.getValueFactory() != null && charLevelSpinner.getValueFactory().valueProperty().isBound()) {
            charLevelSpinner.getValueFactory().valueProperty().unbindBidirectional(charLevelProperty);
        }
        if (!characterIsLoading) {
            strSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            intSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            dexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            lifeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            manaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            skillSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            charLevelSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            strSpinner.getValueFactory().setValue(0);
            intSpinner.getValueFactory().setValue(0);
            dexSpinner.getValueFactory().setValue(0);
            lifeSpinner.getValueFactory().setValue(0);
            manaSpinner.getValueFactory().setValue(0);
            skillSpinner.getValueFactory().setValue(0);
            charLevelSpinner.getValueFactory().setValue(0);
        }
        strSpinner.setValueFactory(null);
        intSpinner.setValueFactory(null);
        dexSpinner.setValueFactory(null);
        lifeSpinner.setValueFactory(null);
        manaSpinner.setValueFactory(null);
        skillSpinner.setValueFactory(null);
        charLevelSpinner.setValueFactory(null);
        gender.getSelectionModel().clearSelection();
    }

    public void loadCharHandler() {
        characterIsLoading = true;
        strStep = db.player().getPlayerLevels().getStrengthIncrement();
        strMin = Math.round(db.player().getPc().getCharacterStrength());
        intStep = db.player().getPlayerLevels().getIntelligenceIncrement();
        intMin = Math.round(db.player().getPc().getCharacterIntelligence());
        dexStep = db.player().getPlayerLevels().getDexterityIncrement();
        dexMin = Math.round(db.player().getPc().getCharacterDexterity());
        lifeStep = db.player().getPlayerLevels().getLifeIncrement();
        lifeMin = Math.round(db.player().getPc().getCharacterLife());
        manaStep = db.player().getPlayerLevels().getManaIncrement();
        manaMin = Math.round(db.player().getPc().getCharacterMana());
        attrPointsStep = db.player().getPlayerLevels().getCharacterModifierPoints();
        skillPointsStep = db.player().getPlayerLevels().getSkillModifierPoints();
        maxLevel = db.player().getPlayerLevels().getMaxPlayerLevel();

        clearProperties();
        if (playerProps().getAttrAvailable() < 0 || playerProps().getStr() < 0 || playerProps().getDex() < 0
                || playerProps().getIntl() < 0 || playerProps().getLife() < 0 || playerProps().getMana() < 0) {
            uiUtils.showError(ResourceHelper.getMessage("alert.errorloadingchar_header"),
                    ResourceHelper.getMessage("alert.errorloadingchar_content",
                            playerProps().getLife(), playerProps().getMana(), playerProps().getStr(), playerProps().getIntl(), playerProps().getDex()));
            return;
        }
        setAvailablePointsField(playerProps().getAttrAvailable());
        setStrField(playerProps().getStr());
        setIntField(playerProps().getIntl());
        setDexField(playerProps().getDex());
        setLifeField(playerProps().getLife());
        setManaField(playerProps().getMana());
        setSkillField(playerProps().getSkillAvailable());

        charClassText.setText(player.getPlayerClassName());

        List<DifficultyItem> difficultyItems = new ArrayList<>();
        //load difficulty list from game, the tag id starts with 1, while the difficulty id in save game starts with 0
        for (int i = 0; txt.getString(String.format("%s%02d", Constants.UI.PREFIXTAG_DIFFICULTYLABEL, i + 1)) != null; i++) {
            difficultyItems.add(new DifficultyItem(
                    i, ResourceHelper.cleanTagString(txt.getString(String.format("%s%02d", Constants.UI.PREFIXTAG_DIFFICULTYLABEL, i + 1)))
            ));
        }
        if (difficultyItems.isEmpty()) {
            for (int i = 0; i <= 2; i++) {
                difficultyItems.add(new DifficultyItem(
                        i, ResourceHelper.getMessage(String.format("difficulty.%d", i))
                ));
            }
        }

        difficulty.getItems().setAll(difficultyItems);
        difficulty.getSelectionModel().select(player.getDifficulty());
        difficulty.getItems().sort(Comparator.comparing(DifficultyItem::getId));

        electrumText.setText(String.valueOf(playerProps().getElectrum()));
        experienceText.setText(NumberFormat.getInstance(State.get().getLocale()).format(playerProps().getXp()));
        setCharLevelField(playerProps().getCharLevel());
        goldText.setText(String.valueOf(playerProps().getGold()));

        gender.getItems().setAll(ResourceHelper.getMessage("main.gender.male"), ResourceHelper.getMessage("main.gender.female"));
        int genderSelection;
        if (player.getGender().equals(Gender.FEMALE)) {
            genderSelection = 1;
        } else {
            genderSelection = 0;
        }

        gender.getSelectionModel().clearAndSelect(genderSelection);

        gender.getSelectionModel().selectedIndexProperty().addListener((o, oldValue, newValue) -> {
            if (oldValue.intValue() >= 0 && newValue.intValue() >= 0) {
                Platform.runLater(() -> Toast.show((Stage) gender.getScene().getWindow(),
                        ResourceHelper.getMessage("alert.genderchange_header"),
                        ResourceHelper.getMessage("alert.genderchange_content"),
                        4000));
            }
        });

        characterIsLoading = false;
    }

    @FXML
    public void genderSelect(ActionEvent e) {
        if (characterIsLoading) {
            return;
        }

        int selected = gender.getSelectionModel().getSelectedIndex();
        if (selected == 0) {
            player.setGender(Gender.MALE);
        } else if (selected == 1) {
            player.setGender(Gender.FEMALE);
        }
    }

    @FXML
    public void difficultySelect(ActionEvent e) {
        if (characterIsLoading) {
            return;
        }

        if (difficulty.getSelectionModel().getSelectedItem() != null) {
            playerProps().setDifficulty(difficulty.getSelectionModel().getSelectedItem().getId());
        }
    }

    public void setMainController(MainController mainController) {
        this.mc = mainController;
    }

    class SkillIntegerSpinnerValueFactory extends SpinnerValueFactory.IntegerSpinnerValueFactory {
        SkillIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy) {
            super(min, max, initialValue, amountToStepBy);
        }

        @Override
        public void decrement(int v) {
            int oldValue = getValue();
            int step = v * getAmountToStepBy();
            int newValue = oldValue - step;
            if (newValue >= getMin() && mc.isUnlockedEdit()) {
                setValue(newValue);
            }
        }

        @Override
        public void increment(int v) {
            int oldValue = getValue();
            int step = v * getAmountToStepBy();
            int newValue = oldValue + step;
            if (newValue <= getMax() && mc.isUnlockedEdit()) {
                setValue(newValue);
            }
        }
    }

    class AttrIntegerSpinnerValueFactory extends SpinnerValueFactory.IntegerSpinnerValueFactory {
        private final IntegerProperty available;

        AttrIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy, IntegerProperty available) {
            super(min, max, initialValue, amountToStepBy);
            this.available = available;
        }

        @Override
        public void decrement(int v) {
            int oldValue = getValue();
            int step = v * getAmountToStepBy();
            int newValue = oldValue - step;
            if (newValue >= getMin()) {
                setValue(newValue);
            }
        }

        @Override
        public void increment(int v) {
            int oldValue = getValue();
            int step = v * getAmountToStepBy();
            int newValue = oldValue + step;
            int pointsAvail = available.get() * step;
            if (oldValue == getMax() && available.get() <= 0 && !mc.isFreeLvl()) {
                return;
            }
            if (mc.isFreeLvl()) {
                setMax(Integer.MAX_VALUE);
            } else {
                setMax(oldValue + pointsAvail);
            }
            if (newValue <= getMax() || mc.isFreeLvl())
                setValue(newValue);
        }
    }

    class LevelIntegerSpinnerValueFactory extends SpinnerValueFactory.IntegerSpinnerValueFactory {
        private final IntegerProperty attrAvailable;
        private final IntegerProperty skillAvailable;

        LevelIntegerSpinnerValueFactory(int min, int max, int initialValue, IntegerProperty attrAvailable, IntegerProperty skillAvailable) {
            super(min, max, initialValue, 1);
            this.attrAvailable = attrAvailable;
            this.skillAvailable = skillAvailable;
        }

        @Override
        public void decrement(int v) {
            int oldValue = this.getValue();
            int step = v * this.getAmountToStepBy();
            int newValue = oldValue - step;

            if (newValue >= this.getMin() && ((!mc.isFreeLvl() && attrAvailable.get() >= attrPointsStep && skillAvailable.get() >= skillPointsStep)
                    || mc.isFreeLvl())) {
                setValue(newValue);
            }
        }

        @Override
        public void increment(int v) {
            int oldValue = this.getValue();
            int step = v * this.getAmountToStepBy();
            int newValue = oldValue + step;
            if (newValue <= this.getMax()) {
                setValue(newValue);
            }
        }
    }
}
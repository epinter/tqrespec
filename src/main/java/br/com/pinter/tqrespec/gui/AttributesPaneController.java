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
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;

@SuppressWarnings("unused")
public class AttributesPaneController implements Initializable {
    private static final System.Logger logger = Log.getLogger(AttributesPaneController.class);
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
    private MainController mc;
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
    @Inject
    private Db db;
    @Inject
    private Txt txt;
    @Inject
    private Player player;
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
    private Label availPointsText;
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
    private ComboBox<String> gender;
    @Inject
    private UIUtils uiUtils;

    private ObjectProperty<Integer> strProperty;
    private ObjectProperty<Integer> intProperty;
    private ObjectProperty<Integer> dexProperty;
    private ObjectProperty<Integer> lifeProperty;
    private ObjectProperty<Integer> manaProperty;
    private ObjectProperty<Integer> availProperty;
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
    private boolean characterIsLoading = false;

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
    }

    private UiPlayerProperties playerProps() {
        return mc.getPlayerProperties();
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
    }

    public void disableControls(boolean disable) {
        setSpinnersDisable(disable);
        gender.setDisable(disable);
        availPointsText.setDisable(disable);
        charClassText.setDisable(disable);
        difficultyText.setDisable(disable);
        experienceText.setDisable(disable);
        charLevelText.setDisable(disable);
        goldText.setDisable(disable);
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

    private void attributesChanged(int oldValue, int newValue, int step, IntegerProperty currentAttr) {
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
        charLevelText.setText("");
        goldText.setText("");
        charClassText.setText("");
        difficultyText.setText("");

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
        if (!characterIsLoading) {
            strSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            intSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            dexSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            lifeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            manaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0));
            strSpinner.getValueFactory().setValue(0);
            intSpinner.getValueFactory().setValue(0);
            dexSpinner.getValueFactory().setValue(0);
            lifeSpinner.getValueFactory().setValue(0);
            manaSpinner.getValueFactory().setValue(0);
        }
        strSpinner.setValueFactory(null);
        intSpinner.setValueFactory(null);
        dexSpinner.setValueFactory(null);
        lifeSpinner.setValueFactory(null);
        manaSpinner.setValueFactory(null);
        gender.getSelectionModel().clearSelection();
    }

    public void commitChanges() {
        int strOld = player.getStr();
        int intOld = player.getInt();
        int dexOld = player.getDex();
        int lifeOld = player.getLife();
        int manaOld = player.getMana();
        int modifierOld = player.getModifierPoints();

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
    }

    public void saveCharHandler() {
        logger.log(DEBUG, "starting savegame task");

        commitChanges();

        logger.log(DEBUG, "returning savegame task");
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

        charClassText.setText(player.getPlayerClassName());

        String difficultyTextValue = String.format("%s%02d", Constants.UI.PREFIXTAG_DIFFICULTYLABEL, playerProps().getDifficulty() + 1);
        if (txt.isTagStringValid(difficultyTextValue)) {
            difficultyText.setText(ResourceHelper.cleanTagString(txt.getString(difficultyTextValue)));
        } else {
            difficultyText.setText(ResourceHelper.getMessage(String.format("difficulty.%d", playerProps().getDifficulty())));
        }

        experienceText.setText(NumberFormat.getInstance().format(playerProps().getXp()));
        charLevelText.setText(String.valueOf(playerProps().getCharLevel()));
        goldText.setText(NumberFormat.getInstance().format(playerProps().getGold()));

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

    public void setMainController(MainController mainController) {
        this.mc = mainController;
    }
}

@SuppressWarnings("CanBeFinal")
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
        if (oldValue == getMax() && available.get() <= 0) {
            return;
        }
        setMax(oldValue + pointsAvail);
        if (newValue <= getMax())
            setValue(newValue);
    }
}

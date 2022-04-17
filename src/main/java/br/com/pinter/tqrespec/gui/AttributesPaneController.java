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
import javafx.beans.property.*;
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

@SuppressWarnings("unused")
public class AttributesPaneController implements Initializable {
    private static final System.Logger logger = Log.getLogger(AttributesPaneController.class.getName());
    private final BooleanProperty saveDisabled = new SimpleBooleanProperty();
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

    private IntegerProperty currentStr = new SimpleIntegerProperty();
    private IntegerProperty currentInt = new SimpleIntegerProperty();
    private IntegerProperty currentDex = new SimpleIntegerProperty();
    private IntegerProperty currentLife = new SimpleIntegerProperty();
    private IntegerProperty currentMana = new SimpleIntegerProperty();
    private IntegerProperty currentAvail = new SimpleIntegerProperty();
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

    public int getCurrentAvail() {
        return currentAvail.get();
    }

    public IntegerProperty currentAvailProperty() {
        return currentAvail;
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
            currentAvail.set(currentAvail.get() - ((strMin - value) / strStep));
            value = strMin;
        }
        currentStr = new SimpleIntegerProperty(value);
        strProperty = currentStr.asObject();
        AttrIntegerSpinnerValueFactory strFactory = new AttrIntegerSpinnerValueFactory(strMin, currentStr.get(), currentStr.get(), strStep, currentAvail);
        strSpinner.setValueFactory(strFactory);
        strSpinner.getValueFactory().valueProperty().bindBidirectional(strProperty);
        currentStr.addListener(((observable, oldValue, newValue) -> attributesChanged((int) oldValue, (int) newValue, strStep, currentStr)));
    }

    private void setIntField(int value) {
        if (value < intMin && (value - intMin) % intStep == 0) {
            currentAvail.set(currentAvail.get() - ((intMin - value) / intStep));
            value = intMin;
        }
        currentInt = new SimpleIntegerProperty(value);
        intProperty = currentInt.asObject();
        AttrIntegerSpinnerValueFactory intFactory = new AttrIntegerSpinnerValueFactory(intMin, currentInt.get(), currentInt.get(), intStep, currentAvail);
        intSpinner.setValueFactory(intFactory);
        intSpinner.getValueFactory().valueProperty().bindBidirectional(intProperty);
        currentInt.addListener(((observable, oldValue, newValue) -> attributesChanged((int) oldValue, (int) newValue, intStep, currentInt)));
    }

    private void setDexField(int value) {
        if (value < dexMin && (value - dexMin) % dexStep == 0) {
            currentAvail.set(currentAvail.get() - ((dexMin - value) / dexStep));
            value = dexMin;
        }
        currentDex = new SimpleIntegerProperty(value);
        dexProperty = currentDex.asObject();
        AttrIntegerSpinnerValueFactory dexFactory = new AttrIntegerSpinnerValueFactory(dexMin, currentDex.get(), currentDex.get(), dexStep, currentAvail);
        dexSpinner.setValueFactory(dexFactory);
        dexSpinner.getValueFactory().valueProperty().bindBidirectional(dexProperty);
        currentDex.addListener(((observable, oldValue, newValue) -> attributesChanged((int) oldValue, (int) newValue, dexStep, currentDex)));
    }

    private void setLifeField(int value) {
        if (value < lifeMin && (value - lifeMin) % lifeStep == 0) {
            currentAvail.set(currentAvail.get() - ((lifeMin - value) / lifeStep));
            value = lifeMin;
        }
        currentLife = new SimpleIntegerProperty(value);
        lifeProperty = currentLife.asObject();
        AttrIntegerSpinnerValueFactory lifeFactory = new AttrIntegerSpinnerValueFactory(lifeMin, currentLife.get(), currentLife.get(), lifeStep, currentAvail);
        lifeSpinner.setValueFactory(lifeFactory);
        lifeSpinner.getValueFactory().valueProperty().bindBidirectional(lifeProperty);
        currentLife.addListener(((observable, oldValue, newValue) -> attributesChanged((int) oldValue, (int) newValue, lifeStep, currentLife)));
    }

    private void setManaField(int value) {
        if (value < manaMin && (value - manaMin) % manaStep == 0) {
            currentAvail.set(currentAvail.get() - ((manaMin - value) / manaStep));
            value = manaMin;
        }
        currentMana = new SimpleIntegerProperty(value);
        manaProperty = currentMana.asObject();
        AttrIntegerSpinnerValueFactory manaFactory = new AttrIntegerSpinnerValueFactory(manaMin, currentMana.get(), currentMana.get(), manaStep, currentAvail);
        manaSpinner.setValueFactory(manaFactory);
        manaSpinner.getValueFactory().valueProperty().bindBidirectional(manaProperty);
        currentMana.addListener(((observable, oldValue, newValue) -> attributesChanged((int) oldValue, (int) newValue, manaStep, currentMana)));
    }

    private void attributesChanged(int oldValue, int newValue, int step, IntegerProperty currentAttr) {
        if (newValue > oldValue && currentAvail.get() > 0) {
            int diff = newValue - oldValue;
            if (diff < step) return;
            currentAttr.set(newValue);
            currentAvail.set(currentAvail.get() - (diff / step));
        }

        if (newValue < oldValue) {
            int diff = oldValue - newValue;
            if (diff < step) return;
            currentAttr.set(newValue);
            currentAvail.set(currentAvail.get() + (diff / step));
        }
        if (currentAvail.get() > 0) {
            saveDisabled.set(false);
        }
    }

    private void setAvailablePointsField(int value) {
        this.availPointsText.setText(String.valueOf(value));
        currentAvail = new SimpleIntegerProperty(value);
        availPointsText.textProperty().bindBidirectional(currentAvail, new NumberStringConverter());
    }

    public void clearProperties() {
        availPointsText.textProperty().unbindBidirectional(currentAvail);
        currentAvail.setValue(null);
        currentStr.setValue(null);
        currentInt.setValue(null);
        currentDex.setValue(null);
        currentLife.setValue(null);
        currentMana.setValue(null);
        availPointsText.setText("");
        experienceText.setText("");
        charLevelText.setText("");
        goldText.setText("");
        charClassText.setText("");
        difficultyText.setText("");
        if (strSpinner.getValueFactory() != null && strSpinner.getValueFactory().valueProperty().isBound())
            strSpinner.getValueFactory().valueProperty().unbindBidirectional(currentStr.asObject());
        if (intSpinner.getValueFactory() != null && intSpinner.getValueFactory().valueProperty().isBound())
            intSpinner.getValueFactory().valueProperty().unbindBidirectional(currentInt.asObject());
        if (dexSpinner.getValueFactory() != null && dexSpinner.getValueFactory().valueProperty().isBound())
            dexSpinner.getValueFactory().valueProperty().unbindBidirectional(currentDex.asObject());
        if (lifeSpinner.getValueFactory() != null && lifeSpinner.getValueFactory().valueProperty().isBound())
            lifeSpinner.getValueFactory().valueProperty().unbindBidirectional(currentLife.asObject());
        if (manaSpinner.getValueFactory() != null && manaSpinner.getValueFactory().valueProperty().isBound())
            manaSpinner.getValueFactory().valueProperty().unbindBidirectional(currentMana.asObject());
        strSpinner.setValueFactory(null);
        intSpinner.setValueFactory(null);
        dexSpinner.setValueFactory(null);
        lifeSpinner.setValueFactory(null);
        manaSpinner.setValueFactory(null);
        gender.getSelectionModel().clearSelection();
    }

    public void saveCharHandler() {
        logger.log(System.Logger.Level.DEBUG, "starting savegame task");

        int strOld = player.getStr();
        int intOld = player.getInt();
        int dexOld = player.getDex();
        int lifeOld = player.getLife();
        int manaOld = player.getMana();
        int modifierOld = player.getModifierPoints();

        if (strOld != currentStr.get() && currentStr.get() > 0) {
            player.setStr(currentStr.get());
        }
        if (intOld != currentInt.get() && currentInt.get() > 0) {
            player.setInt(currentInt.get());
        }
        if (dexOld != currentDex.get() && currentDex.get() > 0) {
            player.setDex(currentDex.get());
        }
        if (lifeOld != currentLife.get() && currentLife.get() > 0) {
            player.setLife(currentLife.get());
        }
        if (manaOld != currentMana.get() && currentMana.get() > 0) {
            player.setMana(currentMana.get());
        }
        if (modifierOld != currentAvail.get() && currentAvail.get() >= 0) {
            player.setModifierPoints(currentAvail.get());
        }
        logger.log(System.Logger.Level.DEBUG, "returning savegame task");
    }

    public void loadCharHandler() {
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
        int str = player.getStr();
        int inl = player.getInt();
        int dex = player.getDex();
        int life = player.getLife();
        int mana = player.getMana();
        int modifier = player.getModifierPoints();
        if (modifier < 0 || str < 0 || dex < 0 || inl < 0 || life < 0 || mana < 0) {
            uiUtils.showError(ResourceHelper.getMessage("alert.errorloadingchar_header"),
                    ResourceHelper.getMessage("alert.errorloadingchar_content", life, mana, str, inl, dex));
            clearProperties();
            return;
        }
        this.setAvailablePointsField(modifier);
        this.setStrField(str);
        this.setIntField(inl);
        this.setDexField(dex);
        this.setLifeField(life);
        this.setManaField(mana);
        int xp = player.getXp();
        int level = player.getLevel();
        int gold = player.getMoney();
        charClassText.setText(player.getPlayerClassName());
        int difficulty = player.getDifficulty();

        String difficultyTextValue = String.format("%s%02d", Constants.UI.PREFIXTAG_DIFFICULTYLABEL, difficulty + 1);
        if (txt.isTagStringValid(difficultyTextValue)) {
            difficultyText.setText(ResourceHelper.cleanTagString(txt.getString(difficultyTextValue)));
        } else {
            difficultyText.setText(ResourceHelper.getMessage(String.format("difficulty.%d", difficulty)));
        }

        experienceText.setText(NumberFormat.getInstance().format(xp));
        charLevelText.setText(String.valueOf(level));
        goldText.setText(NumberFormat.getInstance().format(gold));

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
    }

    @FXML
    public void genderSelect(ActionEvent e) {
        int selected = gender.getSelectionModel().getSelectedIndex();
        if (selected == 0) {
            player.setGender(Gender.MALE);
        } else if (selected == 1) {
            player.setGender(Gender.FEMALE);
        }
    }
}

@SuppressWarnings("CanBeFinal")
class AttrIntegerSpinnerValueFactory extends SpinnerValueFactory.IntegerSpinnerValueFactory {
    private final IntegerProperty available;
    private int originalMax = -1;

    AttrIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy, IntegerProperty available) {
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

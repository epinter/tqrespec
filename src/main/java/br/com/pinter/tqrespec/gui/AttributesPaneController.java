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

import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

public class AttributesPaneController implements Initializable {
    private static final boolean DBG = false;
    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private PlayerData playerData;

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
    private Label experienceText;

    @FXML
    private Label charLevelText;

    @FXML
    private Label charClassText;

    @FXML
    private Label goldText;

    @FXML
    private Label difficultyText;

    private SimpleIntegerProperty currentStr = null;
    private SimpleIntegerProperty currentInt = null;
    private SimpleIntegerProperty currentDex = null;
    private SimpleIntegerProperty currentLife = null;
    private SimpleIntegerProperty currentMana = null;
    private SimpleIntegerProperty currentAvail = null;

    private BooleanProperty saveDisabled = new SimpleBooleanProperty();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public boolean isSaveDisabled() {
        return saveDisabled.get();
    }

    public BooleanProperty saveDisabledProperty() {
        return saveDisabled;
    }

    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled.set(saveDisabled);
    }

    public int getCurrentAvail() {
        return currentAvail.get();
    }

    public SimpleIntegerProperty currentAvailProperty() {
        return currentAvail;
    }

    public void setSpinnersDisable(boolean disable) {
        lifeSpinner.setDisable(disable);
        manaSpinner.setDisable(disable);
        strSpinner.setDisable(disable);
        intSpinner.setDisable(disable);
        dexSpinner.setDisable(disable);
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
        if (currentAvail != null)
            currentAvail.setValue(null);
        if (currentStr != null)
            currentStr.setValue(null);
        if (currentInt != null)
            currentInt.setValue(null);
        if (currentDex != null)
            currentDex.setValue(null);
        if (currentLife != null)
            currentLife.setValue(null);
        if (currentMana != null)
            currentMana.setValue(null);
        if (experienceText != null)
            experienceText.setText("");
        if (charLevelText != null)
            charLevelText.setText("");
        if (goldText != null)
            goldText.setText("");
        if (charClassText != null)
            charClassText.setText("");
        if (difficultyText != null)
            difficultyText.setText("");
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
    }

    public void saveCharHandler() throws Exception {
        if (DBG) System.out.println("starting savegame task");

        int strOld = playerData.getStr();
        int intOld = playerData.getInt();
        int dexOld = playerData.getDex();
        int lifeOld = playerData.getLife();
        int manaOld = playerData.getMana();
        int modifierOld = playerData.getModifierPoints();

        if (strOld != currentStr.get() && currentStr.get() > 0) {
            playerData.setStr(currentStr.get());
        }
        if (intOld != currentInt.get() && currentInt.get() > 0) {
            playerData.setInt(currentInt.get());
        }
        if (dexOld != currentDex.get() && currentDex.get() > 0) {
            playerData.setDex(currentDex.get());
        }
        if (lifeOld != currentLife.get() && currentLife.get() > 0) {
            playerData.setLife(currentLife.get());
        }
        if (manaOld != currentMana.get() && currentMana.get() > 0) {
            playerData.setMana(currentMana.get());
        }
        if (modifierOld != currentAvail.get() && currentAvail.get() > 0) {
            playerData.setModifierPoints(currentAvail.get());
        }
        if (DBG) System.out.println("returning savegame task");
    }

    public void loadCharHandler() {
        clearProperties();
        int str = playerData.getStr();
        int inl = playerData.getInt();
        int dex = playerData.getDex();
        int life = playerData.getLife();
        int mana = playerData.getMana();
        int modifier = playerData.getModifierPoints();
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
        int xp = playerData.getXp();
        int level = playerData.getLevel();
        int gold = playerData.getMoney();
        charClassText.setText(playerData.getPlayerClassName());
        int difficulty = playerData.getDifficulty();
        difficultyText.setText(Util.getUIMessage(String.format("difficulty.%d", difficulty)));
        experienceText.setText(NumberFormat.getInstance().format(xp));
        charLevelText.setText(String.valueOf(level));
        goldText.setText(NumberFormat.getInstance().format(gold));

    }
}

@SuppressWarnings("CanBeFinal")
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

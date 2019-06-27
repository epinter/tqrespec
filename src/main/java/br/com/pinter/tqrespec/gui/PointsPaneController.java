/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.Constants;
import br.com.pinter.tqrespec.Util;
import br.com.pinter.tqrespec.save.PlayerData;
import br.com.pinter.tqrespec.tqdata.Data;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;

public class PointsPaneController implements Initializable {
    private static final boolean DBG = false;
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

    public void saveCharHandler() throws Exception {
        if (DBG) System.out.println("starting savegame task");

        int strOld = Math.round(playerData.getChanges().getFloat("str"));
        int intOld = Math.round(playerData.getChanges().getFloat("int"));
        int dexOld = Math.round(playerData.getChanges().getFloat("dex"));
        int lifeOld = Math.round(playerData.getChanges().getFloat("life"));
        int manaOld = Math.round(playerData.getChanges().getFloat("mana"));
        int modifierOld = playerData.getChanges().getInt("modifierPoints");

        if (strOld != currentStr.get() && currentStr.get() > 0) {
            playerData.getChanges().setFloat("str", currentStr.get());
        }
        if (intOld != currentInt.get() && currentInt.get() > 0) {
            playerData.getChanges().setFloat("int", currentInt.get());
        }
        if (dexOld != currentDex.get() && currentDex.get() > 0) {
            playerData.getChanges().setFloat("dex", currentDex.get());
        }
        if (lifeOld != currentLife.get() && currentLife.get() > 0) {
            playerData.getChanges().setFloat("life", currentLife.get());
        }
        if (manaOld != currentMana.get() && currentMana.get() > 0) {
            playerData.getChanges().setFloat("mana", currentMana.get());
        }
        if (modifierOld != currentAvail.get() && currentAvail.get() > 0) {
            playerData.getChanges().setInt("modifierPoints", currentAvail.get());
        }
        if (DBG) System.out.println("returning savegame task");
    }

    public void loadCharHandler() {
        clearProperties();
        int str = Math.round(playerData.getChanges().getFloat("str"));
        int inl = Math.round(playerData.getChanges().getFloat("int"));
        int dex = Math.round(playerData.getChanges().getFloat("dex"));
        int life = Math.round(playerData.getChanges().getFloat("life"));
        int mana = Math.round(playerData.getChanges().getFloat("mana"));
        int modifier = playerData.getChanges().getInt("modifierPoints");
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
        int xp = playerData.getChanges().getInt("currentStats.experiencePoints");
        int level = playerData.getChanges().getInt("currentStats.charLevel");
        int gold = playerData.getChanges().getInt("money");
        String charClass = playerData.getPlayerClassTag();

        if (StringUtils.isNotEmpty(charClass)) {
            charClassText.setText(Data.text().getString(charClass));
        }
        int difficulty = playerData.getChanges().getInt("difficulty");
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

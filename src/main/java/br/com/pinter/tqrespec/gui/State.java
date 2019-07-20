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

import javafx.beans.property.SimpleBooleanProperty;

public class State {
    private static final Object lock = new Object();
    private static State instance = null;
    private SimpleBooleanProperty saveInProgress = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty gameRunning = new SimpleBooleanProperty(false);

    public static State get() {
        State c = instance;
        if (c == null) {
            synchronized (lock) {
                c = instance;
                if (c == null) {
                    c = new State();
                    instance = c;
                }
            }
        }
        return instance;
    }

    private State() {
    }

    public Boolean getSaveInProgress() {
        return saveInProgress.getValue();
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress.setValue(saveInProgress);
    }

    public Boolean getGameRunning() {
        return gameRunning.getValue();
    }

    public void setGameRunning(Boolean gameRunning) {
        this.gameRunning.setValue(gameRunning);
    }

    public SimpleBooleanProperty saveInProgressProperty() {
        return saveInProgress;
    }

    public SimpleBooleanProperty gameRunningProperty() {
        return gameRunning;
    }
}

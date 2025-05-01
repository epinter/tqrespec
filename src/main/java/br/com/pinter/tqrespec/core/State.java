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

package br.com.pinter.tqrespec.core;

import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class State {
    private static final Object lock = new Object();
    private static State instance = null;
    private final SimpleBooleanProperty saveInProgress = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty gameRunning = new SimpleBooleanProperty(false);
    private final Map<String, Level> debugPrefix = new HashMap<>();
    private final AtomicReference<MyTask<Integer>> lastCursorWaitTask = new AtomicReference<>();
    private final AtomicBoolean gameFontFound = new AtomicBoolean(false);
    private Locale locale = Locale.of("en");

    private State() {
    }

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

    public boolean isSaveInProgress() {
        return saveInProgress.getValue();
    }

    public void setSaveInProgress(Boolean saveInProgress) {
        this.saveInProgress.setValue(saveInProgress);
    }

    public boolean isGameRunning() {
        return gameRunning.getValue();
    }

    public void setGameRunning(Boolean gameRunning) {
        this.gameRunning.setValue(gameRunning);
    }

    public SimpleBooleanProperty gameRunningProperty() {
        return gameRunning;
    }

    public Map<String, Level> getDebugPrefix() {
        return debugPrefix;
    }

    public void addDebugPrefix(String prefix, Level level) {
        getDebugPrefix().put(prefix, level);
    }

    public Locale getLocale() {
        if (locale == null) {
            locale = Locale.of("en");
        }
        return locale;
    }

    public boolean isLocaleLatin() {
        if (locale == null) {
            return true;
        }
        return !StringUtils.equalsAnyIgnoreCase(locale.getLanguage(), "zh", "ja", "ko", "pl", "ru", "uk");
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public MyTask<Integer> getLastCursorWaitTask() {
        return lastCursorWaitTask.get();
    }

    public void setLastCursorWaitTask(MyTask<Integer> lastCursorWaitTask) {
        this.lastCursorWaitTask.set(lastCursorWaitTask);
    }

    public boolean isGameFontFound() {
        return gameFontFound.get();
    }

    public void setGameFontFound(boolean gameFontFound) {
        this.gameFontFound.set(gameFontFound);
    }
}

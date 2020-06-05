/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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

package br.com.pinter.tqrespec;

import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public class Settings {

    enum Options {
        LAST_DETECTED_GAMEPATH("last_detectedgamepath"),
        ALWAYS_FULL_BACKUP("always_fullbackup"),
        ;

        private String key;

        Options(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public static void setLastDetectedGamePath(String lastDetectedGamePath) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        if(lastDetectedGamePath == null) {
            prefs.remove(Options.LAST_DETECTED_GAMEPATH.getKey());
        } else {
            prefs.put(Options.LAST_DETECTED_GAMEPATH.getKey(), lastDetectedGamePath);
        }
    }

    public static String getLastDetectedGamePath() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.get(Options.LAST_DETECTED_GAMEPATH.getKey(), null);
    }

    public static void setAlwaysFullBackup(boolean alwaysFullBackup) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        prefs.putBoolean(Options.ALWAYS_FULL_BACKUP.getKey(), alwaysFullBackup);
    }

    public static boolean getAlwaysFullBackup() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.getBoolean(Options.ALWAYS_FULL_BACKUP.getKey(), false);
    }
}

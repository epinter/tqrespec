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

import br.com.pinter.tqrespec.tqdata.GameVersion;
import br.com.pinter.tqrespec.tqdata.InstallType;

import java.util.prefs.Preferences;

@SuppressWarnings("unused")
public class Settings {

    enum Options {
        LAST_DETECTED_GAMEPATH("last_detectedgamepath"),
        LAST_DETECTED_GAMEVERSION("last_detectedgameversion"),
        LAST_DETECTED_INSTALLTYPE("last_detectedinstalltype"),
        LAST_DETECTED_TQBASEPATH("last_detectedtqbasepath"),
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

    public static void removeLastDetectedGame() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        prefs.remove(Options.LAST_DETECTED_GAMEPATH.getKey());
        prefs.remove(Options.LAST_DETECTED_GAMEVERSION.getKey());
        prefs.remove(Options.LAST_DETECTED_INSTALLTYPE.getKey());
        prefs.remove(Options.LAST_DETECTED_TQBASEPATH.getKey());
    }

    public static void setLastDetectedGamePath(String lastDetectedGamePath) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        if(lastDetectedGamePath == null) {
            prefs.remove(Options.LAST_DETECTED_GAMEPATH.getKey());
        } else {
            prefs.put(Options.LAST_DETECTED_GAMEPATH.getKey(), lastDetectedGamePath);
        }
    }

    public static void setLastDetectedGameVersion(GameVersion gameVersion) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        if(gameVersion == null) {
            prefs.remove(Options.LAST_DETECTED_GAMEVERSION.getKey());
        } else {
            prefs.putInt(Options.LAST_DETECTED_GAMEVERSION.getKey(), gameVersion.value());
        }
    }

    public static void setLastDetectedInstallType(InstallType installType) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        if(installType == null) {
            prefs.remove(Options.LAST_DETECTED_INSTALLTYPE.getKey());
        } else {
            prefs.putInt(Options.LAST_DETECTED_INSTALLTYPE.getKey(), installType.value());
        }
    }

    public static void setLastDetectedTqBasePath(String tqBasePath) {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        if(tqBasePath == null) {
            prefs.remove(Options.LAST_DETECTED_TQBASEPATH.getKey());
        } else {
            prefs.put(Options.LAST_DETECTED_TQBASEPATH.getKey(), tqBasePath);
        }
    }

    public static String getLastDetectedGamePath() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.get(Options.LAST_DETECTED_GAMEPATH.getKey(), null);
    }

    public static int getLastDetectedGameVersion() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.getInt(Options.LAST_DETECTED_GAMEVERSION.getKey(), 0);
    }

    public static int getLastDetectedInstallType() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.getInt(Options.LAST_DETECTED_INSTALLTYPE.getKey(), 0);
    }

    public static String getLastDetectedTqBasePath() {
        Preferences prefs = Preferences.userNodeForPackage(Settings.class);
        return prefs.get(Options.LAST_DETECTED_TQBASEPATH.getKey(), null);
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

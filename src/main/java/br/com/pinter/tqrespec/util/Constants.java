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

package br.com.pinter.tqrespec.util;

import java.io.File;
import java.nio.file.Paths;

public class Constants {
    private Constants() {
    }

    public static final String APPNAME = "TQRespec";
    public static final String LOGFILE = new File(System.getProperty("java.io.tmpdir"), "tqrespec.log").getAbsolutePath();
    public static final String LOGLEVELS = "br.com.pinter.tqrespec=INFO;br.com.pinter.tqdatabase=INFO";
    public static final String REGEX_REGISTRY_INSTALL = "Titan Quest.*Anniversary.*";
    public static final String REGEX_REGISTRY_PACKAGE = "(?i:Titan Quest.*)";
    public static final String REGEX_REGISTRY_INSTALL_FALLBACK = "Titan Quest.*";
    public static final String SAVEGAME_SUBDIR = Paths.get("My Games", "Titan Quest - Immortal Throne").toString();
    public static final String GAME_DIRECTORY_STEAM = Paths.get("common", "Titan Quest Anniversary Edition").toString();
    public static final String INITIAL_FONT_SIZE = "1";
    public static final String SAVEDATA = "SaveData";
    public static final String BACKUP_DIRECTORY = Paths.get(SAVEDATA, "TQRespec Backup").toString();
    public static final String VERSION_CHECK_URL = "https://epinter.github.io/version/tqrespec";
    public static final String DEV_GAMEDATA = Paths.get(System.getProperty("user.dir"), "gamedata").toString();
    public static final String PARENT_GAMEDATA = Paths.get(Paths.get(System.getProperty("user.dir")).getParent().toString(), "gamedata").toString();
    public static final int PROCESS_SCAN_INTERVAL_MS = 3000;
    public static final String ERROR_MSG_EXCEPTION = "Error: ";

    public static class Save {
        private Save() {
        }

        public static final String SKILL_NAME = "skillName";
        public static final String SKILL_ENABLED = "skillEnabled";
        public static final String SKILL_ACTIVE = "skillActive";
        public static final String SKILL_SUB_LEVEL = "skillSubLevel";
        public static final String SKILL_TRANSITION = "skillTransition";
        public static final String SKILL_LEVEL = "skillLevel";
        public static final String SKILL_POINTS = "skillPoints";
    }

    public static class UI {
        private UI() {
        }

        public static final String MAIN_FXML = "/fxml/main.fxml";
        public static final String ABOUT_FXML = "/fxml/about.fxml";
        public static final String MAIN_CSS = "/fxml/main.css";
        public static final String PRELOADER_CSS = "/fxml/preloader.css";
        public static final String PRELOADER_PANE_STYLE = "bg-container";
        public static final String PRELOADER_TITLE_STYLE = "tq-bigtitle";
        public static final String PRELOADER_INDICATOR_STYLE = "indicator";
        public static final String PRELOADER_BAR_STYLE = "bar";
        public static final int PRELOADER_WIDTH = 370;
        public static final int PRELOADER_HEIGHT = 210;
        public static final String TOAST_HEADER_STYLE = "tq-toast-header";
        public static final String TOAST_CONTENT_STYLE = "tq-toast-content";
    }
}

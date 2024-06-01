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

package br.com.pinter.tqrespec.util;

import com.google.common.collect.ImmutableMap;
import javafx.scene.text.Font;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Constants {
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
    public static final String PLAYERCHR = "Player.chr";
    public static final String SETTINGS = "Settings";
    public static final String JAVA_USERDIR = System.getProperty("user.dir");
    public static final String BACKUP_DIRECTORY = Paths.get(SAVEDATA, "TQRespec Backup").toString();
    public static final String VERSION_CHECK_URL = "https://epinter.github.io/version/tqrespec";
    public static final String DEV_GAMEDATA = Paths.get(JAVA_USERDIR, "gamedata").toString();
    public static final String PARENT_GAMEDATA = Paths.get(Paths.get(JAVA_USERDIR).getParent().toString(), "gamedata").toString();
    public static final String EXT_SAVEDATA = Paths.get(JAVA_USERDIR, "savedata").toString();
    public static final int PROCESS_SCAN_INTERVAL_MS = 3000;
    public static final String ERROR_MSG_EXCEPTION = "Error: ";
    public static final String STASH_FILE = "winsys.dxb";
    public static final String STASH_FILE_BACKUP = "winsys.dxg";
    public static final int MAX_CHARACTER_NAME_LENGTH = 14;
    public static final String ARCHIVE_DIR = "ArchivedCharacters";
    public static final String EXPLORER_COMMAND = "explorer.exe";
    public static final String XDGOPEN_COMMAND = "xdg-open";
    /**
     * Maps Game Options language to i18n locale
     */
    public static final Map<String, Locale> GAMELANGUAGE_LOCALE = ImmutableMap.<String, Locale>builder()
            .put("English", Locale.of("en"))
            .put("Portuguese", Locale.of("pt", "BR"))
            .put("Chinese", Locale.of("ch"))
            .put("German", Locale.of("de"))
            .put("Spanish", Locale.of("es"))
            .put("French", Locale.of("fr"))
            .put("Italian", Locale.of("it"))
            .put("Japanese", Locale.of("ja"))
            .put("Korean", Locale.of("ko"))
            .put("Polish", Locale.of("pl"))
            .put("Russian", Locale.of("ru"))
            .put("Ukrainian", Locale.of("uk")).build();
    /**
     * Maps Locale to Text files
     */
    public static final Map<Locale, String> LOCALE_TEXT = ImmutableMap.<Locale, String>builder()
            .put(Locale.of("en"), "EN")
            .put(Locale.of("pt", "BR"), "BR")
            .put(Locale.of("ch"), "CH")
            .put(Locale.of("cz"), "CZ")
            .put(Locale.of("de"), "DE")
            .put(Locale.of("es"), "ES")
            .put(Locale.of("fr"), "FR")
            .put(Locale.of("it"), "IT")
            .put(Locale.of("ja"), "JA")
            .put(Locale.of("ko"), "KO")
            .put(Locale.of("pl"), "PL")
            .put(Locale.of("ru"), "RU")
            .put(Locale.of("uk"), "UK").build();

    private Constants() {
    }

    public static class Save {
        public static final String SKILL_NAME = "skillName";
        public static final String SKILL_ENABLED = "skillEnabled";
        public static final String SKILL_ACTIVE = "skillActive";
        public static final String SKILL_SUB_LEVEL = "skillSubLevel";
        public static final String SKILL_TRANSITION = "skillTransition";
        public static final String SKILL_LEVEL = "skillLevel";
        public static final String SKILL_POINTS = "skillPoints";
        public static final String PLAYER_CHARACTER_CLASS = "playerCharacterClass";
        public static final String PLAYER_TEXTURE = "playerTexture";
        public static final String VALUE_PC_CLASS_MALE = "Warrior";
        public static final String VALUE_PC_CLASS_FEMALE = "Sorceress";
        public static final String MALE_DEFAULT_TEXTURE = "Creatures\\pc\\male\\malepc01_tan.tex";
        public static final String FEMALE_DEFAULT_TEXTURE = "Creatures\\pc\\female\\femalepc01_tan.tex";
        public static final String VAR_TELEPORTUIDSSIZE = "teleportUIDsSize";
        public static final String VAR_TELEPORTUID = "teleportUID";

        private Save() {
        }

    }

    public static class UI {
        public static final String MAIN_FXML = "/fxml/main.fxml";
        public static final String ABOUT_FXML = "/fxml/about.fxml";
        public static final String MAIN_CSS = "/fxml/main.css";
        public static final String PRELOADER_CSS = "/fxml/preloader.css";
        public static final String PRELOADER_PANE_STYLE = "bg-container";
        public static final String PRELOADER_TITLE_STYLE = "tq-bigtitle";
        public static final String PRELOADER_INDICATOR_STYLE = "indicator";
        public static final String PRELOADER_VERSION_STYLE = "version";
        public static final String PRELOADER_TOP_STYLE = "topContainer";
        public static final String PRELOADER_BOTTOM_STYLE = "bottomContainer";
        public static final String PRELOADER_BAR_STYLE = "bar";
        public static final int PRELOADER_WIDTH = 370;
        public static final int PRELOADER_HEIGHT = 180;
        public static final String TOAST_HEADER_STYLE = "tq-toast-header";
        public static final String TOAST_CONTENT_STYLE = "tq-toast-content";
        public static final int TOAST_WARNING_TIMEOUT = 10000;
        public static final int TOOLTIP_SHOWDELAY_MILLIS = 150;
        public static final int TOOLTIP_SHOWDURATION_MILLIS = 9000;
        public static final double TOOLTIP_MAXWIDTH = 500d;
        public static final Font TOOLTIP_FONT = Font.font("Marcellus", 16d);
        public static final String TAG_STRLABEL = "tagCStrength";
        public static final String TAG_INTLABEL = "tagCIntelligence";
        public static final String TAG_DEXLABEL = "tagCDexterity";
        public static final String TAG_ENERGYLABEL = "tagCEnergy";
        public static final String TAG_HEALTHLABEL = "tagCHealth";
        public static final String TAG_CHARLEVELLABEL = "tagSCharLevel";
        public static final String TAG_XPLABEL = "tagHUDXP_Current";
        public static final String TAG_CLASSLABEL = "tagSCharClass";
        public static final String TAG_DIFFICULTYLABEL = "xtagLobbyDifficulty";
        public static final String TAG_AVAILPOINTSLABEL = "tagCPoints";
        public static final String PREFIXTAG_DIFFICULTYLABEL = "tagRDifficultyTitle";
        public static final String TAG_GOLDLABEL = "tagGold";
        public static final String TAG_ATTRIBUTESTAB = "tagCAttributes";
        public static final String TAG_SKILLSTAB = "tagWindowName02";
        public static final String TAG_STAT_TOTALDEATHS = "tagCDeaths";
        public static final String TAG_STAT_ELAPSEDTIME = "tagCTime";
        public static final String TAG_STAT_MONSTERSKILLED = "tagCMonsterKills";
        public static final String TAG_STAT_GREATESTDAMAGE = "tagCHighestDamage";
        public static final String TAG_STAT_GREATESTMONSTER = "tagCHighestMonster";

        public static final List<String> FONTS_LOADLIST = List.of(
                "/fxml/Marcellus-Regular.ttf",
                "/fxml/fa5-free-solid-900.ttf"
        );

        private UI() {
        }
    }

    public static class Msg {
        public static final String MAIN_GAMENOTDETECTED = "main.gameNotDetected";
        public static final String MAIN_CHOOSEGAMEDIRECTORY = "main.chooseGameDirectory";
        public static final String CHARACTERS_TITLE_MASTERY = "characters.masteryTitle";
        public static final String CHARACTERS_NAMENUMBER_FORMAT = "%s (%d)";

        private Msg() {
        }
    }
}

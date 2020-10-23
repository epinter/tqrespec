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

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.core.GameNotFoundException;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Singleton;
import com.sun.jna.platform.win32.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class GameInfo {
    private final System.Logger logger = Log.getLogger(GameInfo.class.getName());
    private String gamePath = null;
    private HashMap<String, String> gameOptions;
    private final List<Path> resourcesText = new ArrayList<>();
    private final List<Path> databases = new ArrayList<>();
    private GameVersion installedVersion = GameVersion.UNKNOWN;

    private boolean gamePathExists(Path path) {
        Path databasePath = Paths.get(path.toString(), "Database");
        return Files.exists(databasePath) && Files.isDirectory(databasePath);
    }

    private boolean gamePathFileExists(String... path) throws InvalidPathException {
        return new File(Paths.get(gamePath, path).toString()).exists() && new File(gamePath, "Database").isDirectory();
    }

    private Path getGameSteamPath() {
        Path steamLibraryPath = getSteamLibraryPath();
        if (steamLibraryPath != null) {
            Path steamGamePath = Paths.get(steamLibraryPath.toString(), Constants.GAME_DIRECTORY_STEAM).toAbsolutePath();
            if (gamePathExists(steamGamePath)) {
                return steamGamePath;
            }
        }
        return null;
    }

    private Path getSteamLibraryPath() {
        String steamPath = null;
        try {
            steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");
        } catch (Win32Exception e) {
            logger.log(System.Logger.Level.ERROR, "Error", e);
        }

        try {
            if (steamPath == null) {
                steamPath = Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Valve\\Steam", "InstallPath",
                        WinNT.KEY_WOW64_32KEY);
            }
        } catch (Win32Exception e) {
            logger.log(System.Logger.Level.ERROR, "Error", e);
            return null;
        }

        try {
            Path steamappsPath = Paths.get(steamPath, "SteamApps").toAbsolutePath();
            Path steamGamePath = Paths.get(steamappsPath.toString(), Constants.GAME_DIRECTORY_STEAM).toAbsolutePath();
            if (gamePathExists(steamGamePath)) {
                return steamappsPath;
            }

            Pattern regexOuter = Pattern.compile(".*LibraryFolders.*\\{(.*)}.*", Pattern.DOTALL);
            Pattern regexInner = Pattern.compile("\\s*\"\\d\"\\s+\"([^\"]+)\".*");

            ArrayList<String> libraryFolderList = new ArrayList<>();
            String steamConfig = Files.readString(Paths.get(steamappsPath.toString(), "libraryfolders.vdf"));

            Matcher outer = regexOuter.matcher(steamConfig);
            if (outer.find()) {
                String content = outer.group(1);
                Matcher inner = regexInner.matcher(content);
                while (inner.find()) {
                    if (inner.group(1) != null && !inner.group(1).isEmpty()) {
                        libraryFolderList.add(inner.group(1));
                    }
                }
            }

            for (String libraryFolder : libraryFolderList) {
                Path libraryPath = Paths.get(libraryFolder, "SteamApps").toAbsolutePath();
                Path libraryGamePath = Paths.get(libraryPath.toString(), Constants.GAME_DIRECTORY_STEAM).toAbsolutePath();
                if (gamePathExists(libraryGamePath)) {
                    return libraryPath;
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private Path getGameGogPath() {
        try {
            String gog = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\GOG.com\\Games\\1196955511", "PATH", WinNT.KEY_WOW64_32KEY);
            if (StringUtils.isNotEmpty(gog)) {
                Path gogPath = Paths.get(gog).toAbsolutePath();
                if (gamePathExists(gogPath)) {
                    return gogPath;
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private Path getGameDiscTqitPath() {
        return getGameDiscPath("SOFTWARE\\Iron Lore\\Titan Quest Immortal Throne");
    }

    private Path getGameDiscTqPath() {
        return getGameDiscPath("SOFTWARE\\Iron Lore\\Titan Quest");
    }

    private Path getGameDiscPath(String reg) {
        try {
            String disc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, reg, "Install Location", WinNT.KEY_WOW64_32KEY);
            if (StringUtils.isNotBlank(disc)) {
                Path discPath = Paths.get(disc).toAbsolutePath();
                if (gamePathExists(discPath)) {
                    return discPath;
                }
            }
        } catch (Win32Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }

        return null;
    }

    private Path getGameInstalledPath(String regexGameName) {
        String[] installedApps = new String[0];
        try {
            installedApps = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall");
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }

        for (String app : installedApps)
            try {
                String appDisplayName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "DisplayName");
                if (appDisplayName.matches(regexGameName)) {
                    logger.log(System.Logger.Level.DEBUG, "Installed: displayname found -- ''{0}''", regexGameName);
                    String installed = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "InstallLocation");
                    Path installedPath = Paths.get(installed).toAbsolutePath();
                    if (gamePathExists(installedPath)) {
                        return installedPath;
                    }
                } else {
                    logger.log(System.Logger.Level.DEBUG, "Installed: displayname not found --- ''{0}'' -- ''{1}''", regexGameName, appDisplayName);
                }
            } catch (Exception e) {
                logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
            }
        return null;
    }

    private Path getGameMicrosoftStorePath() {
        String regexGameName = Constants.REGEX_REGISTRY_PACKAGE;
        String[] pkgList;
        String pkgKeyPath = "Software\\Classes\\Local Settings\\Software\\Microsoft\\Windows\\CurrentVersion\\" +
                "AppModel\\Repository\\Packages";
        try {
            pkgList = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER,
                    pkgKeyPath);
        } catch (Win32Exception e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            return null;
        }

        for (String pkg : pkgList) {
            try {
                String pkgDisplayName = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,
                        String.format("%s\\%s", pkgKeyPath, pkg), "DisplayName");
                if (pkgDisplayName.matches(regexGameName)) {
                    logger.log(System.Logger.Level.DEBUG, "Package: displayname found -- ", regexGameName);
                    String pkgInstalled = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,
                            String.format("%s\\%s", pkgKeyPath, pkg), "PackageRootFolder");
                    Path pkgInstalledPath = Paths.get(pkgInstalled).toAbsolutePath();
                    if (gamePathExists(pkgInstalledPath)) {
                        return pkgInstalledPath;
                    }
                } else {
                    logger.log(System.Logger.Level.DEBUG, "Package: displayname not found --- ''{0}'' -- ''{1}''", regexGameName, pkgDisplayName);
                }
            } catch (Win32Exception e) {
                logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            }
        }

        return null;
    }

    private Path getGameSteamApiBasedPath() {
        try {
            String steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");

            Path steamGamePath = Paths.get(steamPath).toAbsolutePath();
            if (gamePathExists(steamGamePath)) {
                return steamGamePath;
            } else {
                logger.log(System.Logger.Level.DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGamePath);
            }

            Path steamGameParentPath = Paths.get(steamPath).getParent().toAbsolutePath();
            if (gamePathExists(steamGameParentPath)) {
                return steamGameParentPath;
            } else {
                logger.log(System.Logger.Level.DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGameParentPath);
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private String detectGamePath() {
        Path installedPath = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL);
        if (installedPath != null && gamePathExists(installedPath)) {
            logger.log(System.Logger.Level.DEBUG, "Installed: found");
            return installedPath.toString();
        }

        Path gameSteam = getGameSteamPath();
        if (gameSteam != null && gamePathExists(gameSteam)) {
            logger.log(System.Logger.Level.DEBUG, "SteamLibrary: found");
            return gameSteam.toString();
        }

        Path gogPath = getGameGogPath();
        if (gogPath != null && gamePathExists(gogPath)) {
            logger.log(System.Logger.Level.DEBUG, "Gog: found");
            return gogPath.toString();
        }

        Path installedPathFallback = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL_FALLBACK);
        if (installedPathFallback != null && gamePathExists(installedPathFallback)) {
            logger.log(System.Logger.Level.DEBUG, "Installed: found");
            return installedPathFallback.toString();
        }

        Path microsoftStorePath = getGameMicrosoftStorePath();
        if (microsoftStorePath != null && gamePathExists(microsoftStorePath)) {
            logger.log(System.Logger.Level.DEBUG, "Package: found");
            return microsoftStorePath.toString();
        }

        //Anniversary Edition not found, search for TQIT
        Path discPath = getGameDiscTqitPath();
        if (discPath != null && gamePathExists(discPath)) {
            logger.log(System.Logger.Level.DEBUG, "Disc: found");
            return discPath.toString();
        }

        //Search versions that incorrectly uses SteamPath registry
        Path alternativeSteamBasedPath = getGameSteamApiBasedPath();
        if (alternativeSteamBasedPath != null && gamePathExists(alternativeSteamBasedPath)) {
            logger.log(System.Logger.Level.DEBUG, "'Alternative' installation: found");
            return alternativeSteamBasedPath.toString();
        }

        return null;
    }

    private void saveDetectedGamePath(String saveGamePath) {
        if (StringUtils.isNotBlank(saveGamePath)) {
            Settings.setLastDetectedGamePath(saveGamePath);
        }
    }

    public void removeSavedDetectedGamePath() {
        Settings.setLastDetectedGamePath(null);
    }

    public void setGamePath(String gamePath) throws GameNotFoundException {
        if (gamePathExists(Paths.get(gamePath))) {
            this.gamePath = gamePath;
            searchGamepathResources();
            saveDetectedGamePath(gamePath);
        } else {
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }

    }

    public String getGamePath() throws GameNotFoundException {
        if (StringUtils.isEmpty(gamePath) && !SystemUtils.IS_OS_WINDOWS) {
            gamePath = Constants.DEV_GAMEDATA;
            logger.log(System.Logger.Level.DEBUG, "OS is not windows, using dev game path");
            return gamePath;
        }

        if (StringUtils.isEmpty(gamePath)) {
            String lastUsed = Settings.getLastDetectedGamePath();
            if (StringUtils.isNotBlank(lastUsed)
                    && gamePathExists(Paths.get(lastUsed))) {
                gamePath = lastUsed;
                logger.log(System.Logger.Level.DEBUG, "Last-used game path found.");
                try {
                    searchGamepathResources();
                } catch (InvalidPathException e) {
                    logger.log(System.Logger.Level.ERROR, "Exception", e);
                    throw new GameNotFoundException("Game path not found", e);
                }
                return gamePath;
            }
        }

        if (StringUtils.isEmpty(gamePath)) {
            gamePath = detectGamePath();
            saveDetectedGamePath(gamePath);
        }

        if (StringUtils.isEmpty(gamePath)) {
            if (gamePathExists(Paths.get(Constants.DEV_GAMEDATA))) {
                gamePath = Constants.DEV_GAMEDATA;
                logger.log(System.Logger.Level.DEBUG, "Dev game path found");
            } else if (gamePathExists(Paths.get(Constants.PARENT_GAMEDATA))) {
                gamePath = Constants.PARENT_GAMEDATA;
            } else {
                removeSavedDetectedGamePath();
                throw new GameNotFoundException("Game path not found");
            }
        }

        logger.log(System.Logger.Level.DEBUG, "Game data found: ''{0}''", gamePath);
        if (StringUtils.isEmpty(gamePath)) {
            removeSavedDetectedGamePath();
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }

        try {
            searchGamepathResources();
        } catch (InvalidPathException e) {
            logger.log(System.Logger.Level.ERROR, "Exception", e);
            throw new GameNotFoundException("Game path not found", e);
        }
        return gamePath;
    }

    private boolean isTqitDisc() {
        return gamePathFileExists("Resources", "Text_EN.arc")
                && gamePathFileExists("tqit.exe")
                && gamePathFileExists("Resources", "XPack")
                && !gamePathFileExists("tq.exe")
                && !gamePathFileExists("Titan Quest.exe")
                && !gamePathFileExists("XPack2")
                && !gamePathFileExists("XPack3");
    }

    private boolean isTqitSteam() {
        return gamePathFileExists("Text", "Text_EN.arc")
                && gamePathFileExists("Resources", "Text_EN.arc")
                && gamePathFileExists("Resources", "XPack")
                && gamePathFileExists("tqit.exe")
                && gamePathFileExists("Titan Quest.exe")
                && !gamePathFileExists("XPack2")
                && !gamePathFileExists("XPack3");
    }

    private void addDatabasePath(Path path) {
        if (!databases.contains(path)) {
            databases.add(path);
        }
    }

    private void addTextPath(Path path) {
        if (!resourcesText.contains(path)) {
            resourcesText.add(path);
        }
    }

    private void searchGamepathResources() throws GameNotFoundException {
        if (gamePath == null) {
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }

        if (isTqitSteam()) {
            //steam tqit
            Path tqPath = getGameDiscTqPath();
            if (tqPath == null) {
                removeSavedDetectedGamePath();
                throw new GameNotFoundException("TQ base game not found for steam-version of TQIT: " + gamePath);
            }
            addDatabasePath(Paths.get(tqPath.toString(), "Database", "database.arz"));
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(gamePath, "Text"));
            addTextPath(Paths.get(gamePath, "Resources"));
            logger.log(System.Logger.Level.DEBUG, "steam tqit");
            installedVersion = GameVersion.TQIT;
        } else if (isTqitDisc()) {
            //disc tqit (one disc tq and other disc tqit)
            Path tqPath = getGameDiscTqPath();
            if (tqPath == null) {
                removeSavedDetectedGamePath();
                throw new GameNotFoundException("TQ base game not found for disc-version of TQIT" + gamePath);
            }
            addDatabasePath(Paths.get(tqPath.toString(), "Database", "database.arz"));
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(tqPath.toString(), "Text"));
            addTextPath(Paths.get(gamePath, "Resources"));
            logger.log(System.Logger.Level.DEBUG, "legacy disc");
            installedVersion = GameVersion.TQIT;
        } else {
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(gamePath, "Text"));
            installedVersion = GameVersion.TQAE;
        }
        logger.log(System.Logger.Level.INFO, "Using databases ''{0}''", databases.toString());
        logger.log(System.Logger.Level.INFO, "Using text ''{0}''", resourcesText.toString());
    }

    public String getSavePath() {
        String userHome = System.getProperty("user.home");
        logger.log(System.Logger.Level.DEBUG, "SavePath: user.home is ''{0}''", userHome);

        if (!SystemUtils.IS_OS_WINDOWS) {
            return Constants.DEV_GAMEDATA;
        }

        String saveDirectory;
        try {
            saveDirectory = Shell32Util.getFolderPath(ShlObj.CSIDL_MYDOCUMENTS);
        } catch (Exception e) {
            saveDirectory = userHome;
        }

        Path savePath = Paths.get(saveDirectory, Constants.SAVEGAME_SUBDIR);
        if (Files.exists(savePath)) {
            logger.log(System.Logger.Level.DEBUG, "SavePath: found");
            return savePath.toAbsolutePath().toString();
        }
        return null;
    }

    public String getSaveDataMainPath() {
        if (!SystemUtils.IS_OS_WINDOWS) return Paths.get(Constants.DEV_GAMEDATA, Constants.SAVEDATA, "Main").toString();
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SAVEDATA, "Main").toString();
        }
        return null;
    }

    public String getSaveDataUserPath() {
        if (!SystemUtils.IS_OS_WINDOWS) return Paths.get(Constants.DEV_GAMEDATA, Constants.SAVEDATA, "User").toString();
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SAVEDATA, "User").toString();
        }
        return null;
    }

    public String getSaveSetingsPath() {
        if (!SystemUtils.IS_OS_WINDOWS) return Paths.get(Constants.DEV_GAMEDATA, Constants.SETTINGS).toString();
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SETTINGS).toString();
        }
        return null;
    }

    public String[] getPlayerListMain() {
        String savePath = this.getSaveDataMainPath();
        File directory = new File(savePath);
        ArrayList<String> playerList = new ArrayList<>();
        if (directory.exists()) {
            for (File player : Objects.requireNonNull(directory.listFiles((File fileName) -> fileName.getName().startsWith("_")))) {
                playerList.add(player.getName().replaceAll("^_", ""));
            }
        } else {
            throw new UnhandledRuntimeException("No player found");
        }
        String[] ret = new String[playerList.size()];
        playerList.toArray(ret);
        return ret;
    }


    public Path playerChr(String playerName, boolean customQuest) {
        String path;

        if (customQuest) {
            path = getSaveDataUserPath();
        } else {
            path = getSaveDataMainPath();
        }

        return Paths.get(path, "_" + playerName, "Player.chr");
    }

    public Locale getGameLanguage() {
        String language;
        try {
            language = getGameOptionValue("language");
        } catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            return Locale.ENGLISH;
        }
        return Constants.GAMELANGUAGE_LOCALE.get(language);
    }

    public String getGameOptionValue(String key) throws IOException {
        if (gameOptions == null || !gameOptions.containsKey(key)) {
            readGameOptions();
        }
        return gameOptions.get(key);
    }

    public void readGameOptions() throws IOException {
        gameOptions = new HashMap<>();

        FileChannel optionsFile = FileChannel.open(Paths.get(getSaveSetingsPath(), "options.txt"));
        try (BufferedReader reader = new BufferedReader(Channels.newReader(optionsFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] opt = line.split("\\s+=\\s+");
                if (opt[1] != null) {
                    gameOptions.put(opt[0].trim(), opt[1].replace("\"", "").trim());
                }
            }
        }
    }

    private String[] pathsListToArray(List<Path> list) throws FileNotFoundException {
        List<String> paths = new ArrayList<>();
        for (Path p : list) {
            if (!p.toFile().exists()) {
                throw new FileNotFoundException("Invalid path " + p);
            }
            paths.add(p.toString());
        }
        return paths.toArray(new String[]{});
    }

    public String[] getDatabasePath() throws FileNotFoundException {
        try {
            if (gamePath == null) {
                getGamePath();
            }
        } catch (GameNotFoundException e) {
            removeSavedDetectedGamePath();
            logger.log(System.Logger.Level.ERROR, "Error", e);
            throw new FileNotFoundException("Database not found");
        }
        return pathsListToArray(databases);
    }

    public String[] getTextPath() throws FileNotFoundException {
        try {
            if (gamePath == null) {
                getGamePath();
            }
        } catch (GameNotFoundException e) {
            removeSavedDetectedGamePath();
            logger.log(System.Logger.Level.ERROR, "Error", e);
            throw new FileNotFoundException("Text resources not found");
        }
        return pathsListToArray(resourcesText);
    }

    public GameVersion getInstalledVersion() {
        return installedVersion;
    }
}

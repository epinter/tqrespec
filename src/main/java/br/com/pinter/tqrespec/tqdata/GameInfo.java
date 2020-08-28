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
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class GameInfo {
    private final System.Logger logger = Log.getLogger(GameInfo.class.getName());
    private String gamePath = null;
    private HashMap<String, String> gameOptions;

    @SuppressWarnings("unused")
    public String getWindowsVersion() {
        int major = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
                "CurrentMajorVersionNumber");
        int minor = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion",
                "CurrentMinorVersionNumber");
        return String.format("%d.%d", major, minor);
    }

    private boolean gamePathExists(Path path) {
        Path databasePath = Paths.get(path.toString(), "Database");
        return Files.exists(databasePath) && Files.isDirectory(databasePath);
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
        try {
            String steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");

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
            String gog64bit = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\GOG.com\\Games\\1196955511", "PATH");
            if (StringUtils.isNotEmpty(gog64bit)) {
                Path gog64bitPath = Paths.get(gog64bit).toAbsolutePath();
                if (gamePathExists(gog64bitPath)) {
                    return gog64bitPath;
                }
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }

        try {
            String gog32bit = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Wow6432Node\\GOG.com\\Games\\1196955511", "PATH");
            Path gog32bitPath = Paths.get(gog32bit).toAbsolutePath();
            if (gamePathExists(gog32bitPath)) {
                return gog32bitPath;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private Path getGameDiscPath() {
        try {
            String disc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Iron Lore\\Titan Quest Immortal Throne", "Install Location");
            if (StringUtils.isNotBlank(disc)) {
                Path discPath = Paths.get(disc).toAbsolutePath();
                if (gamePathExists(discPath)) {
                    return discPath;
                }
            }
        } catch (Exception e) {
            return null;
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

        Path microsoftStorePath = getGameMicrosoftStorePath();
        if (microsoftStorePath != null && gamePathExists(microsoftStorePath)) {
            logger.log(System.Logger.Level.DEBUG, "Package: found");
            return microsoftStorePath.toString();
        }

        Path installedPathFallback = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL_FALLBACK);
        if (installedPathFallback != null && gamePathExists(installedPathFallback)) {
            logger.log(System.Logger.Level.DEBUG, "Installed: found");
            return installedPathFallback.toString();
        }

        Path alternativeSteamBasedPath = getGameSteamApiBasedPath();
        if (alternativeSteamBasedPath != null && gamePathExists(alternativeSteamBasedPath)) {
            logger.log(System.Logger.Level.DEBUG, "'Alternative' installation: found");
            return alternativeSteamBasedPath.toString();
        }

        Path discPath = getGameDiscPath();
        if (discPath != null && gamePathExists(discPath)) {
            logger.log(System.Logger.Level.DEBUG, "Disc: found");
            return discPath.toString();
        }
        return null;
    }

    private void saveDetectedGamePath(String saveGamePath) {
        if (StringUtils.isNotBlank(saveGamePath)) {
            Settings.setLastDetectedGamePath(saveGamePath);
        }
    }

    public void setGamePath(String gamePath) {
        if (gamePathExists(Paths.get(gamePath))) {
            Settings.setLastDetectedGamePath(gamePath);
        }
        this.gamePath = gamePath;
    }

    public String getGamePath() throws FileNotFoundException {
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
                return gamePath;
            }
        }

        if (StringUtils.isEmpty(gamePath)) {
            gamePath = detectGamePath();
            logger.log(System.Logger.Level.DEBUG, "Game path not detected");
            saveDetectedGamePath(gamePath);
        }

        if (StringUtils.isEmpty(gamePath)) {
            if (gamePathExists(Paths.get(Constants.DEV_GAMEDATA))) {
                gamePath = Constants.DEV_GAMEDATA;
                logger.log(System.Logger.Level.DEBUG, "Dev game path found");
            } else if (gamePathExists(Paths.get(Constants.PARENT_GAMEDATA))) {
                gamePath = Constants.PARENT_GAMEDATA;
            } else {
                throw new FileNotFoundException("Game path not detected");
            }
        }

        logger.log(System.Logger.Level.DEBUG, "Game data found: ''{0}''", gamePath);
        return gamePath;
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
}

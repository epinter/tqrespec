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
    private InstallType installType = InstallType.UNKNOWN;
    private Path tqBasePath = null;
    private HashMap<String, String> gameOptions;
    private final List<Path> resourcesText = new ArrayList<>();
    private final List<Path> databases = new ArrayList<>();
    private GameVersion installedVersion = GameVersion.UNKNOWN;
    private boolean dlcRagnarok = false;
    private boolean dlcAtlantis = false;

    private boolean isValidGamePath(Path path) {
        if(path == null) {
            return false;
        }
        return Paths.get(path.toString(),"Database").toFile().isDirectory()
                && Paths.get(path.toString(),"Resources").toFile().isDirectory();
    }

    private boolean gamePathFileExists(String... path) throws InvalidPathException {
        return Paths.get(gamePath,"Database").toFile().isDirectory() && Paths.get(gamePath, path).toFile().exists();
    }

    private boolean gamePathFileExists(Path basePath, String... path) throws InvalidPathException {
        return Paths.get(basePath.toString(),"Database").toFile().isDirectory() && Paths.get(basePath.toString(), path).toFile().exists();
    }

    private Path getGameSteamPath() {
        Path steamLibraryPath = getSteamLibraryPath();
        if (steamLibraryPath != null) {
            Path steamGamePath = Paths.get(steamLibraryPath.toString(), Constants.GAME_DIRECTORY_STEAM).toAbsolutePath();
            if (isValidGamePath(steamGamePath)) {
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
            if (isValidGamePath(steamGamePath)) {
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
                if (isValidGamePath(libraryGamePath)) {
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
            //TQAE GOG 1196955511
            String gog = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\GOG.com\\Games\\1196955511", "PATH", WinNT.KEY_WOW64_32KEY);
            if (StringUtils.isNotEmpty(gog)) {
                Path gogPath = Paths.get(gog).toAbsolutePath();
                if (isValidGamePath(gogPath)) {
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
                if (isValidGamePath(discPath)) {
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
                    if (isValidGamePath(installedPath)) {
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
                    if (isValidGamePath(pkgInstalledPath)) {
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
            if (isValidGamePath(steamGamePath)) {
                return steamGamePath;
            } else {
                logger.log(System.Logger.Level.DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGamePath);
            }

            Path steamGameParentPath = Paths.get(steamPath).getParent().toAbsolutePath();
            if (isValidGamePath(steamGameParentPath)) {
                return steamGameParentPath;
            } else {
                logger.log(System.Logger.Level.DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGameParentPath);
            }
        } catch (Exception e) {
            logger.log(System.Logger.Level.DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private Path detectInstallation() throws GameNotFoundException {
        Path installedPath = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL);
        if (isValidGamePath(installedPath)) {
            logger.log(System.Logger.Level.DEBUG, "Installed: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.WINDOWS;
            return installedPath;
        }

        Path gameSteam = getGameSteamPath();
        if (isValidGamePath(gameSteam)) {
            logger.log(System.Logger.Level.DEBUG, "SteamLibrary: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.STEAM;
            return gameSteam;
        }

        Path gogPath = getGameGogPath();
        if (isValidGamePath(gogPath)) {
            logger.log(System.Logger.Level.DEBUG, "Gog: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.GOG;
            return gogPath;
        }

        Path installedPathFallback = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL_FALLBACK);
        if (isValidGamePath(installedPathFallback)) {
            logger.log(System.Logger.Level.DEBUG, "Installed: found");
            installType = InstallType.WINDOWS;
            installedVersion = getGameVersion(installedPathFallback);
            if(!GameVersion.UNKNOWN.equals(installedVersion)) {
                return installedPathFallback;
            }
        }

        Path microsoftStorePath = getGameMicrosoftStorePath();
        if (isValidGamePath(microsoftStorePath)) {
            logger.log(System.Logger.Level.DEBUG, "Package: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.MICROSOFT_STORE;
            return microsoftStorePath;
        }

        //Anniversary Edition not found, search for TQIT
        Path discPath = getGameDiscTqitPath();
        if (isValidGamePath(discPath)) {
            logger.log(System.Logger.Level.DEBUG, "Disc: found");
            installedVersion = GameVersion.TQIT;
            detectTqBasePath(discPath);
            return discPath;
        }

        //Search versions that incorrectly uses SteamPath registry
        Path alternativeSteamBasedPath = getGameSteamApiBasedPath();
        if (isValidGamePath(alternativeSteamBasedPath)) {
            logger.log(System.Logger.Level.DEBUG, "'Alternative' (modified dll) installation: found");
            installedVersion = getGameVersion(alternativeSteamBasedPath);
            installType = InstallType.ALTERNATIVE_STEAM_API;
            return alternativeSteamBasedPath;
        }

        return null;
    }

    private void detectTqBasePath(Path discPath) throws GameNotFoundException {
        tqBasePath = getGameDiscTqPath();
        if (tqBasePath == null || !isTqPath(tqBasePath)) {
            removeSavedDetectedGame();
            throw new GameNotFoundException("TQ base game not found for steam-version of TQIT: " + gamePath);
        }

        installType = detectTqItInstallType(discPath);
    }

    private InstallType detectTqItInstallType(Path path) {
        if(isTqitDisc(path)) {
            //disc tqit (one disc tq and other disc tqit)
            return InstallType.LEGACY_DISC;
        } else if(isTqitSteam(path)) {
            //steam tqit
            return InstallType.STEAM;
        } else {
            return InstallType.UNKNOWN;
        }
    }

    private void saveDetectedGame() {
        if (StringUtils.isNotBlank(gamePath) && installedVersion != null && installType != null
                && !InstallType.UNKNOWN.equals(installType)
                && !GameVersion.UNKNOWN.equals(installedVersion)) {
            Settings.setLastDetectedGamePath(gamePath);
            Settings.setLastDetectedGameVersion(installedVersion);
            Settings.setLastDetectedInstallType(installType);
            if(tqBasePath!=null) {
                Settings.setLastDetectedTqBasePath(tqBasePath.toString());
            } else {
                Settings.setLastDetectedTqBasePath(null);
            }
        }
    }

    public void removeSavedDetectedGame() {
        Settings.removeLastDetectedGame();
    }

    public void setManualGamePath(String path) throws GameNotFoundException {
        Path manualPath = Paths.get(path);
        if (isValidGamePath(manualPath) && (isTqAe(manualPath) || isTqitDisc(manualPath) || isTqitSteam(manualPath))) {
            installedVersion = getGameVersion(manualPath);
            if(GameVersion.TQIT.equals(installedVersion)) {
                detectTqBasePath(manualPath);
            }
            if(InstallType.UNKNOWN.equals(installType)) {
                installType = InstallType.MANUAL;
            }
            logger.log(System.Logger.Level.INFO,"Path manually set: path:{0};version:{1}:type:{2}",manualPath,installedVersion,installType);
        } else {
            logger.log(System.Logger.Level.ERROR,"Path ''{0}'' is invalid", manualPath);
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }
    }

    public void setManualTqBaseGamePath(String tqItPath, String tqPath) throws GameNotFoundException {
        Path manualTqPath = Paths.get(tqPath);
        Path manualTqItPath = Paths.get(tqItPath);
        if(isValidGamePath(manualTqPath) && isTqPath(manualTqPath)) {
            tqBasePath = manualTqPath;
            installedVersion = GameVersion.TQIT;
            installType = detectTqItInstallType(manualTqItPath);
            setGamePath(tqItPath);
            logger.log(System.Logger.Level.INFO,"Path manually set: path:{0};version:{1}:type:{2}",manualTqItPath,installedVersion,installType);
        } else {
            logger.log(System.Logger.Level.ERROR,"Path ''{0}'' is invalid", manualTqPath);
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }
    }

    private String setGamePath(String path) throws GameNotFoundException {
        if (isValidGamePath(Paths.get(path))) {
            gamePath = path;
            try {
                searchGamepathResources();
            } catch (RuntimeException e) {
                gamePath = null;
                logger.log(System.Logger.Level.ERROR, "Exception", e);
                throw new GameNotFoundException("Game path not found", e);
            }
            saveDetectedGame();
            return gamePath;
        } else {
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }
    }

    private String setDevGamePath(String path) {
        if(Paths.get(path,"Database").toFile().isDirectory() && Paths.get(path,"Text").toFile().isDirectory()) {
            addDatabasePath(Paths.get(path, "Database", "database.arz"));
            addTextPath(Paths.get(path, "Text"));
            gamePath = path;
            return gamePath;
        }

        return null;
    }

    public String getGamePath() throws GameNotFoundException {
        if (StringUtils.isEmpty(gamePath) && !SystemUtils.IS_OS_WINDOWS) {
            logger.log(System.Logger.Level.DEBUG, "OS is not windows, using dev game path");
            return setDevGamePath(Constants.DEV_GAMEDATA);
        }

        if (StringUtils.isEmpty(gamePath)) {
            String lastUsedPath = Settings.getLastDetectedGamePath();
            String lastUsedTqBase = Settings.getLastDetectedTqBasePath();
            GameVersion lastUsedVersion = GameVersion.fromValue(Settings.getLastDetectedGameVersion());
            InstallType lastUsedInstallType = InstallType.fromValue(Settings.getLastDetectedInstallType());
            if(StringUtils.isNotBlank(lastUsedPath)
                    && GameVersion.UNKNOWN.equals(lastUsedVersion) && InstallType.UNKNOWN.equals(lastUsedInstallType)) {
                removeSavedDetectedGame();
            }

            if (StringUtils.isNotBlank(lastUsedPath) && isValidGamePath(Paths.get(lastUsedPath))
                    && !GameVersion.UNKNOWN.equals(lastUsedVersion) && !InstallType.UNKNOWN.equals(lastUsedInstallType)) {
                installedVersion = lastUsedVersion;
                installType = lastUsedInstallType;
                if(StringUtils.isNotBlank(lastUsedTqBase) && isValidGamePath(Paths.get(lastUsedTqBase))
                    && GameVersion.TQIT.equals(installedVersion)) {
                    tqBasePath = Paths.get(lastUsedTqBase);
                    if(!isValidGamePath(tqBasePath)) {
                        removeSavedDetectedGame();
                        throw new GameNotFoundException("TQ base game not found for steam-version of TQIT: " + gamePath);
                    }
                }
                logger.log(System.Logger.Level.DEBUG, "Last-used game path found.");
                return setGamePath(lastUsedPath);
            }
        }

        if (StringUtils.isEmpty(gamePath)) {
            Path installedPath = detectInstallation();
            if (installedPath != null) {
                setGamePath(installedPath.toString());
            }
        }

        if (StringUtils.isEmpty(gamePath)) {
            if (Paths.get(Constants.DEV_GAMEDATA,"Database").toFile().isDirectory()) {
                logger.log(System.Logger.Level.INFO, "Dev game path found");
                return setDevGamePath(Constants.DEV_GAMEDATA);
            } else if (Paths.get(Constants.PARENT_GAMEDATA,"Database").toFile().isDirectory()) {
                logger.log(System.Logger.Level.INFO, "Parent game path found");
                return setDevGamePath(Constants.PARENT_GAMEDATA);
            } else {
                removeSavedDetectedGame();
                throw new GameNotFoundException("Game path not found");
            }
        }

        logger.log(System.Logger.Level.DEBUG, "Game data found: ''{0}''", gamePath);
        if (StringUtils.isEmpty(gamePath)) {
            removeSavedDetectedGame();
            throw new GameNotFoundException(Util.getUIMessage("main.gameNotDetected"));
        }
        return gamePath;
    }

    private boolean isTqPath(Path path) {
        return gamePathFileExists(path,"Text", "Text_EN.arc")
                && gamePathFileExists(path,"Titan Quest.exe")
                && !gamePathFileExists(path,"Resources", "XPack");
    }

    private boolean isTqitDisc(Path path) {
        return gamePathFileExists(path, "Resources", "Text_EN.arc")
                && gamePathFileExists(path,"tqit.exe")
                && gamePathFileExists(path,"Resources", "XPack")
                && !gamePathFileExists(path,"tq.exe")
                && !gamePathFileExists(path,"Titan Quest.exe")
                && !gamePathFileExists(path,"XPack2")
                && !gamePathFileExists(path,"XPack3");
    }

    private boolean isTqitSteam(Path path) {
        return gamePathFileExists(path,"Text", "Text_EN.arc")
                && gamePathFileExists(path,"Resources", "Text_EN.arc")
                && gamePathFileExists(path,"Resources", "XPack")
                && gamePathFileExists(path,"tqit.exe")
                && gamePathFileExists(path,"Titan Quest.exe")
                && !gamePathFileExists(path,"XPack2")
                && !gamePathFileExists(path,"XPack3");
    }

    private boolean isTqAe(Path path) {
        return !gamePathFileExists(path,"Resources", "Text_EN.arc")
                && !gamePathFileExists(path,"tqit.exe")
                && gamePathFileExists(path,"Text", "Text_EN.arc")
                && gamePathFileExists(path,"Resources", "XPack");
    }

    private GameVersion getGameVersion(Path path) {
        if(isTqitSteam(path) || isTqitDisc(path)) {
            return GameVersion.TQIT;
        } else if(isTqAe(path)){
            return GameVersion.TQAE;
        }
        return GameVersion.UNKNOWN;
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

        if (GameVersion.TQIT.equals(installedVersion) && InstallType.STEAM.equals(installType)) {
            addDatabasePath(Paths.get(tqBasePath.toString(), "Database", "database.arz"));
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(gamePath, "Text"));
            addTextPath(Paths.get(gamePath, "Resources"));
            logger.log(System.Logger.Level.DEBUG, "steam tqit");
        } else if (GameVersion.TQIT.equals(installedVersion) && InstallType.LEGACY_DISC.equals(installType)) {
            addDatabasePath(Paths.get(tqBasePath.toString(), "Database", "database.arz"));
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(tqBasePath.toString(), "Text"));
            addTextPath(Paths.get(gamePath, "Resources"));
            logger.log(System.Logger.Level.DEBUG, "legacy disc");
        } else if (GameVersion.TQAE.equals(installedVersion)){
            addDatabasePath(Paths.get(gamePath, "Database", "database.arz"));
            addTextPath(Paths.get(gamePath, "Text"));
            if(gamePathFileExists("Resources","XPack2")) {
                dlcRagnarok = true;
            }
            if(gamePathFileExists("Resources","XPack3")) {
                dlcAtlantis = true;
            }
        } else {
            gamePath = null;
            throw new GameNotFoundException(String.format("Can't find TQIT or TQAE (%s,%s)", installedVersion,installType));
        }
        logger.log(System.Logger.Level.INFO, "Using databases ''{0}''", databases.toString());
        logger.log(System.Logger.Level.INFO, "Using text ''{0}''", resourcesText.toString());
        logger.log(System.Logger.Level.INFO, "GameVersion:''{0}'';InstallType:''{1}''", installedVersion,installType);
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
        String[] databasePaths;
        try {
            if (gamePath == null) {
                getGamePath();
            }
            databasePaths = pathsListToArray(databases);
        } catch (GameNotFoundException | FileNotFoundException e) {
            removeSavedDetectedGame();
            logger.log(System.Logger.Level.ERROR, "Error", e);
            throw new FileNotFoundException("Database not found");
        }
        return databasePaths;
    }

    public String[] getTextPath() throws FileNotFoundException {
        String[] textPaths;
        try {
            if (gamePath == null) {
                getGamePath();
            }
            textPaths = pathsListToArray(resourcesText);
        } catch (GameNotFoundException | FileNotFoundException e) {
            removeSavedDetectedGame();
            logger.log(System.Logger.Level.ERROR, "Error", e);
            throw new FileNotFoundException("Text resources not found");
        }
        return textPaths;
    }

    public GameVersion getInstalledVersion() {
        return installedVersion;
    }

    public boolean isDlcRagnarok() {
        return dlcRagnarok;
    }

    public boolean isDlcAtlantis() {
        return dlcAtlantis;
    }
}

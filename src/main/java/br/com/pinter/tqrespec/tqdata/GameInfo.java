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

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.core.GameNotFoundException;
import br.com.pinter.tqrespec.gui.ResourceHelper;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.SaveLocation;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Singleton;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Shell32Util;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@Singleton
public class GameInfo {
    private final System.Logger logger = Log.getLogger(GameInfo.class);
    private static final String DATABASE_DIR = "Database";
    private static final String TEXT_DIR = "Text";
    private static final String DATABASE_FILE = "database.arz";
    private static final String TEXT_FILE = "Text_EN.arc";
    private static final String RESOURCES_DIR = "Resources";
    private static final String REG_KEY_VALVE_STEAM = "SOFTWARE\\Valve\\Steam";
    private final List<Path> resourcesText = new ArrayList<>();
    private final List<Path> databases = new ArrayList<>();
    private String gamePath = null;
    private InstallType installType = InstallType.UNKNOWN;
    private Path tqBasePath = null;
    private Path steamLibraryPathFound = null;
    private Path savePathFound = null;
    private HashMap<String, String> gameOptions;
    private GameVersion installedVersion = GameVersion.UNKNOWN;
    private boolean dlcRagnarok = false;
    private boolean dlcAtlantis = false;
    private boolean dlcEmbers = false;

    private boolean isValidGamePath(Path path) {
        if (path == null) {
            return false;
        }
        return resolvePath(Paths.get(path.toString(), DATABASE_DIR)).toFile().isDirectory()
                && resolvePath(Paths.get(path.toString(), RESOURCES_DIR)).toFile().isDirectory();
    }

    private boolean isValidLocalPath(Path path) {
        if (path == null) {
            return false;
        }
        return resolvePath(Paths.get(path.toString(), DATABASE_DIR, DATABASE_FILE)).toFile().isFile()
                && resolvePath(Paths.get(path.toString(), TEXT_DIR)).toFile().isDirectory();
    }

    private boolean gamePathFileExists(Path basePath, String... path) {
        return resolvePath(Paths.get(basePath.toString(), DATABASE_DIR)).toFile().isDirectory() && resolvePath(Paths.get(basePath.toString(), path)).toFile().exists();
    }

    private boolean existsTqitExe(Path basePath) {
        return gamePathFileExists(basePath, "tqit.exe");
    }

    private boolean existsTqAeExe(Path basePath) {
        return gamePathFileExists(basePath, "TQ.exe");
    }

    private boolean existsLegacyTqExe(Path basePath) {
        return gamePathFileExists(basePath, "Titan Quest.exe");
    }

    private boolean existsXpack(Path basePath, int n) {
        return gamePathFileExists(basePath, RESOURCES_DIR, String.format("XPack%s", n == 1 ? "" : n));
    }

    private Path getGameSteamPath() {
        Path steamLibraryPath = getSteamLibraryPath();
        logger.log(DEBUG, "LibraryPathFound -- ''{0}''", steamLibraryPath);
        if (steamLibraryPath != null) {
            Path steamGamePath = resolvePath(Path.of(
                    steamLibraryPath.toAbsolutePath().toString(), Constants.GAME_DIRECTORY_STEAM));
            if (isValidGamePath(steamGamePath)) {
                return steamGamePath;
            }
        }
        return null;
    }

    private Path getSteamLibraryPath() {
        String steamPath;
        Path steamLibraryFolderVdf;

        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                steamPath = Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_CURRENT_USER, REG_KEY_VALVE_STEAM, "SteamPath");

                if (steamPath == null) {
                    steamPath = Advapi32Util.registryGetStringValue(
                            WinReg.HKEY_LOCAL_MACHINE, REG_KEY_VALVE_STEAM, "InstallPath",
                            WinNT.KEY_WOW64_32KEY);
                }
            } catch (Win32Exception e) {
                logger.log(ERROR, "", e);
                return null;
            }
            steamLibraryFolderVdf = Paths.get(steamPath, "SteamApps", "libraryfolders.vdf").toAbsolutePath();
        } else {
            steamLibraryFolderVdf = resolvePath(Path.of(System.getProperty("user.home"), ".steam", "steam", "config", "libraryfolders.vdf").toAbsolutePath());
        }

        try {

            List<String> libraryPaths = getLibraryPathsFromSteam(steamLibraryFolderVdf.toString());

            logger.log(DEBUG, "libraryFolderList -- ''{0}''", libraryPaths);

            for (String directory : libraryPaths) {
                logger.log(DEBUG, "Trying library -- ''{0}''", directory);

                Path libraryPath = resolvePath(Path.of(directory, "steamapps").toAbsolutePath());
                Path libraryGamePath = resolvePath(Path.of(libraryPath.toString(), Constants.GAME_DIRECTORY_STEAM));
                if (isValidGamePath(libraryGamePath)) {
                    logger.log(DEBUG, "VALID PATH FOUND!! -- ''{0}''", libraryGamePath);
                    steamLibraryPathFound = libraryPath;
                    return libraryPath;
                }
            }
        } catch (Exception e) {
            logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private List<String> getLibraryPathsFromSteam(String configPath) {
        Pattern regexLibraryFolders = Pattern.compile("\"LibraryFolders\"\\s*[\\n\\s.]*\\{(.*)}[\\n\\s\\W]*",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Pattern regexAppsLibraries = Pattern.compile("\\s*(?<=\"\\d{1,10}\")[\\n\\s]*\\{([^}]*(?:(?<=\\{).*(?=})){0,100}[^}]+})[\\n\\s]*}[\\n\\s\\W]*",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Pattern regexLibraryPath = Pattern.compile("\"path\"\\s*\"([^\"]*)\"[\\n\\W]*", Pattern.CASE_INSENSITIVE);

        Pattern regexAppId = Pattern.compile("\"(\\d{1,10})\"\\s*\"([^\"]*)\"[\\n\\W]*");

        Pattern regexApps = Pattern.compile("(?<=\"apps\")[\\n\\s]*\\{((?:(?<=\\{).*(?=})){0,100}[^}]+)}[\\n\\s\\W]*",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Pattern regexInner = Pattern.compile("\"\\d{1,10}\"[\\n\\s]*(?<!\\{)\"([^\"]+)\"");

        String steamConfig;
        try {
            steamConfig = Files.readString(Paths.get(configPath));
        } catch (IOException e) {
            return Collections.emptyList();
        }

        List<String> librariesPath = new ArrayList<>();
        Matcher matcherFile = regexLibraryFolders.matcher(steamConfig);
        if (matcherFile.find()) {
            String libraryFoldersContent = matcherFile.group(1);
            Matcher matcherLibrary = regexAppsLibraries.matcher(libraryFoldersContent);
            if (matcherLibrary.find()) {
                matcherLibrary.results().forEach(l -> {
                    Matcher matcherApp = regexApps.matcher(l.group(1));
                    matcherApp.results().forEach(a -> {
                        Matcher matcherAppId = regexAppId.matcher(a.group(1));
                        matcherAppId.results().forEach(id -> {
                            if (StringUtils.equals(id.group(1), "475150")) {
                                logger.log(DEBUG, "Steam AppId found ''{0}''", id.group(1));
                            }
                        });
                    });
                    Matcher matcherPath = regexLibraryPath.matcher(l.group(1));
                    if (matcherPath.find() && Files.isDirectory(Path.of(matcherPath.group(1)))) {
                        librariesPath.add(matcherPath.group(1));
                    }
                });
            }

            if (librariesPath.isEmpty()) {
                Matcher inner = regexInner.matcher(libraryFoldersContent);
                while (inner.find()) {
                    if (inner.group(1) != null && !inner.group(1).isEmpty()) {
                        librariesPath.add(inner.group(1));
                    }
                }
            }
        }
        return librariesPath;
    }

    private Path getGameGogPath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

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
            logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
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
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        try {
            String disc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, reg, "Install Location", WinNT.KEY_WOW64_32KEY);
            if (StringUtils.isNotBlank(disc)) {
                Path discPath = Paths.get(disc).toAbsolutePath();
                if (isValidGamePath(discPath)) {
                    return discPath;
                }
            }
        } catch (Win32Exception e) {
            logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }

        return null;
    }

    private Path getGameInstalledPath(String regexGameName) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        String[] installedApps = new String[0];
        try {
            installedApps = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall");
        } catch (Exception e) {
            logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }

        for (String app : installedApps)
            try {
                String appDisplayName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "DisplayName");
                if (appDisplayName.matches(regexGameName)) {
                    logger.log(DEBUG, "Installed: displayname found -- ''{0}''", regexGameName);
                    String installed = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "InstallLocation");
                    Path installedPath = Paths.get(installed).toAbsolutePath();
                    if (isValidGamePath(installedPath)) {
                        return installedPath;
                    }
                } else {
                    logger.log(DEBUG, "Installed: displayname not found --- ''{0}'' -- ''{1}''", regexGameName, appDisplayName);
                }
            } catch (Exception e) {
                logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
            }
        return null;
    }

    private Path getGameMicrosoftStorePath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        String regexGameName = Constants.REGEX_REGISTRY_PACKAGE;
        String[] pkgList;
        String pkgKeyPath = "Software\\Classes\\Local Settings\\Software\\Microsoft\\Windows\\CurrentVersion\\" +
                "AppModel\\Repository\\Packages";
        try {
            pkgList = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER,
                    pkgKeyPath);
        } catch (Win32Exception e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            return null;
        }

        for (String pkg : pkgList) {
            try {
                String pkgDisplayName = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,
                        String.format("%s\\%s", pkgKeyPath, pkg), "DisplayName");
                if (pkgDisplayName.matches(regexGameName)) {
                    logger.log(DEBUG, "Package: displayname found -- ", regexGameName);
                    String pkgInstalled = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,
                            String.format("%s\\%s", pkgKeyPath, pkg), "PackageRootFolder");
                    Path pkgInstalledPath = Paths.get(pkgInstalled).toAbsolutePath();
                    if (isValidGamePath(pkgInstalledPath)) {
                        return pkgInstalledPath;
                    }
                } else {
                    logger.log(DEBUG, "Package: displayname not found --- ''{0}'' -- ''{1}''", regexGameName, pkgDisplayName);
                }
            } catch (Win32Exception e) {
                logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            }
        }

        return null;
    }

    private Path getGameSteamApiBasedPath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        try {
            String steamPath = Advapi32Util.registryGetStringValue(
                    WinReg.HKEY_CURRENT_USER, REG_KEY_VALVE_STEAM, "SteamPath");

            Path steamGamePath = Paths.get(steamPath).toAbsolutePath();
            if (isValidGamePath(steamGamePath)) {
                return steamGamePath;
            } else {
                logger.log(DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGamePath);
            }

            Path steamGameParentPath = Paths.get(steamPath).getParent().toAbsolutePath();
            if (isValidGamePath(steamGameParentPath)) {
                return steamGameParentPath;
            } else {
                logger.log(DEBUG, "GameSteamApiBasedPath: not found at ''{0}''", steamGameParentPath);
            }
        } catch (Exception e) {
            logger.log(DEBUG, Constants.ERROR_MSG_EXCEPTION, e);
        }
        return null;
    }

    private Path detectInstallation() throws GameNotFoundException {
        //search AE in Windows registry
        Path installedPath = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL);
        if (isValidGamePath(installedPath)) {
            logger.log(DEBUG, "Installed: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.WINDOWS;
            return installedPath;
        }

        //search AE in Steam
        Path gameSteam = getGameSteamPath();
        if (isValidGamePath(gameSteam)) {
            logger.log(DEBUG, "SteamLibrary: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.STEAM;
            return gameSteam;
        }

        //search AE in GOG
        Path gogPath = getGameGogPath();
        if (isValidGamePath(gogPath)) {
            logger.log(DEBUG, "Gog: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.GOG;
            return gogPath;
        }

        //try Windows registry with more generic name, and guess the version
        Path installedPathFallback = getGameInstalledPath(Constants.REGEX_REGISTRY_INSTALL_FALLBACK);
        if (isValidGamePath(installedPathFallback)) {
            logger.log(DEBUG, "Installed: found");
            installType = InstallType.WINDOWS;
            installedVersion = getGameVersion(installedPathFallback);
            if (!GameVersion.UNKNOWN.equals(installedVersion)) {
                return installedPathFallback;
            }
        }

        //search AE in MS Store
        Path microsoftStorePath = getGameMicrosoftStorePath();
        if (isValidGamePath(microsoftStorePath)) {
            logger.log(DEBUG, "Package: found");
            installedVersion = GameVersion.TQAE;
            installType = InstallType.MICROSOFT_STORE;
            return microsoftStorePath;
        }

        //Anniversary Edition not found, search for TQIT
        Path discPath = getGameDiscTqitPath();
        if (isValidGamePath(discPath)) {
            logger.log(DEBUG, "Disc: found");
            installedVersion = GameVersion.TQIT;
            detectTqBasePath(discPath);
            return discPath;
        }

        //Search versions that incorrectly uses SteamPath registry
        Path alternativeSteamBasedPath = getGameSteamApiBasedPath();
        if (isValidGamePath(alternativeSteamBasedPath)) {
            logger.log(DEBUG, "'Alternative' (modified dll) installation: found");
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
        if (isTqitDisc(path)) {
            //disc tqit (one disc tq and other disc tqit)
            return InstallType.LEGACY_DISC;
        } else if (isTqitSteam(path)) {
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
            if (tqBasePath != null) {
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
            if (GameVersion.TQIT.equals(installedVersion)) {
                detectTqBasePath(manualPath);
            }
            if (InstallType.UNKNOWN.equals(installType)) {
                installType = InstallType.MANUAL;
            }
            setGamePath(manualPath.toString());
            logger.log(INFO, "Path manually set: path:{0};version:{1}:type:{2}", manualPath, installedVersion, installType);
        } else {
            logger.log(ERROR, "Path ''{0}'' is invalid", manualPath);
            throw new GameNotFoundException(ResourceHelper.getMessage(Constants.Msg.MAIN_GAMENOTDETECTED));
        }
    }

    public void setManualTqBaseGamePath(String tqItPath, String tqPath) throws GameNotFoundException {
        Path manualTqPath = Paths.get(tqPath);
        Path manualTqItPath = Paths.get(tqItPath);
        if (isValidGamePath(manualTqPath) && isTqPath(manualTqPath)) {
            tqBasePath = manualTqPath;
            installedVersion = GameVersion.TQIT;
            installType = detectTqItInstallType(manualTqItPath);
            setGamePath(tqItPath);
            logger.log(INFO, "Path manually set: path:{0};version:{1}:type:{2}", manualTqItPath, installedVersion, installType);
        } else {
            logger.log(ERROR, "Path ''{0}'' is invalid", manualTqPath);
            throw new GameNotFoundException(ResourceHelper.getMessage(Constants.Msg.MAIN_GAMENOTDETECTED));
        }
    }

    private String setGamePath(String path) throws GameNotFoundException {
        if (isValidGamePath(Paths.get(path))) {
            gamePath = path;
            try {
                searchGamepathResources();
            } catch (RuntimeException e) {
                gamePath = null;
                logger.log(ERROR, "Exception", e);
                throw new GameNotFoundException("Game path not found", e);
            }
            saveDetectedGame();
            logger.log(INFO, "Using databases ''{0}''", databases.toString());
            logger.log(INFO, "Using text ''{0}''", resourcesText.toString());
            logger.log(INFO, "GameVersion:''{0}'';InstallType:''{1}''", installedVersion, installType);
            return gamePath;
        } else {
            throw new GameNotFoundException(ResourceHelper.getMessage(Constants.Msg.MAIN_GAMENOTDETECTED));
        }
    }

    private String setDevGamePath(String path) {
        if (resolvePath(Paths.get(path, DATABASE_DIR)).toFile().isDirectory() && resolvePath(Paths.get(path, TEXT_DIR)).toFile().isDirectory()) {
            addDatabasePath(Paths.get(path, DATABASE_DIR, DATABASE_FILE));
            addTextPath(Paths.get(path, TEXT_DIR));
            dlcRagnarok = true;
            dlcAtlantis = true;
            gamePath = path;
            logger.log(INFO, "Using databases ''{0}''", databases.toString());
            logger.log(INFO, "Using text ''{0}''", resourcesText.toString());
            logger.log(INFO, "GameVersion:''{0}'';InstallType:''{1}''", installedVersion, installType);
            return gamePath;
        }

        return null;
    }

    private String getLastUsedGamePath() throws GameNotFoundException {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return null;
        }

        String lastUsedPath = Settings.getLastDetectedGamePath();
        String lastUsedTqBase = Settings.getLastDetectedTqBasePath();
        GameVersion lastUsedVersion = GameVersion.fromValue(Settings.getLastDetectedGameVersion());
        InstallType lastUsedInstallType = InstallType.fromValue(Settings.getLastDetectedInstallType());
        if (StringUtils.isNotBlank(lastUsedPath)
                && GameVersion.UNKNOWN.equals(lastUsedVersion) && InstallType.UNKNOWN.equals(lastUsedInstallType)) {
            removeSavedDetectedGame();
        }

        if (StringUtils.isNotBlank(lastUsedPath) && isValidGamePath(Paths.get(lastUsedPath))
                && !GameVersion.UNKNOWN.equals(lastUsedVersion) && !InstallType.UNKNOWN.equals(lastUsedInstallType)) {
            installedVersion = lastUsedVersion;
            installType = lastUsedInstallType;
            if (StringUtils.isNotBlank(lastUsedTqBase) && isValidGamePath(Paths.get(lastUsedTqBase))
                    && GameVersion.TQIT.equals(installedVersion)) {
                tqBasePath = Paths.get(lastUsedTqBase);
                if (!isValidGamePath(tqBasePath)) {
                    removeSavedDetectedGame();
                    throw new GameNotFoundException("TQ base game not found for steam-version of TQIT: " + gamePath);
                }
            }
            logger.log(DEBUG, "Last-used game path found.");
            return setGamePath(lastUsedPath);
        }
        return null;
    }

    public String getGamePath() throws GameNotFoundException {
        if (isValidLocalPath(Paths.get(Constants.DEV_GAMEDATA))) {
            logger.log(INFO, "Local gamedata path found");
            return setDevGamePath(Constants.DEV_GAMEDATA);
        } else if (isValidLocalPath(Paths.get(Constants.PARENT_GAMEDATA))) {
            logger.log(INFO, "Parent gamedata path found");
            return setDevGamePath(Constants.PARENT_GAMEDATA);
        }

        if (StringUtils.isEmpty(gamePath)) {
            String lastUsedGamePath = getLastUsedGamePath();
            if (StringUtils.isNotBlank(lastUsedGamePath)) {
                return lastUsedGamePath;
            }
        }

        if (StringUtils.isEmpty(gamePath)) {
            Path installedPath = detectInstallation();
            if (installedPath != null) {
                setGamePath(installedPath.toString());
            }
        }

        if (StringUtils.isEmpty(gamePath) && !SystemUtils.IS_OS_WINDOWS) {
            logger.log(DEBUG, "OS is not windows, using dev game path");
            return setDevGamePath(Constants.DEV_GAMEDATA);
        }

        logger.log(DEBUG, "Game data found: ''{0}''", gamePath);
        if (StringUtils.isEmpty(gamePath)) {
            removeSavedDetectedGame();
            throw new GameNotFoundException(ResourceHelper.getMessage(Constants.Msg.MAIN_GAMENOTDETECTED));
        }
        return gamePath;
    }

    private boolean isTqPath(Path path) {
        return gamePathFileExists(path, TEXT_DIR, TEXT_FILE)
                && gamePathFileExists(path, "Titan Quest.exe")
                && !existsXpack(path, 1);
    }

    private boolean isTqitDisc(Path path) {
        return gamePathFileExists(path, RESOURCES_DIR, TEXT_FILE)
                && existsTqitExe(path)
                && existsXpack(path, 1)
                && !existsTqAeExe(path)
                && !existsLegacyTqExe(path)
                && !existsXpack(path, 2)
                && !existsXpack(path, 3);
    }

    private boolean isTqitSteam(Path path) {
        return gamePathFileExists(path, TEXT_DIR, TEXT_FILE)
                && gamePathFileExists(path, RESOURCES_DIR, TEXT_FILE)
                && existsXpack(path, 1)
                && existsTqitExe(path)
                && existsLegacyTqExe(path)
                && !existsXpack(path, 2)
                && !existsXpack(path, 3);
    }

    private boolean isTqAe(Path path) {
        return !gamePathFileExists(path, RESOURCES_DIR, TEXT_FILE)
                && !existsTqitExe(path)
                && gamePathFileExists(path, TEXT_DIR, TEXT_FILE)
                && existsXpack(path, 1);
    }

    private GameVersion getGameVersion(Path path) {
        if (isTqitSteam(path) || isTqitDisc(path)) {
            return GameVersion.TQIT;
        } else if (isTqAe(path)) {
            return GameVersion.TQAE;
        }
        return GameVersion.UNKNOWN;
    }

    private void addDatabasePath(Path path) {
        Path realPath = resolvePath(path);
        if (!databases.contains(realPath)) {
            databases.add(realPath);
        }
    }

    private Path resolvePath(Path path) {
        if (Files.exists(path)) {
            return path;
        }
        if (path.getParent() != null) {
            try (Stream<Path> stream = Files.list(resolvePath(path.getParent()))) {
                List<Path> file = stream.filter(f -> f.getFileName().toString().equalsIgnoreCase(path.getFileName().toString())).toList();
                if (!file.isEmpty()) {
                    return file.getFirst().toAbsolutePath();
                }
            } catch (NoSuchFileException ignored) {
            } catch (IOException e) {
                logger.log(ERROR, "Error", e);
            }
        }
        return path.toAbsolutePath();
    }

    private void addTextPath(Path path) {
        Path realPath = resolvePath(path);
        if (!resourcesText.contains(realPath)) {
            resourcesText.add(realPath);
        }
    }

    private void searchGamepathResources() throws GameNotFoundException {
        if (gamePath == null) {
            throw new GameNotFoundException(ResourceHelper.getMessage(Constants.Msg.MAIN_GAMENOTDETECTED));
        }

        if (GameVersion.TQIT.equals(installedVersion) && InstallType.STEAM.equals(installType)) {
            addDatabasePath(Paths.get(tqBasePath.toString(), DATABASE_DIR, DATABASE_FILE));
            addDatabasePath(Paths.get(gamePath, DATABASE_DIR, DATABASE_FILE));
            addTextPath(Paths.get(gamePath, TEXT_DIR));
            addTextPath(Paths.get(gamePath, RESOURCES_DIR));
            logger.log(DEBUG, "steam tqit");
        } else if (GameVersion.TQIT.equals(installedVersion) && InstallType.LEGACY_DISC.equals(installType)) {
            addDatabasePath(Paths.get(tqBasePath.toString(), DATABASE_DIR, DATABASE_FILE));
            addDatabasePath(Paths.get(gamePath, DATABASE_DIR, DATABASE_FILE));
            addTextPath(Paths.get(tqBasePath.toString(), TEXT_DIR));
            addTextPath(Paths.get(gamePath, RESOURCES_DIR));
            logger.log(DEBUG, "legacy disc");
        } else if (GameVersion.TQAE.equals(installedVersion)) {
            addDatabasePath(Paths.get(gamePath, DATABASE_DIR, DATABASE_FILE));
            addTextPath(Paths.get(gamePath, TEXT_DIR));
            if (existsXpack(Paths.get(gamePath), 2)) {
                dlcRagnarok = true;
            }
            if (existsXpack(Paths.get(gamePath), 3)) {
                dlcAtlantis = true;
            }
            if (existsXpack(Paths.get(gamePath), 4)) {
                dlcEmbers = true;
            }
        } else {
            gamePath = null;
            throw new GameNotFoundException(String.format("Can't find TQIT or TQAE (%s,%s)", installedVersion, installType));
        }
    }

    private void prepareDevGameSaveData() {
        File gamedataSavePath = Paths.get(Constants.DEV_GAMEDATA, Constants.SAVEDATA).toFile();
        if (!gamedataSavePath.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                gamedataSavePath.mkdir();
            } catch (SecurityException e) {
                logger.log(INFO, "unable to create SaveData directory at " + gamedataSavePath.getAbsolutePath());
            }
        }
    }

    public String getSavePath() {
        if (savePathFound != null && Files.exists(savePathFound)) {
            return savePathFound.toAbsolutePath().toString();
        }

        String userHome = System.getProperty("user.home");
        logger.log(DEBUG, "SavePath: user.home is ''{0}''", userHome);

        if (!SystemUtils.IS_OS_WINDOWS) {
            if (gamePath == null || steamLibraryPathFound == null) {
                try {
                    getGamePath();
                } catch (GameNotFoundException ignored) {
                }
            }
            if (installType == InstallType.STEAM) {
                savePathFound = resolvePath(Path.of(steamLibraryPathFound.toAbsolutePath().toString(),
                        "compatdata", "475150", "pfx", "drive_c", "users", "steamuser", "Documents",
                        Constants.SAVEGAME_SUBDIR)).toAbsolutePath();

                return savePathFound.toString();
            }
            prepareDevGameSaveData();
            savePathFound = resolvePath(Path.of(Constants.DEV_GAMEDATA).toAbsolutePath());
            return savePathFound.toString();
        }

        String doc;
        try {
            doc = Shell32Util.getFolderPath(ShlObj.CSIDL_MYDOCUMENTS);
        } catch (Exception e) {
            doc = userHome;
        }

        Path savePath = Path.of(doc, Constants.SAVEGAME_SUBDIR).toAbsolutePath();
        if (Files.exists(savePath)) {
            logger.log(DEBUG, "SavePath: found");
            savePathFound = savePath;
            return savePathFound.toString();
        }
        return null;
    }

    public List<Path> getCustomMaps() {
        List<Path> mods = new ArrayList<>();
        String savePath = getSavePath();
        if(StringUtils.isEmpty(savePath)) {
            return mods;
        }
        Path userMaps = resolvePath(Path.of(savePath, "CustomMaps")).toAbsolutePath();
        if (steamLibraryPathFound != null && Files.exists(steamLibraryPathFound)) {
            Path steamMapsParent = resolvePath(Path.of(steamLibraryPathFound.toString(), "workshop", "content", "475150"));
            try (DirectoryStream<Path> workshop = Files.newDirectoryStream(steamMapsParent)) {
                workshop.forEach(m -> {
                    if (!Files.isDirectory(m)) {
                        return;
                    }
                    List<Path> mdirs = new ArrayList<>();
                    try (Stream<Path> stream = Files.list(m)) {
                        stream.filter(f -> Files.isDirectory(f) && getCustomMapDatabase(f) != null).forEach(mdirs::add);
                        mods.addAll(mdirs);
                    } catch (IOException e) {
                        logger.log(ERROR, "Error", e);
                    }
                });
            } catch (IOException e) {
                logger.log(ERROR, "Error", e);
            }
        }
        if (Files.exists(userMaps)) {
            File[] files = userMaps.toAbsolutePath().toFile().listFiles(File::isDirectory);
            if (files != null) {
                for (File m : files) {
                    if (getCustomMapDatabase(m.toPath()) != null) {
                        mods.add(m.toPath());
                    }
                }
            }
        }
        return mods;
    }

    public Path getCustomMapDatabase(Path customMap) {
        File[] databaseDirs = customMap.toAbsolutePath().toFile().listFiles(
                f -> f.isDirectory() && f.getName().equalsIgnoreCase("database"));

        if (databaseDirs != null && databaseDirs.length == 1) {
            File[] arzFile = databaseDirs[0].listFiles(f ->
                    f.getName().equalsIgnoreCase(customMap.getFileName().toString() + ".arz"));
            if (arzFile != null && arzFile.length == 1) {
                return arzFile[0].toPath();
            }
        }

        return null;
    }

    public List<Path> getCustomMapText(Path customMap) {
        List<Path> ret = new ArrayList<>();
        File[] resources = customMap.toAbsolutePath().toFile().listFiles(
                f -> f.isDirectory() && f.getName().equalsIgnoreCase("resources"));
        File[] text = customMap.toAbsolutePath().toFile().listFiles(
                f -> f.isDirectory() && f.getName().equalsIgnoreCase("text"));

        if (resources != null && resources.length == 1) {
            File[] files = resources[0].listFiles(f -> f.getName().matches("(?i)text.*\\.arc"));
            if (files != null && Arrays.stream(files).anyMatch(File::exists)) {
                ret.add(resources[0].toPath());
            }
        }

        if (text != null && text.length == 1) {
            File[] files = text[0].listFiles(f -> f.getName().matches("(?i)text.*\\.arc"));
            if (files != null && Arrays.stream(files).anyMatch(File::exists)) {
                ret.add(text[0].toPath());
            }
        }

        return ret;
    }

    public List<Path> getCustomMapResources(Path customMap) {
        File[] databaseDirs = customMap.toAbsolutePath().toFile().listFiles(
                f -> f.isDirectory() && f.getName().equalsIgnoreCase("resources"));

        if (databaseDirs != null && databaseDirs.length == 1) {
            File[] resources = databaseDirs[0].listFiles(f ->
                    !f.getName().toLowerCase(Locale.ROOT).equals("text.arc"));
            if (resources != null && resources.length > 0) {
                return Arrays.stream(resources).map(File::toPath).toList();
            }
        }

        return Collections.emptyList();
    }


    public String getSaveDataMainArchivedPath() {
        String archived = getSaveDataMainPath();
        if (archived == null) {
            return null;
        }
        return Paths.get(archived, Constants.ARCHIVE_DIR).toString();
    }

    public String getSaveDataUserArchivedPath() {
        String archived = getSaveDataUserPath();
        if (archived == null) {
            return null;
        }
        return Paths.get(archived, Constants.ARCHIVE_DIR).toString();
    }

    public String getSaveDataMainPath() {
        if (!SystemUtils.IS_OS_WINDOWS && installType != InstallType.STEAM) {
            prepareDevGameSaveData();
            return Paths.get(Constants.DEV_GAMEDATA, Constants.SAVEDATA, "Main").toString();
        }
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SAVEDATA, "Main").toString();
        }
        return null;
    }

    public String getSaveDataUserPath() {
        if (!SystemUtils.IS_OS_WINDOWS && installType != InstallType.STEAM) {
            prepareDevGameSaveData();
            return Paths.get(Constants.DEV_GAMEDATA, Constants.SAVEDATA, "User").toString();
        }
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SAVEDATA, "User").toString();
        }
        return null;
    }

    public String getSaveSetingsPath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            if (gamePath == null) {
                try {
                    getGamePath();
                } catch (GameNotFoundException ignored) {
                }
            }
            if (installType != InstallType.STEAM) {
                return Paths.get(Constants.DEV_GAMEDATA, Constants.SETTINGS).toString();
            }
        }

        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, Constants.SETTINGS).toString();
        }
        return null;
    }

    public List<PlayerCharacterFile> getPlayerCharacterList() {
        List<PlayerCharacterFile> ret = new ArrayList<>(getPlayerListFromPath(SaveLocation.MAIN));
        ret.addAll(getPlayerListFromPath(SaveLocation.USER));
        ret.addAll(getPlayerListFromPath(SaveLocation.ARCHIVEMAIN));
        ret.addAll(getPlayerListFromPath(SaveLocation.ARCHIVEUSER));
        ret.addAll(getPlayerListFromPath(SaveLocation.EXTERNAL));
        return ret;
    }

    public List<PlayerCharacterFile> getPlayerCharacterList(SaveLocation... locations) {
        List<PlayerCharacterFile> ret = new ArrayList<>();
        for (SaveLocation l : locations) {
            ret.addAll(getPlayerListFromPath(l));
        }
        return ret;
    }

    private List<PlayerCharacterFile> getPlayerListFromPath(SaveLocation location) {
        String savePath = locationPath(location);

        if (savePath == null) {
            return Collections.emptyList();
        }

        File directory = new File(savePath);
        List<PlayerCharacterFile> playerList = new ArrayList<>();
        if (directory.exists()) {
            for (File player : Objects.requireNonNull(directory.listFiles((File fileName) -> fileName.getName().startsWith("_")))) {
                playerList.add(new PlayerCharacterFile(player.getName().replaceAll("^_", ""), location));
            }
        } else {
            return Collections.emptyList();
        }

        return playerList;
    }

    private String getExternalSaveDataPath() {
        if (resolvePath(Paths.get(Constants.EXT_SAVEDATA)).toFile().isDirectory()) {
            logger.log(DEBUG, "External save path found: " + Paths.get(Constants.EXT_SAVEDATA));
            return Constants.EXT_SAVEDATA;
        }

        return null;
    }

    public Path playerPath(String playerName, SaveLocation saveLocation) {
        return Paths.get(locationPath(saveLocation), "_" + playerName);
    }

    private String locationPath(SaveLocation saveLocation) {
        if (saveLocation == null) {
            saveLocation = SaveLocation.MAIN;
        }

        switch (saveLocation) {
            case USER -> {
                return getSaveDataUserPath();
            }
            case EXTERNAL -> {
                return getExternalSaveDataPath();
            }
            case ARCHIVEMAIN -> {
                return getSaveDataMainArchivedPath();
            }
            case ARCHIVEUSER -> {
                return getSaveDataUserArchivedPath();
            }
            default -> {
                return getSaveDataMainPath();
            }
        }
    }

    public Path playerChr(String playerName, SaveLocation saveLocation) {
        if (SaveLocation.EXTERNAL.equals(saveLocation) && getExternalSaveDataPath() == null) {
            return null;
        }
        return Paths.get(playerPath(playerName, saveLocation).toString(), Constants.PLAYERCHR);
    }

    public Locale getGameLanguage() {
        String language;
        try {
            language = getGameOptionValue("language");
        } catch (IOException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
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

        String saveSetingsPath = getSaveSetingsPath();
        if (StringUtils.isBlank(saveSetingsPath)) {
            throw new IOException("savegame path not found");
        }

        Path optionsPath = Paths.get(saveSetingsPath, "options.txt");
        if (!Files.exists(optionsPath)) {
            throw new IOException("options.txt not found");
        }
        FileChannel optionsFile = FileChannel.open(optionsPath);
        try (BufferedReader reader = new BufferedReader(Channels.newReader(optionsFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] opt = line.split("\\s*=\\s*");
                if (opt.length == 2 && opt[1] != null) {
                    gameOptions.put(opt[0].trim(), opt[1].replace("\"", "").trim());
                } else {
                    logger.log(ERROR, "error reading game options.txt file, line ''{0}''", line);
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
            logger.log(ERROR, "", e);
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
            logger.log(ERROR, "", e);
            throw new FileNotFoundException("Text resources not found");
        }
        return textPaths;
    }

    public String getResourcesPath() throws FileNotFoundException {
        try {
            if (gamePath == null) {
                getGamePath();
            }
            return Path.of(gamePath, "Resources").toString();
        } catch (GameNotFoundException e) {
            logger.log(ERROR, "", e);
            throw new FileNotFoundException("Resources path not found");
        }
    }

    public GameVersion getInstalledVersion() {
        return installedVersion;
    }

    public InstallType getInstallType() {
        return installType;
    }

    public boolean isDlcRagnarok() {
        return dlcRagnarok;
    }

    public boolean isDlcAtlantis() {
        return dlcAtlantis;
    }

    public boolean isDlcEmbers() {
        return dlcEmbers;
    }
}

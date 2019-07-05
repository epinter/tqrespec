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

package br.com.pinter.tqrespec.tqdata;

import br.com.pinter.tqrespec.util.Constants;
import com.sun.jna.platform.win32.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GameInfo {
    private static final boolean DBG = false;

    private static GameInfo instance = null;

    private String gamePath = null;

    private GameInfo() {
    }

    public static GameInfo getInstance() {
        if (instance == null) {
            synchronized (GameInfo.class) {
                if (instance == null)
                    instance = new GameInfo();
            }
        }
        return instance;
    }

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

    private String getGameSteamLibraryPath() {
        String steamPath = Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Valve\\Steam", "SteamPath");

        Path steamappsMainPath = Paths.get(steamPath, "SteamApps", "common", "Titan Quest Anniversary Edition");
        if (Files.exists(steamappsMainPath) && Files.isDirectory(steamappsMainPath)) {
            return steamappsMainPath.toAbsolutePath().toString();
        }

        try {
            Pattern regexOuter = Pattern.compile(".*LibraryFolders.*\\{(.*)}.*", Pattern.DOTALL);
            Pattern regexInner = Pattern.compile("\\s*\"\\d\"\\s+\"([^\"]+)\".*");

            ArrayList<String> libraryFolderList = new ArrayList<>();
            String steamConfig = Files.readString(Paths.get(steamPath, "SteamApps", "libraryfolders.vdf"));

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
                Path steamappsPath = Paths.get(libraryFolder, "SteamApps", "common", "Titan Quest Anniversary Edition");
                if (Files.exists(steamappsPath) && Files.isDirectory(steamappsPath)) {
                    return steamappsPath.toAbsolutePath().toString();
                }
            }
        } catch (IOException e) {
            if (DBG) e.printStackTrace();
        }
        return null;
    }

    private String getGameGogPath() {
        try {
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\GOG.com\\Games\\1196955511", "PATH");
        } catch (Exception e) {
            return null;
        }
    }

    private String getGameDiscPath() {
        try {
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Iron Lore\\Titan Quest Immortal Throne", "Install Location");
        } catch (Exception e) {
            return null;
        }
    }

    private String getGameInstalledPath() {
        String regexGameName = "Titan Quest.*Anniversary Edition";
        String[] installedApps = new String[0];
        try {
            installedApps = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall");
        } catch (Exception e) {
            if (DBG) e.printStackTrace();
        }

        for (String app : installedApps)
            try {
                String appDisplayName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "DisplayName");
                if (appDisplayName.matches(regexGameName)) {
                    if (DBG) System.err.println("Installed: displayname found -- " + regexGameName);
                    return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                            "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + app, "InstallLocation");
                } else {
                    if (DBG)
                        System.err.println("Installed: displayname not found --- " + regexGameName + "---" + appDisplayName);
                }
            } catch (Win32Exception e) {
                if (DBG) e.printStackTrace();
            }
        return null;
    }

    private String detectGamePath() {
        String installed = this.getGameInstalledPath();
        if (StringUtils.isNotEmpty(installed)) {
            Path installedPath = Paths.get(installed);
            if (Files.exists(installedPath)) {
                if (DBG) System.err.println("Installed: found");
                return installedPath.toAbsolutePath().toString();
            }
        }

        String steamLibrary = this.getGameSteamLibraryPath();
        if (StringUtils.isNotEmpty(steamLibrary)) {
            Path steamLibraryPath = Paths.get(steamLibrary);
            if (Files.exists(steamLibraryPath)) {
                if (DBG) System.err.println("SteamLibrary: found");
                return steamLibraryPath.toAbsolutePath().toString();
            }
        }

        String gog = this.getGameGogPath();
        if (StringUtils.isNotEmpty(gog)) {
            Path gogPath = Paths.get(gog);
            if (Files.exists(gogPath)) {
                if (DBG) System.err.println("Gog: found");
                return gogPath.toAbsolutePath().toString();
            }
        }


        String disc = this.getGameDiscPath();
        if (StringUtils.isNotEmpty(disc)) {
            Path discPath = Paths.get(this.getGameDiscPath());
            if (Files.exists(discPath)) {
                if (DBG) System.err.println("Disc: found");
                return discPath.toAbsolutePath().toString();
            }
        }
        return null;
    }

    public String getGamePath() throws FileNotFoundException {
        if(DBG || !SystemUtils.IS_OS_WINDOWS) {
            return Constants.DEV_GAMEDATA;
        }
        if (StringUtils.isEmpty(gamePath)) {
            String detected = detectGamePath();
            if (StringUtils.isEmpty(detected))
                throw new FileNotFoundException("Game path not detected");
            gamePath = detected;
        }

        if(StringUtils.isEmpty(gamePath)) {
            return Constants.DEV_GAMEDATA;
        }

        return gamePath;
    }

    public String getSavePath() {
        String userHome = System.getProperty("user.home");
        String subdirectory = File.separator + Paths.get("My Games", "Titan Quest - Immortal Throne").toString();

        if (DBG || !SystemUtils.IS_OS_WINDOWS) return Constants.DEV_GAMEDATA;

        if (DBG) System.err.println("SavePath: user.home is " + userHome);
        String saveDirectory;
        try {
            saveDirectory = Shell32Util.getFolderPath(ShlObj.CSIDL_MYDOCUMENTS);
        } catch (Exception e) {
            saveDirectory = userHome;
        }

        Path savePath = Paths.get(saveDirectory + subdirectory);
        if (Files.exists(savePath)) {
            if (DBG) System.err.println("SavePath: found");
            return savePath.toAbsolutePath().toString();
        }
        return null;
    }

    public String getSaveDataMainPath() {
        if (DBG || !SystemUtils.IS_OS_WINDOWS) return Paths.get(Constants.DEV_GAMEDATA, "SaveData", "Main").toString();
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, "SaveData", "Main").toString();
        }
        return null;
    }

    public String getSaveDataUserPath() {
        if (DBG || !SystemUtils.IS_OS_WINDOWS) return Paths.get(Constants.DEV_GAMEDATA, "SaveData", "User").toString();
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return Paths.get(savePath, "SaveData", "User").toString();
        }
        return null;
    }

    public String[] getPlayerListMain() throws Exception {
        String savePath = this.getSaveDataMainPath();
        File directory = new File(savePath);
        ArrayList<String> playerList = new ArrayList<>();
        if (directory.exists()) {
            for (File player : Objects.requireNonNull(directory.listFiles((File fileName) -> fileName.getName().startsWith("_")))) {
                playerList.add(player.getName().replaceAll("^_", ""));
            }
        } else {
            throw new Exception("No player found");
        }
        String[] ret = new String[playerList.size()];
        playerList.toArray(ret);
        return ret;
    }
}

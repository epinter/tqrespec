/*
 * Copyright (C) 2017 Emerson Pinter - All Rights Reserved
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

import com.sun.jna.platform.win32.*;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GameInfo {
    private static final boolean DBG = false;

    private static GameInfo instance = null;

    private String gamePath = null;

    private String savePath = null;

    private String saveDataPath = null;

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
        try {
            FileInputStream steamConfig = new FileInputStream(new File(steamPath + "\\config\\config.vdf"));
            BufferedReader in = new BufferedReader(new InputStreamReader(steamConfig));

            Pattern regexConfig = Pattern.compile(".*\"BaseInstallFolder_\\d+\"\\s+\"([^\"]+)\".*");

            ArrayList<String> baseInstallFolderList = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null) {
                Matcher m = regexConfig.matcher(line);
                if (m.find()) {
                    baseInstallFolderList.add(m.group(1));
                    if (DBG) System.err.println("SteamLibrary: Match Found!!" + m.group(1));
                }
            }
            for (String baseInstallFolder : baseInstallFolderList) {
                Path steamappsPath = Paths.get(baseInstallFolder + "\\SteamApps\\common\\Titan Quest Anniversary Edition");
                if (Files.exists(steamappsPath)) {
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
        String installedApps[] = new String[0];
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
        if (StringUtils.isEmpty(gamePath)) {
            String detected = detectGamePath();
            if (StringUtils.isEmpty(detected))
                throw new FileNotFoundException("Game Path not detected");
            gamePath = detected;
        }
        return gamePath;
    }

    public String getSavePath() {
        String userHome = System.getProperty("user.home");
        String subdirectory = "\\My Games\\Titan Quest - Immortal Throne";

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
        if (DBG) return "d:\\dev\\save\\SaveData\\Main";
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return savePath + "\\SaveData\\Main";
        }
        return null;
    }

    public String getSaveDataUserPath() {
        if (DBG) return "d:\\dev\\save\\SaveData\\User";
        String savePath = getSavePath();
        if (StringUtils.isNotEmpty(savePath)) {
            return savePath + "\\SaveData\\User";
        }
        return null;
    }

    public String[] getPlayerListMain() throws Exception {
        String savePath = this.getSaveDataMainPath();
        File directory = new File(savePath);
        ArrayList<String> playerList = new ArrayList<>();
        if (directory.exists()) {
            for (File player : directory.listFiles((File fileName) -> fileName.getName().startsWith("_"))) {
                playerList.add(player.getName().replaceAll("^_", ""));
            }
        } else {
            throw new Exception("No player found");
        }
        String ret[] = new String[playerList.size()];
        playerList.toArray(ret);
        return ret;
    }
}

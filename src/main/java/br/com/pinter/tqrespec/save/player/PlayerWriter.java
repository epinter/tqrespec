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

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.save.FileDataHolder;
import br.com.pinter.tqrespec.save.FileDataMap;
import br.com.pinter.tqrespec.save.FileWriter;
import br.com.pinter.tqrespec.save.Platform;
import br.com.pinter.tqrespec.save.stash.StashLoader;
import br.com.pinter.tqrespec.save.stash.StashWriter;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerWriter extends FileWriter {
    private static final System.Logger logger = Log.getLogger(PlayerWriter.class.getName());
    @Inject
    private CurrentPlayerData saveData;

    @Inject
    private GameInfo gameInfo;

    @Override
    public int getCrcOffset() {
        return 0;
    }

    @Override
    public boolean isCreateCrc() {
        return false;
    }

    @Override
    protected FileDataHolder getSaveData() {
        return saveData;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean backupSaveGame(String fileName, String playerName) throws IOException {
        File backupDirectory = new File(gameInfo.getSavePath(), Constants.BACKUP_DIRECTORY);
        Path player = Paths.get(fileName);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HH");
        String ts = df.format(new Date());
        boolean fullBackup = Settings.getAlwaysFullBackup();
        File destPlayerZip = new File(backupDirectory, String.format("%s%s_%s.zip", playerName, fullBackup ? "-fullbackup" : "", ts));

        //doesn't overwrite previous backup
        if (destPlayerZip.exists() && destPlayerZip.length() > 1) {
            return true;
        }

        if (!backupDirectory.exists() && !backupDirectory.mkdir()) {
            throw new IOException("Unable to create backup directory");
        }

        if (backupDirectory.canWrite()) {
            URI zipUri = URI.create("jar:" + destPlayerZip.toURI().toString());
            HashMap<String, String> zipCreateOptions = new HashMap<>();
            zipCreateOptions.put("create", "true");

            try (FileSystem zipFs = FileSystems.newFileSystem(zipUri, zipCreateOptions)) {
                final Path root = zipFs.getPath("/");
                if (fullBackup) {
                    Files.walkFileTree(player.getParent(), new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path subPath = file.subpath(player.getParent().getNameCount() - 1, file.getNameCount());
                            final Path dest = zipFs.getPath(root.toString(), subPath.toString());
                            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (player.getParent().getNameCount() == dir.getNameCount()) {
                                return FileVisitResult.CONTINUE;
                            }
                            Path subPath = dir.subpath(player.getParent().getNameCount() - 1, dir.getNameCount());

                            final Path createDir = zipFs.getPath(root.toString(), subPath.toString());
                            Files.createDirectories(createDir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.createDirectories(zipFs.getPath(root.toString(), "/" + player.getName(player.getNameCount() - 2)));
                    Path destPlayer = zipFs.getPath("/" + player.getName(player.getNameCount() - 2) + "/" + player.getFileName());

                    Path srcDxb = Paths.get(saveData.getPlayerPath().toString(), "winsys.dxb");
                    Path destDxb = zipFs.getPath("/" + player.getName(player.getNameCount() - 2) + "/winsys.dxb");
                    Path srcDxg = Paths.get(saveData.getPlayerPath().toString(), "winsys.dxg");
                    Path destDxg = zipFs.getPath("/" + player.getName(player.getNameCount() - 2) + "/winsys.dxg");

                    Files.copy(player, destPlayer, StandardCopyOption.REPLACE_EXISTING);
                    copyFileTimes(player, destPlayer);

                    if(Files.exists(srcDxb)) {
                        Files.copy(srcDxb, destDxb, StandardCopyOption.REPLACE_EXISTING);
                        copyFileTimes(srcDxb, destDxb);
                    }

                    if(Files.exists(srcDxg)) {
                        Files.copy(srcDxg, destDxg, StandardCopyOption.REPLACE_EXISTING);
                        copyFileTimes(srcDxg, destDxg);
                    }
                }

                return true;
            } catch (IOException e) {
                logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
                return false;
            }
        }

        return false;
    }

    private void copyFileTimes(Path src, Path dst) throws IOException {
        Files.setAttribute(dst, "creationTime", Files.getAttribute(src, "creationTime"));
        Files.setAttribute(dst, "lastModifiedTime", Files.getAttribute(src, "lastModifiedTime"));
        Files.setAttribute(dst, "lastAccessTime", Files.getAttribute(src, "lastAccessTime"));
    }

    public boolean backupCurrent() throws IOException {
        String playerChr = saveData.getPlayerChr().toString();
        String playerName = saveData.getPlayerName();
        return this.backupSaveGame(playerChr, playerName);
    }

    public boolean save() {
        if (State.get().getSaveInProgress() != null && State.get().getSaveInProgress()) {
            return false;
        }
        State.get().setSaveInProgress(true);
        Path chrPath = saveData.getPlayerChr();
        String rootPath = chrPath.getRoot()+chrPath.subpath(0, chrPath.getNameCount()-1).toString();
        String playerChr = chrPath.getFileName().toString();
        try {
            this.writeBuffer(rootPath, playerChr);
            State.get().setSaveInProgress(false);
            return true;
        } catch (IOException e) {
            State.get().setSaveInProgress(false);
            throw new UnhandledRuntimeException("Error saving character", e);
        }
    }

    public void copyCurrentSave(String toPlayerName) throws IOException {
        State.get().setSaveInProgress(true);
        try {
            String path;
            if (saveData.isCustomQuest()) {
                path = gameInfo.getSaveDataUserPath();
            } else {
                path = gameInfo.getSaveDataMainPath();
            }

            String fromPlayerName = saveData.getPlayerName();

            Path playerSaveDirSource = Paths.get(path, "_" + fromPlayerName);
            Path playerSaveDirTarget = Paths.get(path, "_" + toPlayerName);

            if (Files.exists(playerSaveDirTarget)) {
                State.get().setSaveInProgress(false);
                throw new FileAlreadyExistsException("Target Directory already exists");
            }
            FileDataMap fileDataMap = (FileDataMap) saveData.getDataMap().deepClone();
            fileDataMap.setString("myPlayerName", toPlayerName, true);

            Util.copyDirectoryRecurse(playerSaveDirSource, playerSaveDirTarget, false);
            writeBuffer(playerSaveDirTarget.toString(), "Player.chr", fileDataMap);

            StashLoader stashLoader = new StashLoader();
            if (stashLoader.loadStash(playerSaveDirTarget, toPlayerName)) {
                StashWriter stashWriter = new StashWriter(stashLoader.getSaveData());
                stashWriter.save();
            }
        }catch (IOException e) {
            logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            State.get().setSaveInProgress(false);
            throw new IOException(e);
        }

        State.get().setSaveInProgress(false);
    }
}

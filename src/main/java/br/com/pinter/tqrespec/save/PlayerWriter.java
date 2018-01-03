/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

/*    This file is part of TQ Respec.

    TQ Respec is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package br.com.pinter.tqrespec.save;

import br.com.pinter.tqrespec.Constants;
import br.com.pinter.tqrespec.GameInfo;
import br.com.pinter.tqrespec.Settings;
import br.com.pinter.tqrespec.Util;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class PlayerWriter {
    public PlayerWriter() {
    }

    private boolean backupSaveGame(String fileName, String playerName,boolean fullBackup) {
        File backupDirectory = new File(String.format("%s\\%s", GameInfo.getInstance().getSavePath(), Constants.BACKUP_DIRECTORY));
        Path player = Paths.get(fileName);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HH");
        String ts = df.format(new Date());
        File destPlayerZip = new File(String.format("%s\\%s%s_%s.zip", backupDirectory.toPath().toString(), playerName, fullBackup?"-fullbackup":"",ts));

        //doesn't overwrite previous backup
        if(destPlayerZip.exists() && destPlayerZip.length()> 1) {
            return true;
        }
        if (!backupDirectory.exists()) {
            backupDirectory.mkdir();
        }
        if (backupDirectory.canWrite()) {
            URI zipUri = URI.create("jar:" + destPlayerZip.toURI().toString());
            try (FileSystem zipFs = FileSystems.newFileSystem(zipUri, new HashMap<String, String>() {{
                put("create", "true");
            }})) {
                final Path root = zipFs.getPath("/");
                if (fullBackup || Settings.getAlwaysFullBackup()) {
                    Files.walkFileTree(player.getParent(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path subPath = file.subpath(player.getParent().getNameCount()-1, file.getNameCount());
                            final Path dest = zipFs.getPath(root.toString(), subPath.toString());
                            Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            if (player.getParent().getNameCount() == dir.getNameCount()) {
                                return FileVisitResult.CONTINUE;
                            }
                            Path subPath = dir.subpath(player.getParent().getNameCount()-1, dir.getNameCount());

                            final Path createDir = zipFs.getPath(root.toString(), subPath.toString());
                            if (Files.notExists(createDir)) {
                                Files.createDirectories(createDir);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.createDirectories(zipFs.getPath(root.toString(),"/"+player.getName(player.getNameCount()-2)));

                    Path destPlayer = zipFs.getPath("/" + player.getName(player.getNameCount()-2)+ "/" + player.getFileName());
                    Path createdBackup = Files.copy(player, destPlayer, StandardCopyOption.REPLACE_EXISTING);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    public boolean backupCurrent() {
        String playerChr = PlayerData.getInstance().getPlayerChr().toString();
        String playerName = PlayerData.getInstance().getPlayerName();
        if(this.backupSaveGame(playerChr,playerName,false)) {
            return true;
        }
        return false;
    }

    public boolean saveCurrent() throws IOException {
        if(PlayerData.getInstance().getSaveInProgress()!=null && PlayerData.getInstance().getSaveInProgress()) {
            return false;
        }
        PlayerData.getInstance().setSaveInProgress(true);
        String playerChr = PlayerData.getInstance().getPlayerChr().toString();
        try {
            this.writeBuffer(playerChr);
            PlayerData.getInstance().setSaveInProgress(false);
            return true;
        } catch (IOException e) {
            PlayerData.getInstance().setSaveInProgress(false);
            e.printStackTrace();
            throw  e;
        }
    }

    private void writeBuffer(String filename) throws IOException {
        PlayerData.getInstance().getBuffer().rewind();
        List<Integer> changedOffsets = new ArrayList<>(PlayerData.getInstance().getChanges().keySet());
        Collections.sort(changedOffsets);

        File out = new File(filename);
        FileChannel outChannel = new FileOutputStream(out).getChannel();

        for (int offset : changedOffsets) {
            int rawCount = offset - PlayerData.getInstance().getBuffer().position();
            PlayerData.getInstance().getBuffer().limit(rawCount +
                    PlayerData.getInstance().getBuffer().position()
            );
            outChannel.write(PlayerData.getInstance().getBuffer());
            PlayerData.getInstance().getBuffer().limit(PlayerData.getInstance().getBuffer().capacity());
            byte c[] = PlayerData.getInstance().getChanges().get(offset);
            outChannel.write(ByteBuffer.wrap(c));
            int previousValueLength = PlayerData.getInstance().getValuesLengthIndex().get(offset);
            PlayerData.getInstance().getBuffer().position(
                    PlayerData.getInstance().getBuffer().position() + previousValueLength);
        }

        while (true) {
            if (outChannel.write(PlayerData.getInstance().getBuffer()) <= 0) break;
        }

        PlayerData.getInstance().getBuffer().rewind();
        outChannel.force(false);
        outChannel.close();
    }
}

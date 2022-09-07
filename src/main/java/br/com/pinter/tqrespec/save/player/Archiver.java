/*
 * Copyright (C) 2022 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.save.player;

import br.com.pinter.tqrespec.save.SaveLocation;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.PlayerCharacter;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Archiver {
    @Inject
    private GameInfo gameInfo;

    public void archive(PlayerCharacter playerCharacter) throws IOException {
        Path src = playerCharacter.getPath();
        Path dst;

        Path main = Paths.get(gameInfo.getSaveDataMainPath());
        Path mainArchive = Paths.get(gameInfo.getSaveDataMainArchivedPath());
        if(main.toFile().exists() && !mainArchive.toFile().exists()) {
            Files.createDirectory(mainArchive);
        }

        Path user = Paths.get(gameInfo.getSaveDataUserPath());
        Path userArchive = Paths.get(gameInfo.getSaveDataUserArchivedPath());
        if(user.toFile().exists() && !userArchive.toFile().exists()) {
            Files.createDirectory(userArchive);
        }

        if (SaveLocation.MAIN.equals(playerCharacter.getLocation())) {
            dst = gameInfo.playerPath(playerCharacter.getName(), SaveLocation.ARCHIVEMAIN);
        } else if (SaveLocation.USER.equals(playerCharacter.getLocation())) {
            dst = gameInfo.playerPath(playerCharacter.getName(), SaveLocation.ARCHIVEUSER);
        } else {
            throw new IOException("Invalid savegame location");
        }

        if(!dst.startsWith(src.getParent()) || !dst.startsWith(gameInfo.getSavePath())) {
            throw new IOException("Error archiving character, invalid path");
        }

        Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
    }

    public void unarchive(PlayerCharacter playerCharacter) throws IOException {
        Path src = playerCharacter.getPath();
        Path dst;

        if (SaveLocation.ARCHIVEMAIN.equals(playerCharacter.getLocation())) {
            dst = gameInfo.playerPath(playerCharacter.getName(), SaveLocation.MAIN);
        } else if (SaveLocation.ARCHIVEUSER.equals(playerCharacter.getLocation())) {
            dst = gameInfo.playerPath(playerCharacter.getName(), SaveLocation.USER);
        } else {
            throw new IOException("Invalid savegame location");
        }

        if(!src.startsWith(dst.getParent()) || !dst.startsWith(gameInfo.getSavePath())) {
            throw new IOException("Error unarchiving character, invalid path");
        }

        Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
    }
}

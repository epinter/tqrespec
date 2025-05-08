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

import br.com.pinter.tqdatabase.Database;
import br.com.pinter.tqdatabase.Player;
import br.com.pinter.tqdatabase.Skills;
import br.com.pinter.tqdatabase.Teleports;
import br.com.pinter.tqrespec.core.GameNotFoundException;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Path;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@Singleton
public class Db {
    private static final System.Logger logger = Log.getLogger(Db.class);
    private Database database;
    private Db.Platform platform = Db.Platform.WINDOWS;

    @Inject
    private GameInfo gameInfo;

    public Db.Platform getPlatform() {
        return platform;
    }

    public void initialize() {
        try {
            if (database == null) {
                database = new Database(gameInfo.getDatabasePath());
                if (gameInfo.getInstallType().equals(InstallType.UNKNOWN)
                        && !Path.of(gameInfo.getGamePath(), "FORCE_WINDOWS.txt").toFile().exists()
                        && (recordExists("Records\\InGameUI\\Player Character\\Mobile\\CharStatsMobile.dbr")
                        || recordExists("Records\\xpack\\ui\\hud\\hud_mobile.dbr"))) {
                    logger.log(INFO,
                            "Mobile database detected. If this database is from Windows version, create the file to force detection: "
                                    + Path.of(gameInfo.getGamePath(), "FORCE_WINDOWS.txt").toFile());
                    platform = Platform.MOBILE;
                }
            }
        } catch (IOException | GameNotFoundException e) {
            logger.log(ERROR, "", e);
            throw new UnhandledRuntimeException("Error loading database.", e);
        }
    }

    public Skills skills() {
        initialize();
        return database.skills();
    }

    public Player player() {
        initialize();
        return database.player();
    }

    public Teleports teleports() {
        initialize();
        return database.teleports();
    }

    public void preloadAll() {
        initialize();
        database.preloadAll();
    }

    public boolean recordExists(String recordId) {
        return database.recordExists(recordId);
    }

    public enum Platform {
        WINDOWS,
        MOBILE
    }
}

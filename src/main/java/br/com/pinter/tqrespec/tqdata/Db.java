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

import br.com.pinter.tqdatabase.Database;
import br.com.pinter.tqdatabase.Player;
import br.com.pinter.tqdatabase.Skills;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class Db {
    private static final Logger logger = Log.getLogger();

    private Database database;

    public void initialize() {
        try {
            if (database == null) {
                database = new Database(String.format("%s/Database/database.arz", GameInfo.getInstance().getGamePath()));
            }
        } catch (IOException e) {
            if (Log.isDebugEnabled())
                logger.log(Level.SEVERE, Constants.ERROR_MSG_EXCEPTION, e);
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

    public void preloadAll() {
        initialize();
        database.preloadAll();
    }
}

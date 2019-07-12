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
import br.com.pinter.tqdatabase.Skills;

import br.com.pinter.tqdatabase.models.Skill;
import com.google.inject.Singleton;
import java.io.IOException;

@Singleton
public class Db {
    private Database db;

    public void initialize() {
        try {
            if(db == null) {
                db = new Database(String.format("%s/Database/database.arz", GameInfo.getInstance().getGamePath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error loading database.",e);
        }
    }

    public Skills skills() {
        initialize();
        return db.skills();
    }

    public void preloadAll() {
        initialize();
        db.preloadAll();
    }
}

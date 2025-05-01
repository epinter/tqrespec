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

import br.com.pinter.tqdatabase.Resources;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static java.lang.System.Logger.Level.ERROR;

@Singleton
public class GameResources {
    private static final System.Logger logger = Log.getLogger(GameResources.class);

    @Inject
    private GameInfo gameInfo;

    public Map<String, byte[]> getAllFonts() {
        try {
            Resources fonts = new Resources(Path.of(gameInfo.getResourcesPath(), "Fonts.arc").toString());
            return fonts.getAllFonts();
        } catch (IOException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
            return Collections.emptyMap();
        }
    }
}

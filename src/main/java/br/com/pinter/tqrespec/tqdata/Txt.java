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

import br.com.pinter.tqdatabase.Text;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@Singleton
public class Txt {
    private static final System.Logger logger = Log.getLogger(Txt.class);
    private final List<Text> text = new ArrayList<>();
    private final List<Text> textMod = new ArrayList<>();

    @Inject
    private GameInfo gameInfo;

    public void initialize() {
        try {
            if (text.isEmpty()) {
                text.add(new Text(gameInfo.getTextPath(), Constants.LOCALE_TEXT.get(State.get().getLocale())));
                if (!State.get().getLocale().equals(Locale.ENGLISH)) {
                    text.add(new Text(gameInfo.getTextPath(), Constants.LOCALE_TEXT.get(Locale.ENGLISH), false));
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(ERROR, Constants.ERROR_MSG_EXCEPTION, e);
        }
    }

    public String getString(String tag) {
        initialize();
        if (StringUtils.isBlank(tag)) {
            return tag;
        }
        try {
            for (Text t : textMod) {
                String str = t.getString(tag);
                if (!StringUtils.isBlank(str) && !tag.equals(str)) {
                    return str;
                }
            }
            for (Text t : text) {
                String str = t.getString(tag);
                if (!StringUtils.isBlank(str) && !tag.equals(str)) {
                    return str;
                }
            }

        } catch (IOException ignore) {
            return null;
        }
        return null;
    }

    public void preload() {
        initialize();
        try {
            for (Text t : text) {
                t.preload();
            }
            for (Text t : textMod) {
                t.preload();
            }
        } catch (IOException e) {
            throw new UnhandledRuntimeException("Error loading text resource", e);
        }
    }

    public String getCapitalizedString(String tag) {
        return WordUtils.capitalize(getString(tag));
    }

    public boolean isTagStringValid(String tag) {
        String str = getString(tag);
        return !StringUtils.isBlank(tag) && !StringUtils.isBlank(str) && !tag.equals(str);
    }

    public void loadMod(Path path) {
        String[] paths = new String[]{path.toAbsolutePath().toString()};

        textMod.add(new Text(paths, Constants.LOCALE_TEXT.get(State.get().getLocale()), false));
        text.getFirst().clearCache();
        preload();
    }

    public void unloadMods() {
        textMod.clear();
        text.getFirst().clearCache();
        preload();
    }
}

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

package br.com.pinter.tqrespec.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutputStreamLog extends OutputStream {
    private StringBuilder stringBuilder = new StringBuilder();
    private StringBuilder stringBuilderArray = new StringBuilder();
    private Level level;
    private Logger logger = Logger.getLogger(OutputStreamLog.class.getName());

    public OutputStreamLog(Level level) {
        this.level = level;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int max = Math.min(b.length,off+len);

        for (int i = off; i < max; i++) {
            char c = (char) b[i];

            if (c != '\r' && c != '\n') {
                stringBuilderArray.append(c);
            }

            if (c == '\r' || c == '\n' || i == max - 1) {
                logger.log(level, "{0}", stringBuilderArray);
                stringBuilderArray = new StringBuilder();
            }
        }
    }

    @Override
    public void write(int b) {
        char c = (char) b;

        if (c == '\r' || c == '\n') {
            if (stringBuilder.length() > 0) {
                logger.log(level, "{0}", stringBuilder);
                stringBuilder = new StringBuilder();
            }
        } else {
            stringBuilder.append(c);
        }
    }
}

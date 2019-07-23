/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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

/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.logging;

import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.util.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.*;

public class Log {
    private static boolean debugEnabled = false;

    private Log() {
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static Logger getLogger() {
        return LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public static void setupGlobalLogging() {
        Logger setupLogger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(Constants.LOGFILE, false);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            throw new UnhandledRuntimeException(e);
        }
        if (debugEnabled) {
            setupLogger.setLevel(Level.FINE);
        }
        setupLogger.setUseParentHandlers(false);
        setupLogger.addHandler(fileHandler);

        System.setOut(Log.redirectPrintStreamToLog(Level.INFO));
        System.setErr(Log.redirectPrintStreamToLog(Level.SEVERE));
    }

    public static PrintStream redirectPrintStreamToLog(Level level) {
        return new PrintStream(new OutputStream() {
            private StringBuilder stringBuilder = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                char c = (char) b;

                if(c == '\r' || c == '\n') {
                    if(stringBuilder.length() > 0) {
                        getLogger().log(level,() -> stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                    }
                } else {
                    stringBuilder.append(c);
                }
            }
        });
    }
}

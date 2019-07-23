/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.logging;

import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.core.UnhandledRuntimeException;
import br.com.pinter.tqrespec.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.*;

public class Log {
    private Log() {
    }

    public static System.Logger getLogger(String name) {
        return System.getLogger(name);
    }

    public static FileHandler getLogFileHandler() {
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(Constants.LOGFILE, false);
            fileHandler.setFormatter(new SimpleFormatter());

        } catch (IOException e) {
            throw new UnhandledRuntimeException(e);
        }
        return fileHandler;
    }

    public static void setupGlobalLogging() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");

        rootLogger.addHandler(getLogFileHandler());
        for (Handler h : rootLogger.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                rootLogger.removeHandler(h);
            }
        }

        if (State.get().getDebugPrefix().containsKey("*")) {
            Level level = State.get().getDebugPrefix().get("*");
            if (level != null) {
                rootLogger.setLevel(level);
            }
        }
        System.setOut(new PrintStream(new OutputStreamLog(Level.INFO)));
        System.setErr(new PrintStream(new OutputStreamLog(Level.SEVERE)));
    }

}

/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.logging;

import br.com.pinter.tqrespec.core.UnhandledException;
import br.com.pinter.tqrespec.util.Constants;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
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
            throw new UnhandledException(e);
        }
        setupLogger.setUseParentHandlers(false);
        setupLogger.addHandler(fileHandler);
    }
}

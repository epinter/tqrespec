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

package br.com.pinter.tqrespec.logging;

import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class JULLogger implements System.Logger {
    private final Logger logger;
    private String name;

    public JULLogger(String name) {
        this.name = name;
        this.logger = Logger.getLogger(name);

        for (String prefix : State.get().getDebugPrefix().keySet()) {
            if (name.startsWith(prefix)) {
                java.util.logging.Level levelPrefix = State.get().getDebugPrefix().get(prefix);

                if (levelPrefix != null) {
                    this.logger.setLevel(levelPrefix);
                    return;
                }
            }
        }

        if (State.get().getDebugPrefix().containsKey("*")) {
            //a cli parameter was passed to set debug on all packages, ignore from constant
            java.util.logging.Level levelPrefix = State.get().getDebugPrefix().get("*");

            if (levelPrefix != null) {
                this.logger.setLevel(levelPrefix);
                return;
            }
        }

        List<String> levels = Arrays.asList(StringUtils.split(Constants.LOGLEVELS, ";"));
        levels.forEach(f -> {
            String[] rule = StringUtils.split(f, '=');
            if (rule.length == 2 && StringUtils.isNotBlank(rule[1]) && name.startsWith(rule[0] + ".")) {
                this.logger.setLevel(java.util.logging.Level.parse(rule[1]));
            }
        });
        if (this.logger.getLevel() == null) {
            this.logger.setLevel(java.util.logging.Level.SEVERE);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isLoggable(Level level) {
        switch (level) {
            case OFF:
                return false;
            case TRACE:
                return logger.isLoggable(java.util.logging.Level.FINER);
            case DEBUG:
                return logger.isLoggable(java.util.logging.Level.FINE);
            case INFO:
                return logger.isLoggable(java.util.logging.Level.INFO);
            case WARNING:
                return logger.isLoggable(java.util.logging.Level.WARNING);
            case ERROR:
                return logger.isLoggable(java.util.logging.Level.SEVERE);
            case ALL:
            default:
                return true;
        }
    }

    @Override
    public void log(Level level, String format, Object... params) {
        this.log(level, null, format, params);
    }

    @Override
    public void log(Level level, Object obj) {
        this.log(level, obj.toString());
    }

    @Override
    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }

        switch (level) {
            case TRACE:
                logger.log(java.util.logging.Level.FINER, msg);
                break;
            case DEBUG:
                logger.log(java.util.logging.Level.FINE, msg);
                break;
            case WARNING:
                logger.log(java.util.logging.Level.WARNING, msg);
                break;
            case ERROR:
                logger.log(java.util.logging.Level.SEVERE, msg);
                break;
            case ALL:
            default:
                logger.log(java.util.logging.Level.INFO, msg);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable thrown) {
        this.log(level, null, msg, thrown);
    }

    @Override
    public void log(Level level, ResourceBundle resourceBundle, String msg, Throwable throwable) {
        if (!isLoggable(level)) {
            return;
        }

        switch (level) {
            case TRACE:
                logger.log(java.util.logging.Level.FINER, msg, throwable);
                break;
            case DEBUG:
                logger.log(java.util.logging.Level.FINE, msg, throwable);
                break;
            case WARNING:
                logger.log(java.util.logging.Level.WARNING, msg, throwable);
                break;
            case ERROR:
                logger.log(java.util.logging.Level.SEVERE, msg, throwable);
                break;
            case ALL:
            default:
                logger.log(java.util.logging.Level.INFO, msg, throwable);
        }
    }

    @Override
    public void log(Level level, ResourceBundle resourceBundle, String format, Object... params) {
        String msg = MessageFormat.format(format, params);

        this.log(level, msg);
    }
}

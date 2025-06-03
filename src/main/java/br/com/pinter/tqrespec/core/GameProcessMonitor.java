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

package br.com.pinter.tqrespec.core;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import static br.com.pinter.tqrespec.util.Constants.PROCESS_SCAN_INTERVAL_MS;

public class GameProcessMonitor implements Runnable {
    private int interrupted = 0;
    private final String directory;

    public GameProcessMonitor(String directory) {
        this.directory = directory;
    }

    private void monitor() {
        AtomicBoolean foundRunning = new AtomicBoolean(false);
        while (true) {
            try {
                if (interrupted > 5000) {
                    break;
                }

                foundRunning.set(false);
                if (SystemUtils.IS_OS_WINDOWS) {
                    ProcessHandle.allProcesses().forEach(p -> {
                        String command = p.info().command().orElse(null);
                        if (command != null) {
                            Path processCommand = Paths.get(command);
                            Path gamePath = Paths.get(directory);
                            if (processCommand.getFileName().toString().equalsIgnoreCase("tq.exe")
                                    || (processCommand.startsWith(gamePath) &&
                                    processCommand.getFileName().toString().toLowerCase().endsWith(".exe")
                                    && !processCommand.getFileName().toString().equalsIgnoreCase("tqrespec.exe"))) {
                                foundRunning.set(true);
                            }
                        }
                    });
                } else {
                    ProcessHandle.allProcesses().forEach(p -> {
                        String command = p.info().commandLine().orElse(null);
                        Path gamePath = Paths.get(directory);
                        if (command != null && command.matches("(?i).*" + gamePath.toAbsolutePath() + "\\b.*")
                                && command.matches("(?i).*\\btq.exe\\b.*")) {
                            foundRunning.set(true);
                        }
                    });
                }
                State.get().setGameRunning(foundRunning.get());
                //noinspection BusyWait
                Thread.sleep(PROCESS_SCAN_INTERVAL_MS);
            } catch (InterruptedException ignored) {
                //ignored
                interrupted++;
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void run() {
        monitor();
    }
}

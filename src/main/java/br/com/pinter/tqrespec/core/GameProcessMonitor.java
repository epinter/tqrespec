/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static br.com.pinter.tqrespec.util.Constants.PROCESS_SCAN_INTERVAL_MS;

public class GameProcessMonitor implements Runnable {
    private int interrupted = 0;

    private String directory;

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
                ProcessHandle.allProcesses().forEach(p -> {
                    String command = p.info().command().orElse(null);
                    if (Objects.nonNull(command)) {
                        Path processCommand = Paths.get(command);
                        Path gamePath = Paths.get(directory);
                        if (processCommand.startsWith(gamePath) &&
                                processCommand.getFileName().toString().toLowerCase().endsWith(".exe")
                                && !processCommand.getFileName().toString().toLowerCase().equals("tqrespec.exe")) {
                            foundRunning.set(true);
                        }
                    }
                });
                State.get().setGameRunning(foundRunning.get());
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

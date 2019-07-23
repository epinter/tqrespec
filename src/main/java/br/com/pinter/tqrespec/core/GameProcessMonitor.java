/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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
                                processCommand.getFileName().toString().toLowerCase().endsWith(".exe")) {
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

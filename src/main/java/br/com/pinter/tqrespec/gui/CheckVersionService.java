/*
 * Copyright (C) 2021 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.util.Util;
import br.com.pinter.tqrespec.util.Version;
import com.google.inject.Inject;
import javafx.application.HostServices;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

public class CheckVersionService extends Service<Version> {
    private final String url;
    private final String currentVersion;
    private final Control control;
    @Inject
    private HostServices hostServices;

    public CheckVersionService(String currentVersion, String url, Control control) {
        this.url = url;
        this.currentVersion = currentVersion;
        this.control = control;
    }

    @Override
    protected Task<Version> createTask() {
        Task<Version> task = new Task<>() {
            @Override
            protected Version call() {
                Version version = new Version(currentVersion);
                version.checkNewerVersion(url);
                //new version available (-1 our version is less than remote, 0 equal, 1 greater, -2 error checking
                return version;
            }
        };

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent e) -> {
            Version version = (Version) e.getSource().getValue();

            if (version == null || version.getLastCheck() != -1 || StringUtils.isEmpty(version.getUrlPage())) {
                control.setDisable(true);
                return;
            }
            ((Hyperlink) control).setOnAction(event -> {
                final Task<Void> openUrl = new Task<>() {
                    @Override
                    public Void call() {
                        try {
                            hostServices.showDocument(new URI(version.getUrlPage()).toString());
                        } catch (URISyntaxException ignored) {
                            //ignored
                        }
                        return null;
                    }
                };
                new Thread(openUrl).start();
            });
            ((Hyperlink) control).setText(Util.getUIMessage("about.newversion"));
        });

        return task;
    }

}

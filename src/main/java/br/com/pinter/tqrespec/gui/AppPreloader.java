/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class AppPreloader extends Preloader {
    private ProgressBar bar;
    private Stage stage;
    private boolean noLoadingProgress = true;

    private Scene createPreloaderScene() throws IOException {
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 370, 210);
        pane.getStylesheets().add("/fxml/preloader.css");
        pane.getStyleClass().add("bg-container");

        bar = new ProgressBar(0.1);
        bar.getStyleClass().add("bar");
        pane.setCenter(bar);

        Label title = new Label();
        title.setText("TQRespec");
        title.getStyleClass().add("tq-bigtitle");
        pane.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.getStyleClass().add("indicator");
        pane.setBottom(indicator);

        return scene;

    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        this.stage.getIcons().addAll(new Image("icon/icon64.png"), new Image("icon/icon32.png"), new Image("icon/icon16.png"));
        this.stage.setScene(createPreloaderScene());

        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            this.stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            this.stage.initStyle(StageStyle.UNDECORATED);
        }

        this.stage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        //application loading progress is rescaled to be first 50%
        //Even if there is nothing to load 0% and 100% events can be
        // delivered
        if (pn.getProgress() != 1.0 || !noLoadingProgress) {
            bar.setProgress(pn.getProgress() / 2);
            if (pn.getProgress() > 0) {
                noLoadingProgress = false;
            }
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        //ignore, hide after application signals it is ready
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        if (pn instanceof ProgressNotification) {
            //expect application to send us progress notifications
            //with progress ranging from 0 to 1.0
            double v = ((ProgressNotification) pn).getProgress();
            if (!noLoadingProgress) {
                //if we were receiving loading progress notifications
                //then progress is already at 50%.
                //Rescale application progress to start from 50%
                v = 0.5 + v / 2;
            }
            bar.setProgress(v);
        } else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            stage.hide();
        }
    }
}

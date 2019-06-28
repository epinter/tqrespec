/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.core;

import javafx.application.Application;
import javafx.application.Preloader;
import javafx.stage.Stage;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

public class FxApplication extends Application {
    public static void main(String... args) {
        System.setProperty("javafx.preloader", "br.com.pinter.tqrespec.gui.AppPreloader");
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        notifyPreloader(new Preloader.ProgressNotification(0.1));
        SeContainerInitializer seContainerInitializer = SeContainerInitializer.newInstance();
        SeContainer seContainer = seContainerInitializer.initialize();
        seContainer.select(Main.class).get().start(stage, this);
    }
}

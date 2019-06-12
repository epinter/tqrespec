/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
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
    public void handleApplicationNotification(PreloaderNotification pn) {
        if (pn instanceof ProgressNotification) {
            bar.setProgress(((ProgressNotification) pn).getProgress());
        } else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            stage.hide();
        }
    }
}

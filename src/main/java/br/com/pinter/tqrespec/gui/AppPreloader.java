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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.util.Build;
import br.com.pinter.tqrespec.util.Constants;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class AppPreloader extends Preloader {
    private ProgressBar bar;
    private Stage stage;

    private Scene createPreloaderScene() {
        ResourceHelper.loadFonts();

        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, Constants.UI.PRELOADER_WIDTH, Constants.UI.PRELOADER_HEIGHT);
        scene.getStylesheets().add(ResourceHelper.getResource(Constants.UI.PRELOADER_CSS));
        scene.getStylesheets().add(ResourceHelper.getResource(Constants.UI.DEFAULT_FONT_CSS));
        pane.getStyleClass().add(Constants.UI.PRELOADER_PANE_STYLE);

        Label title = new Label();
        title.setText(Constants.APPNAME);
        title.getStyleClass().add(Constants.UI.PRELOADER_TITLE_STYLE);
        StackPane topPane = new StackPane(title);
        topPane.getStyleClass().add(Constants.UI.PRELOADER_TOP_STYLE);
        StackPane.setAlignment(title, Pos.CENTER);
        pane.setTop(topPane);

        bar = new ProgressBar(0.1);
        bar.getStyleClass().add(Constants.UI.PRELOADER_BAR_STYLE);
        bar.setMaxWidth(Constants.UI.PRELOADER_WIDTH - 30d);
        pane.setCenter(bar);

        Label versionText = new Label(Build.version());
        versionText.getStyleClass().add(Constants.UI.PRELOADER_VERSION_STYLE);
        StackPane bottomPane = new StackPane(versionText);
        bottomPane.getStyleClass().add(Constants.UI.PRELOADER_BOTTOM_STYLE);
        StackPane.setAlignment(versionText, Pos.BOTTOM_RIGHT);
        pane.setBottom(bottomPane);

        return scene;

    }

    @Override
    public void start(Stage stage) throws IOException {
        this.stage = stage;
        stage.getIcons().addAll(ResourceHelper.getAppIcons());
        stage.setScene(createPreloaderScene());
        stage.setTitle(Build.title());

        if (SystemUtils.IS_OS_WINDOWS) {
            stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            stage.initStyle(StageStyle.UNDECORATED);
        }

        stage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        if (pn instanceof ProgressNotification progressNotification) {
            bar.setProgress(progressNotification.getProgress());
        } else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            stage.hide();
        }
    }
}

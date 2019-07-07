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

package br.com.pinter.tqrespec;

import br.com.pinter.tqrespec.core.ExceptionHandler;
import br.com.pinter.tqrespec.core.GameProcessMonitor;
import br.com.pinter.tqrespec.core.GuiceModule;
import br.com.pinter.tqrespec.core.InjectionContext;
import br.com.pinter.tqrespec.gui.MainController;
import br.com.pinter.tqrespec.gui.ResizeListener;
import br.com.pinter.tqrespec.tqdata.Db;
import br.com.pinter.tqrespec.tqdata.GameInfo;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import br.com.pinter.tqrespec.util.Util;
import com.google.inject.Inject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.Collections;
import java.util.ResourceBundle;

public class Main extends Application {
    @Inject
    private Db db;

    @Inject
    private Txt txt;

    @Inject
    private FXMLLoader fxmlLoader;

    private InjectionContext injectionContext = new InjectionContext(this,
            Collections.singletonList(new GuiceModule()));

    public static void main(String... args) {
        System.setProperty("javafx.preloader", "br.com.pinter.tqrespec.gui.AppPreloader");
        launch(args);
    }

    private void load(Stage primaryStage) {
        Task<Void> task = new Task<>() {
            @Override
            public Void call() {
                //preload game database metadata and skills
                notifyPreloader(new Preloader.ProgressNotification(0.3));
                db.initialize();
                notifyPreloader(new Preloader.ProgressNotification(0.7));
                db.skills().preload();
                //preload text
                notifyPreloader(new Preloader.ProgressNotification(0.9));
                txt.preload();

                try {
                    new Thread(new GameProcessMonitor(GameInfo.getInstance().getGamePath())).start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error loading application");
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.getSource().getException().printStackTrace(printWriter);
            TextArea textArea = new TextArea(stringWriter.toString());
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            alert.getDialogPane().setExpandableContent(textArea);
            alert.showAndWait();
            System.exit(1);
        });

        task.setOnSucceeded(e -> {
            notifyPreloader(new Preloader.ProgressNotification(1.0));
            primaryStage.show();
        });
        new Thread(task).start();
        primaryStage.setOnShown(windowEvent -> notifyPreloader(new Preloader.StateChangeNotification(
                Preloader.StateChangeNotification.Type.BEFORE_START)));

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //close stderr before initialize guice, we want to hide java warning about reflection
        System.err.close();
        injectionContext.initialize();
        try {
            System.setErr(new PrintStream(new FileOutputStream(
                    new File(System.getProperty("java.io.tmpdir"), "tqrespec.log"))));
        } catch (Exception ignored) {
        }
        notifyPreloader(new Preloader.ProgressNotification(0.1));
        prepareMainStage(primaryStage);
        load(primaryStage);
    }

    public void prepareMainStage(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt-light.ttf"), 16);
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler::unhandled);

        Parent root;
        try {
            fxmlLoader.setResources(ResourceBundle.getBundle("i18n.UI"));
            fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        primaryStage.setTitle(Util.getBuildTitle());
        primaryStage.getIcons().addAll(new Image("icon/icon64.png"), new Image("icon/icon32.png"), new Image("icon/icon16.png"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        //disable alt+f4
        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(Event::consume);

        //remove default window decoration
        String osName = System.getProperty("os.name");
        if (osName != null && osName.startsWith("Windows")) {
            primaryStage.initStyle(StageStyle.TRANSPARENT);
        } else {
            primaryStage.initStyle(StageStyle.UNDECORATED);
        }

        //disable maximize
        primaryStage.resizableProperty().setValue(Boolean.FALSE);

        ResizeListener listener = new ResizeListener(primaryStage);
        scene.setOnMouseMoved(listener);
        scene.setOnMousePressed(listener);
        scene.setOnMouseDragged(listener);

        primaryStage.getScene().getRoot().styleProperty().bind(Bindings.format("-fx-font-size: %sem;", Constants.INITIAL_FONT_SIZE));

        // min* and max* set to -1 will force javafx to use values defined on root element
        primaryStage.setMinHeight(root.minHeight(-1));
        primaryStage.setMinWidth(root.minWidth(-1));
        primaryStage.setMaxHeight(root.maxHeight(-1));
        primaryStage.setMaxWidth(root.maxWidth(-1));

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, (event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Util.closeApplication();
            }
        }));

        //handler to prepare controls on startup, the use of initialize and risk of crash
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, window -> MainController.mainFormInitialized.setValue(true));
    }
}

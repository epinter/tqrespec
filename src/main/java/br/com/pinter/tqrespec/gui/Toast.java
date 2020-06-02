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

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Constants;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {
    private static final System.Logger logger = Log.getLogger(Toast.class.getName());

    private Toast(Stage stage, String header, String content, int delay) {
        Stage toast = new Stage();
        toast.setResizable(false);
        toast.initOwner(stage);
        toast.initStyle(StageStyle.TRANSPARENT);

        StackPane root = new StackPane();
        root.getStylesheets().add(Constants.UI.MAIN_CSS);

        Label msg = new Label();
        Label msg2 = new Label();
        VBox vBoxMsg = new VBox(msg);
        vBoxMsg.setFillWidth(true);
        vBoxMsg.setPadding(new Insets(5, 5, 20, 5));
        VBox vBoxMsg2 = new VBox(msg2);
        vBoxMsg2.setFillWidth(true);
        vBoxMsg2.setPadding(new Insets(0, 5, 5, 5));
        VBox container = new VBox(vBoxMsg, vBoxMsg2);
        container.setFillWidth(true);

        msg.setAlignment(Pos.CENTER);
        msg.setTextAlignment(TextAlignment.CENTER);

        msg.setWrapText(true);
        msg.setText(header);
        msg.getStyleClass().add(Constants.UI.TOAST_HEADER_STYLE);


        msg2.setAlignment(Pos.BOTTOM_LEFT);
        msg2.setTextAlignment(TextAlignment.CENTER);
        msg2.getStyleClass().add(Constants.UI.TOAST_CONTENT_STYLE);
        msg2.setText(content);
        msg2.setWrapText(true);


        msg.setMaxWidth(stage.getWidth() / 1.8);
        msg2.setMaxWidth(stage.getWidth() / 1.8);
        root.getChildren().add(container);

        root.setStyle("-fx-padding: 30px; -fx-background-color: rgba(0, 0, 0, 0.8); -fx-background-radius: 5;");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toast.setScene(scene);

        toast.show();
        toast.setX(stage.getX() + ((stage.getWidth() - toast.getWidth()) / 2));
        toast.setY(stage.getY() + ((stage.getHeight() - toast.getHeight()) / 2));

        stage.xProperty().addListener(((observableValue, number, newValue) -> toast.setX(newValue.doubleValue() + ((stage.getWidth() - toast.getWidth()) / 2))));
        stage.yProperty().addListener(((observableValue, number, newValue) -> toast.setY(newValue.doubleValue() + ((stage.getHeight() - toast.getHeight()) / 2))));

        Timeline fadeIn = new Timeline();
        KeyFrame keyFrameFadeIn = new KeyFrame(Duration.millis(700), new KeyValue(root.opacityProperty(), 1));
        fadeIn.getKeyFrames().add(keyFrameFadeIn);
        fadeIn.setOnFinished(e -> {
            Task fadeOutTask = new Task() {
                @Override
                protected Object call() {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        logger.log(System.Logger.Level.ERROR, Constants.ERROR_MSG_EXCEPTION, ex);
                        Thread.currentThread().interrupt();
                    }

                    Timeline fadeOut = new Timeline();
                    KeyFrame keyFrameFadeOut = new KeyFrame(Duration.millis(700), new KeyValue(root.opacityProperty(), 0));
                    fadeOut.getKeyFrames().add(keyFrameFadeOut);
                    fadeOut.setOnFinished(t -> toast.close());
                    fadeOut.play();
                    return null;
                }
            };
            new Thread(fadeOutTask).start();
        });
        fadeIn.play();
    }

    public static Toast show(Stage stage, String header, String content, int delay) {
        return new Toast(stage, header, content, delay);
    }
}

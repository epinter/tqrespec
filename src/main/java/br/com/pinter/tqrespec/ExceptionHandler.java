/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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

import br.com.pinter.tqrespec.save.PlayerData;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ExceptionHandler {
    public static void unhandled(Thread t, Throwable e) {
        e.printStackTrace();
        PlayerData.getInstance().reset();
        if (Platform.isFxApplicationThread()) {
            ExceptionHandler.showAlert(t, e);
        }
    }

    public static void showAlert(Thread t, Throwable e) {
        String header = e.toString();
        header = header.replaceAll("^java.lang.RuntimeException: (.*Exception.*)", "$1");
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Error");
        alert.setHeaderText("An unhandled exception occurred");
        alert.setContentText(header);

        TextArea textArea = new TextArea(stackTrace.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        ButtonType abort = new ButtonType("Abort");
        alert.getButtonTypes().setAll(abort);


        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == abort) {
            Platform.exit();
            System.exit(0);
        }
    }


}

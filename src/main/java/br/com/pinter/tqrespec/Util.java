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
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Modality;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Util {
    public static void log(String message) {
        System.err.println(message);
    }

    public static String getBuildVersion() {
        String implementationVersion = Util.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            return "0.0";
        }
        return implementationVersion;
    }

    public static String getBuildTitle() {
        String implementationTitle = Util.class.getPackage().getImplementationTitle();
        if (implementationTitle == null) {
            return "Development";
        }
        return implementationTitle;
    }

    public static void showError(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showWarning(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Warning");
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static void showInformation(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Information");
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static String getUIMessage(String message) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI");
        if (ui.containsKey(message)) {
            return ui.getString(message);
        }
        return message;
    }

    public static String getUIMessage(String message,Object... parameters) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI");
        if (ui.containsKey(message)) {
            return MessageFormat.format(ui.getString(message),parameters);
        }
        return message;
    }

    public static boolean tryToCloseApplication() {
        if (PlayerData.getInstance().getSaveInProgress() != null && !PlayerData.getInstance().getSaveInProgress()
                || PlayerData.getInstance().getSaveInProgress() == null) {
            Platform.exit();
            System.exit(0);
        }
        return false;
    }
    public static void closeApplication() {
        if (!Util.tryToCloseApplication()) {
            Util.showWarning(Util.getUIMessage("alert.saveinprogress_header"),Util.getUIMessage("alert.saveinprogress_content"));
            Task tryAgain = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Util.tryToCloseApplication();
                    return null;
                }
            };
        }
    }
}

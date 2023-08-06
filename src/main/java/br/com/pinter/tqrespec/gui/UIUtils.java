/*
 * Copyright (C) 2022 Emerson Pinter - All Rights Reserved
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

import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.util.Build;
import br.com.pinter.tqrespec.util.Constants;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;

public class UIUtils {
    private static final System.Logger logger = Log.getLogger(UIUtils.class.getName());

    public void showError(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(Build.title());
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void showInformation(String message, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(Build.title());
        alert.setHeaderText(message);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public Tooltip simpleTooltip(String message) {
        Tooltip tooltip = new Tooltip(message);
        tooltip.setFont(Constants.UI.TOOLTIP_FONT);
        tooltip.setShowDelay(Duration.millis(Constants.UI.TOOLTIP_SHOWDELAY_MILLIS));
        tooltip.setShowDuration(Duration.millis(Constants.UI.TOOLTIP_SHOWDURATION_MILLIS));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(Constants.UI.TOOLTIP_MAXWIDTH);
        return tooltip;
    }

    public void closeApplication() {
        if (State.get().getSaveInProgress() != null && !State.get().getSaveInProgress()
                || State.get().getSaveInProgress() == null) {
            Platform.exit();
            System.exit(0);
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(Build.title());
        alert.setHeaderText(ResourceHelper.getMessage("alert.saveinprogress_header"));
        alert.setContentText(ResourceHelper.getMessage("alert.saveinprogress_content"));
        alert.showAndWait();
    }

    public static void fileExplorer(String path) {
        try {
            if(SystemUtils.IS_OS_WINDOWS) {
                Runtime.getRuntime().exec(new String[]{Constants.EXPLORER_COMMAND,path});
            } else {
                Runtime.getRuntime().exec(new String[]{Constants.XDGOPEN_COMMAND,path});
            }
        } catch (IOException e) {
            logger.log(System.Logger.Level.WARNING, "unable to open file explorer: ", e);
        }
    }
}

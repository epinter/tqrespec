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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.net.URL;
import java.util.ResourceBundle;

public class AboutController implements Initializable {
    @FXML
    private AnchorPane rootelement;

    @FXML
    private Label aboutFormTitle;

    @FXML
    private Label aboutVersion;

    @FXML
    private TextArea aboutText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Scene scene = new Scene(rootelement);
        Stage stage = new Stage();
        stage.setScene(scene);
        scene.setFill(Color.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.getIcons().addAll(ResourceHelper.getAppIcons());

        //disable maximize
        stage.resizableProperty().setValue(Boolean.FALSE);

        // min* and max* set to -1 will force javafx to use values defined on root element
        stage.setMinHeight(rootelement.minHeight(-1));
        stage.setMinWidth(rootelement.minWidth(-1));
        stage.setMaxHeight(rootelement.maxHeight(-1));
        stage.setMaxWidth(rootelement.maxWidth(-1));

        //remove default window decoration
        if (SystemUtils.IS_OS_WINDOWS) {
            stage.initStyle(StageStyle.TRANSPARENT);
        } else {
            stage.initStyle(StageStyle.UNDECORATED);
        }

        stage.addEventHandler(KeyEvent.KEY_PRESSED, (event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        }));
        stage.setTitle(ResourceHelper.getMessage("about.title", Build.title()));
        aboutFormTitle.setText(ResourceHelper.getMessage("about.title", Build.title()));
        aboutVersion.setText(ResourceHelper.getMessage("about.version", Build.version()));

        String translators = ResourceHelper.getMessage("main.translators");
        if (StringUtils.isNotBlank(translators)) {
            aboutText.appendText("\n\n" + ResourceHelper.getMessage("main.translators"));
        }

        stage.show();
    }

    @FXML
    public void closeAboutWindow(@SuppressWarnings("unused") MouseEvent evt) {
        Stage stage = (Stage) rootelement.getScene().getWindow();
        stage.close();
    }
}

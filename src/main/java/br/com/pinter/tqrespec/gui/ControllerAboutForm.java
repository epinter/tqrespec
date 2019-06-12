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

import br.com.pinter.tqrespec.Util;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ControllerAboutForm implements Initializable {
    @FXML
    private AnchorPane rootelement;

    @FXML
    private Label aboutFormTitle;

    @FXML
    private Label aboutVersion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt-light.ttf"), 16);
        aboutFormTitle.setText(Util.getUIMessage("about.title", Util.getBuildTitle()));
        aboutVersion.setText(Util.getUIMessage("about.version", Util.getBuildVersion()));
    }

    public void closeAboutWindow(MouseEvent evt) {
        Stage stage = (Stage) rootelement.getScene().getWindow();
        stage.close();
    }

}

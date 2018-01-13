/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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

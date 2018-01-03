/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import br.com.pinter.tqrespec.Util;
import br.com.pinter.tqrespec.Version;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerAboutForm  implements Initializable{
    @FXML
    private AnchorPane rootelement;

    @FXML
    private Label aboutFormTitle;

    @FXML
    private Label aboutVersion;

    @FXML
    private Hyperlink versionCheck;

    private boolean newVersionAvailable = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt.ttf"),16);
        Font.loadFont(getClass().getResourceAsStream("/fxml/albertus-mt-light.ttf"),16);
        aboutFormTitle.setText(Util.getUIMessage("about.title",Util.getBuildTitle()));
        aboutVersion.setText(Util.getUIMessage("about.version",Util.getBuildVersion()));
        Version version = new Version(Util.getBuildVersion());
        int check = version.checkNewerVersion();
        //new version available (-1 our version is less than remote, 0 equal, 1 greater, -2 error checking
        if(check == -1) {
            versionCheck.setOnAction(event ->{
                if(Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
                    final Task<Void> openUrl = new Task<Void>() {
                        @Override
                        public Void call() throws Exception {
                                Desktop.getDesktop().browse(new URI(version.getUrlPage()));
                            return null;
                        }
                    };
                    new Thread(openUrl).start();
                }
            });
            versionCheck.setText(Util.getUIMessage("about.newversion"));
        } else {
            versionCheck.setDisable(true);
        }

    }

    public void closeAboutWindow(MouseEvent evt) {
        Stage stage = (Stage) rootelement.getScene().getWindow();
        stage.close();
    }

}

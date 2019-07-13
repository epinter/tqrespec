/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import javafx.scene.text.Text;

public class IconHelper {

    public static Text createIcon(Icon icon) {
        return createIcon(icon, 1.0);
    }

    public static Text createIcon(Icon icon, double sizeEm) {
        Text text = new Text(icon.toString());
        text.getStyleClass().add("tq-icon-graphic");
        if (icon.name().startsWith("FA_")) {
            text.getStyleClass().add("tq-icon-defaultstyle-fa");
            text.setStyle(String.format("-fx-font-family: 'Font Awesome 5 Free Solid'; -fx-font-size: %s;", sizeEm + "em"));
        }
        return text;
    }
}

/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class IconHelper {
    @SuppressWarnings("FieldCanBeLocal")
    private static String fontAwesomeStyle = "-fx-font-family: 'Font Awesome 5 Free Solid'; " +
            "-fx-font-style: normal; " +
            "-fx-font-weight: 900; " +
            "-fx-fill: #FFFFFF; " +
            "-fx-font-size: 1em; " +
            "-fx-effect: dropshadow( one-pass-box , black , 0, 0.0 , 0 , 2 );";

    public static Text createIcon(Icon icon) {
        Text text = new Text(icon.toString());
        if (icon.name().startsWith("FA_")) {
            text.setStyle(fontAwesomeStyle);
        }
        return text;
    }
}

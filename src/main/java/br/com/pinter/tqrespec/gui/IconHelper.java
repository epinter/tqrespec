/*
 * Copyright (C) 2019 Emerson Pinter - All Rights Reserved
 */

package br.com.pinter.tqrespec.gui;

import javafx.scene.image.Image;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;

public class IconHelper {
    private IconHelper() {
    }

    public static List<Image> getAppIcons() {
        return Arrays.asList(new Image("icon/icon64.png"),
                new Image("icon/icon32.png"),
                new Image("icon/icon16.png"));
    }

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

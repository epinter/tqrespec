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

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

import br.com.pinter.tqrespec.Main;
import br.com.pinter.tqrespec.core.ResourceNotFoundException;
import br.com.pinter.tqrespec.core.State;
import br.com.pinter.tqrespec.logging.Log;
import br.com.pinter.tqrespec.tqdata.Txt;
import br.com.pinter.tqrespec.util.Constants;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ResourceHelper {
    private static final System.Logger logger = Log.getLogger(ResourceHelper.class.getName());

    private ResourceHelper() {
    }

    public static List<Image> getAppIcons() {
        return Arrays.asList(loadImage("icon/icon64.png"), loadImage("icon/icon32.png"), loadImage("icon/icon16.png"));
    }

    public static Image loadImage(String url) {
        try (InputStream image = Main.class.getModule().getResourceAsStream(url)) {
            return new Image(image);
        } catch (IOException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    public static String getResource(String resource) {
        return getResourceUrl(resource).toExternalForm();
    }

    public static URL getResourceUrl(String resource) {
        URL url = Main.class.getResource(resource);
        if (url == null) {
            throw new ResourceNotFoundException(String.format("The resource '%s' was not found.", resource));
        }

        return url;
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

    public static String getMessage(String message) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI", State.get().getLocale());
        if (ui != null && message != null && ui.containsKey(message)) {
            return ui.getString(message);
        }
        return message;
    }

    public static String getMessage(String message, Object... parameters) {
        ResourceBundle ui = ResourceBundle.getBundle("i18n.UI", State.get().getLocale());
        if (ui != null && message != null && ui.containsKey(message)) {
            return MessageFormat.format(ui.getString(message), parameters);
        }
        return message;
    }

    public static String cleanTagString(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }

        return value.replaceAll("(?:\\{[^}]+\\})*([^{}:]*)(?:\\{[^}]+\\})*", "$1")
                .replace(":", "")
                .trim();
    }

    public static void tryTagText(Txt txt, Object control, String tag, boolean capitalized, boolean needsClean) {
        if (!txt.isTagStringValid(tag))
            return;

        String text = capitalized ? txt.getCapitalizedString(tag) : txt.getString(tag);

        if (needsClean) {
            text = ResourceHelper.cleanTagString(text);
        }
        if (control instanceof Labeled) {
            setLabeledText(control, text);
        } else if (control instanceof Tab) {
            setTabText(control, text);
        } else {
            throw new UnsupportedOperationException("BUG: trying to set text on unsupported control");
        }
    }

    public static void loadFont(InputStream is, String fileName) {
        Font loaded = null;
        loaded = Font.loadFont(is, Constants.UI.DEFAULT_FONT_SIZE);
        if (loaded != null) {
            logger.log(System.Logger.Level.INFO, "Loading game font file:''{0}''; name:''{1}''; family:''{2}''",
                    fileName, loaded.getName(), loaded.getFamily());
        }
    }

    public static void loadFonts() {
        Constants.UI.FONTS_LOADLIST.forEach(f -> {
            Font loaded = null;
            if ((loaded = Font.loadFont(getResource(f), Constants.UI.DEFAULT_FONT_SIZE)) != null) {
                logger.log(System.Logger.Level.INFO, "Font loaded: name:''{0}''; family''{1}''", f, loaded.getFamily());
            }
        });
    }

    private static void setLabeledText(Object obj, String text) {
        Labeled control = (Labeled) obj;
        control.setText(text);
    }

    private static void setTabText(Object obj, String text) {
        Tab control = (Tab) obj;
        control.setText(text);
    }
}

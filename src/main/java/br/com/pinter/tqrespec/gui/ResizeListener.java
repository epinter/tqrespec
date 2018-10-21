/*
 * Copyright (C) 2018 Emerson Pinter - All Rights Reserved
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

import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ResizeListener implements EventHandler<MouseEvent> {
    Stage stage;

    double dx;
    double dy;
    double deltaX;
    double deltaY;
    double border = 10;
    boolean resizeH = false;
    boolean resizeV = false;

    public ResizeListener(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void handle(MouseEvent t) {
        if (t.getY() < 27) {
            if (!MouseEvent.MOUSE_PRESSED.equals(t.getEventType())
                    && !MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
                stage.getScene().setCursor(Cursor.DEFAULT);
            }

            resizeH = false;
            resizeV = false;
            return;
        }
        if (MouseEvent.MOUSE_MOVED.equals(t.getEventType())) {
            if (t.getX() < border && t.getY() > stage.getScene().getHeight() - border) {
                stage.getScene().setCursor(Cursor.SW_RESIZE);
                resizeH = true;
                resizeV = true;
            } else if (t.getX() > stage.getScene().getWidth() - border && t.getY() < border) {
                stage.getScene().setCursor(Cursor.NE_RESIZE);
                resizeH = true;
                resizeV = true;
            } else if (t.getX() > stage.getScene().getWidth() - border && t.getY() > stage.getScene().getHeight() - border) {
                stage.getScene().setCursor(Cursor.SE_RESIZE);
                resizeH = true;
                resizeV = true;
            } else if (t.getX() < border || t.getX() > stage.getScene().getWidth() - border) {
                stage.getScene().setCursor(Cursor.E_RESIZE);
                resizeH = true;
                resizeV = false;
            } else if (t.getY() > stage.getScene().getHeight() - border) {
                stage.getScene().setCursor(Cursor.N_RESIZE);
                resizeH = false;
                resizeV = true;
            } else {
                stage.getScene().setCursor(Cursor.DEFAULT);
                resizeH = false;
                resizeV = false;
            }
        } else if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
            dx = stage.getWidth() - t.getX();
            dy = stage.getHeight() - t.getY();
        } else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
            stage.getScene().getRoot().styleProperty().bind(Bindings.format("-fx-font-size: %sem;", stage.getWidth() / stage.getMinWidth()));
            if (
                    ((stage.getHeight() < stage.getMinHeight() && t.getY() + dy - stage.getHeight() > 0)
                            || stage.getHeight() >= stage.getMinHeight()) &&

                            ((stage.getWidth() < stage.getMinWidth() && t.getX() + dx - stage.getWidth() > 0)
                                    || stage.getWidth() >= stage.getMinWidth())) {
                if (resizeH) {
                    double newW = t.getX() + dx;
                    if (newW > stage.getMaxWidth()) return;
                    stage.setWidth(newW);
                    stage.setHeight(stage.getWidth() * (stage.getMinHeight() / stage.getMinWidth()));
                } else if (resizeV) {
                    double newH = t.getY() + dy;
                    if (newH > stage.getMaxHeight()) return;
                    stage.setHeight(newH);
                    stage.setWidth(stage.getHeight() * (stage.getMinWidth() / stage.getMinHeight()));
                }
            }
        }
    }
}

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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

@SuppressWarnings("CanBeFinal")
public class ResizeListener implements EventHandler<MouseEvent> {
    private Stage stage;
    private boolean scale = true;
    private boolean keepAspect = true;

    private double dx;
    private double dy;
    private boolean resizeH = false;
    private boolean resizeV = false;
    private StringExpression fontBinding;

    public ResizeListener(Stage stage) {
        this.stage = stage;
    }

    public boolean isScale() {
        return scale;
    }

    public void setScale(boolean scale) {
        this.scale = scale;
    }

    public boolean isKeepAspect() {
        return keepAspect;
    }

    public void setKeepAspect(boolean keepAspect) {
        this.keepAspect = keepAspect;
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
            mouseMoved(t);
        } else if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
            mousePressed(t);
        } else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
            mouseDragged(t);
        } else if(MouseEvent.MOUSE_RELEASED.equals(t.getEventType())) {
            stage.getScene().setCursor(Cursor.DEFAULT);
            resizeH = false;
            resizeV = false;
        }
    }

    private void mouseDragged(MouseEvent t) {
        if(scale) {
            fontBinding = Bindings.format("-fx-font-size: %sem;", stage.getWidth() / stage.getMinWidth());
            stage.getScene().getRoot().styleProperty().bind(fontBinding);
        }
        if (
                ((stage.getHeight() < stage.getMinHeight() && t.getY() + dy - stage.getHeight() > 0)
                        || stage.getHeight() >= stage.getMinHeight()) &&

                        ((stage.getWidth() < stage.getMinWidth() && t.getX() + dx - stage.getWidth() > 0)
                                || stage.getWidth() >= stage.getMinWidth())) {
            if (resizeH) {
                mouseDraggedResizeH(t);
            } else if (resizeV) {
                mouseDraggedResizeV(t);
            }
        }
    }

    private void mouseDraggedResizeH(MouseEvent t) {
        double newW = t.getX() + dx;
        double newH = t.getY() + dy;
        if (newW > stage.getMaxWidth() || newW < stage.getMinWidth()) return;
        stage.setWidth(newW);
        if(keepAspect) {
            stage.setHeight(stage.getWidth() * (stage.getMinHeight() / stage.getMinWidth()));
        } else {
            if (newH > stage.getMaxHeight() || newH < stage.getMinHeight()) return;
            stage.setHeight(newH);
        }
    }
    private void mouseDraggedResizeV(MouseEvent t) {
        double newW = t.getX() + dx;
        double newH = t.getY() + dy;
        if (newH > stage.getMaxHeight() || newH < stage.getMinHeight()) return;
        stage.setHeight(newH);
        if(keepAspect) {
            stage.setWidth(stage.getHeight() * (stage.getMinWidth() / stage.getMinHeight()));
        } else {
            if (newW > stage.getMaxWidth() || newW < stage.getMinWidth()) return;
            stage.setWidth(newW);
        }
    }

    private void mousePressed(MouseEvent t) {
        dx = stage.getWidth() - t.getX();
        dy = stage.getHeight() - t.getY();
    }

    private void mouseMoved(MouseEvent t) {
        double border = 3;
        if (t.getX() < border && t.getY() > stage.getScene().getHeight() - border) {
            stage.getScene().setCursor(Cursor.SW_RESIZE);
            resizeH = true;
            resizeV = true;
        } else if (t.getX() > stage.getScene().getWidth() - border && t.getY() < border) {
            stage.getScene().setCursor(Cursor.NE_RESIZE);
            resizeH = true;
            resizeV = true;
        } else if (t.getX() > stage.getScene().getWidth() - border*3 && t.getY() > stage.getScene().getHeight() - border*3) {
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
    }
}

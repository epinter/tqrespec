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

import java.util.concurrent.atomic.AtomicBoolean;

public class ResizeListener implements EventHandler<MouseEvent> {
    private final Stage stage;
    private final AtomicBoolean cursorChanged = new AtomicBoolean(false);
    private double dx;
    private double dy;
    private double px;
    private double py;
    private boolean resizeHR = false;
    private boolean resizeVB = false;
    private boolean resizeHL = false;
    private boolean resizeVT = false;
    private boolean scale = true;
    private boolean keepAspect = true;
    private int border = 3;
    @SuppressWarnings("FieldCanBeLocal")
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

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    @Override
    public void handle(MouseEvent t) {
        if (MouseEvent.MOUSE_MOVED.equals(t.getEventType())) {
            detect(t, false);
        } else if (MouseEvent.MOUSE_PRESSED.equals(t.getEventType())) {
            mousePressed(t);
        } else if (MouseEvent.MOUSE_DRAGGED.equals(t.getEventType())) {
            mouseDragged(t);
        } else if (MouseEvent.MOUSE_RELEASED.equals(t.getEventType())) {
            if (cursorChanged.get()) {
                stage.getScene().setCursor(Cursor.DEFAULT);
                cursorChanged.set(false);
            }
            resizeHR = false;
            resizeVB = false;
            resizeHL = false;
            resizeVT = false;
        }
    }

    private void mouseDragged(MouseEvent t) {
        if (
                ((stage.getHeight() < stage.getMinHeight() && t.getY() + dy - stage.getHeight() > 0)
                        || stage.getHeight() >= stage.getMinHeight()) &&

                        ((stage.getWidth() < stage.getMinWidth() && t.getX() + dx - stage.getWidth() > 0)
                                || stage.getWidth() >= stage.getMinWidth())) {
            if (scale) {
                fontBinding = Bindings.format("-fx-font-size: %sem;", stage.getWidth() / stage.getMinWidth());
                stage.getScene().getRoot().styleProperty().bind(fontBinding);
            }
            if (resizeHR) {
                mouseDraggedResizeHR(t);
            }
            if (resizeVB) {
                mouseDraggedResizeVB(t);
            }
            if (resizeHL) {
                mouseDraggedResizeHL(t);
            }
            if (resizeVT) {
                mouseDraggedResizeVT(t);
            }
        }

    }

    private void mouseDraggedResizeHR(MouseEvent t) {
        double newW = t.getX() + dx;
        if (newW > stage.getMaxWidth() || newW < stage.getMinWidth()) return;
        stage.setWidth(newW);
        if (keepAspect) {
            stage.setHeight(stage.getWidth() * (stage.getMinHeight() / stage.getMinWidth()));
        }
    }

    private void mouseDraggedResizeVB(MouseEvent t) {
        double newH = t.getY() + dy;
        if (newH > stage.getMaxHeight() || newH < stage.getMinHeight()) return;
        stage.setHeight(newH);
        if (keepAspect) {
            stage.setWidth(stage.getHeight() * (stage.getMinWidth() / stage.getMinHeight()));
        }
    }

    private void mouseDraggedResizeHL(MouseEvent t) {
        double newW = (px + dx) - t.getScreenX();
        if (newW < stage.getMinWidth() || newW > stage.getMaxWidth()) return;
        stage.setX(t.getScreenX());
        stage.setWidth(newW);
        if (keepAspect) {
            stage.setHeight(stage.getWidth() * (stage.getMinHeight() / stage.getMinWidth()));
        }
    }

    private void mouseDraggedResizeVT(MouseEvent t) {
        double newH = (py + dy) - t.getScreenY();
        if (newH < stage.getMinHeight() || newH > stage.getMaxHeight()) return;
        stage.setY(t.getScreenY());
        stage.setHeight(newH);
        if (keepAspect) {
            stage.setWidth(stage.getHeight() * (stage.getMinWidth() / stage.getMinHeight()));
        }
    }

    private void mousePressed(MouseEvent t) {
        dx = stage.getWidth() - t.getX();
        dy = stage.getHeight() - t.getY();
        px = t.getScreenX();
        py = t.getScreenY();
        detect(t, true);
    }

    private void detect(MouseEvent t, boolean pressed) {
        if (t.getX() < (border * 3) && t.getY() > stage.getScene().getHeight() - (border * 3)) {
            setCursor(Cursor.SW_RESIZE);
            if (pressed) {
                resizeHL = true;
                resizeVT = false;
                resizeHR = false;
                resizeVB = true;
            }
        } else if (t.getX() < (border * 3) && t.getY() < (border * 3)) {
            setCursor(Cursor.NW_RESIZE);
            if (pressed) {
                resizeHL = true;
                resizeVT = true;
                resizeHR = false;
                resizeVB = false;
            }
        } else if (t.getX() > stage.getScene().getWidth() - (border * 3) && t.getY() < (border * 3)) {
            setCursor(Cursor.NE_RESIZE);
            if (pressed) {
                resizeHL = false;
                resizeVT = true;
                resizeHR = true;
                resizeVB = false;
            }
        } else if (t.getX() > stage.getScene().getWidth() - (border * 3) && t.getY() > stage.getScene().getHeight() - (border * 3)) {
            setCursor(Cursor.SE_RESIZE);
            if (pressed) {
                resizeHL = false;
                resizeVT = false;
                resizeHR = true;
                resizeVB = true;
            }
        } else if (t.getX() < border) {
            setCursor(Cursor.W_RESIZE);
            if (pressed) {
                resizeHL = true;
                resizeVT = false;
                resizeHR = false;
                resizeVB = false;
            }
        } else if (t.getX() > stage.getScene().getWidth() - border) {
            setCursor(Cursor.E_RESIZE);
            if (pressed) {
                resizeHL = false;
                resizeVT = false;
                resizeHR = true;
                resizeVB = false;
            }
        } else if (t.getY() > stage.getScene().getHeight() - border) {
            setCursor(Cursor.S_RESIZE);
            if (pressed) {
                resizeHL = false;
                resizeVT = false;
                resizeHR = false;
                resizeVB = true;
            }
        } else if (t.getY() < border) {
            setCursor(Cursor.N_RESIZE);
            if (pressed) {
                resizeHL = false;
                resizeVT = true;
                resizeHR = false;
                resizeVB = false;
            }
        } else {
            if (cursorChanged.get()) {
                stage.getScene().setCursor(Cursor.DEFAULT);
                cursorChanged.set(false);
            }
            if (pressed) {
                resizeHL = false;
                resizeVT = false;
                resizeHR = false;
                resizeVB = false;
            }
        }
    }

    private void setCursor(Cursor cursor) {
        stage.getScene().setCursor(cursor);
        cursorChanged.set(true);
    }
}

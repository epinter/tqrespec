/*
 * Copyright (C) 2020 Emerson Pinter - All Rights Reserved
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

module br.com.pinter.tqrespec {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.net.http;
    requires java.prefs;
    requires java.base;
    requires jdk.zipfs;
    requires java.desktop;
    requires java.logging;
    provides System.LoggerFinder with br.com.pinter.tqrespec.logging.JULLoggerFinder;
    requires static org.apache.commons.lang3;
    requires static org.apache.commons.text;
    requires br.com.pinter.tqdatabase;
    requires static com.google.guice;
    requires static com.google.common;
    requires static com.sun.jna.platform;
    requires static com.fasterxml.jackson.core;
    requires static com.fasterxml.jackson.databind;
    requires static com.fasterxml.jackson.annotation;
    exports br.com.pinter.tqrespec;
    exports br.com.pinter.tqrespec.core;
    exports br.com.pinter.tqrespec.gui;
    exports br.com.pinter.tqrespec.tqdata;
    exports br.com.pinter.tqrespec.save;
    opens br.com.pinter.tqrespec to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.core to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.gui to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.save to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.save.player to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.save.stash to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.tqdata to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.util to com.google.guice, javafx.fxml;
    opens fxml;
    opens i18n;
    opens icon;
    exports br.com.pinter.tqrespec.save.exporter;
    opens br.com.pinter.tqrespec.save.exporter to com.google.guice, javafx.fxml;
}
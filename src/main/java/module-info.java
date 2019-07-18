module br.com.pinter.tqrespec {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.prefs;
    requires java.base;
    requires jdk.zipfs;
    requires java.desktop;
    requires java.logging;
    requires static org.apache.commons.lang3;
    requires br.com.pinter.tqdatabase;
    requires static com.google.guice;
    requires static com.sun.jna.platform;
    exports br.com.pinter.tqrespec;
    exports br.com.pinter.tqrespec.core;
    exports br.com.pinter.tqrespec.gui;
    opens br.com.pinter.tqrespec to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.core to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.gui to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.save to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.tqdata to com.google.guice, javafx.fxml;
    opens br.com.pinter.tqrespec.util to com.google.guice, javafx.fxml;
    opens fxml;
    opens i18n;
    opens icon;
}
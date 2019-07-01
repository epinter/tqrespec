module br.com.pinter.tqrespec {
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.prefs;
    requires java.base;
    requires jdk.zipfs;
    requires java.desktop;
    requires static org.apache.commons.lang3;
    requires br.com.pinter.tqdatabase;
    requires static weld.se.shaded;
    requires static com.sun.jna.platform;
    exports br.com.pinter.tqrespec.core;
    exports br.com.pinter.tqrespec.gui;
    opens br.com.pinter.tqrespec.core to weld.se.shaded,javafx.fxml;
    opens br.com.pinter.tqrespec.gui to weld.se.shaded,javafx.fxml;
    opens br.com.pinter.tqrespec.save to weld.se.shaded,javafx.fxml;
    opens br.com.pinter.tqrespec.tqdata to weld.se.shaded,javafx.fxml;
    opens br.com.pinter.tqrespec.util to weld.se.shaded,javafx.fxml;
    opens fxml;
    opens i18n;
    opens icon;
}
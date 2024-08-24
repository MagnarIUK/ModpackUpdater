module org.magnariuk.modpackupdater {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.desktop;

    opens org.magnariuk.modpackupdater.data.classes to com.google.gson;
    opens org.magnariuk.modpackupdater to javafx.fxml;
    exports org.magnariuk.modpackupdater;
}
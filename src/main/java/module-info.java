module lab.visual.pomodoroapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires javafx.media;
    requires java.desktop;

    opens lab.visual.pomodoroapp to javafx.fxml;
    exports lab.visual.pomodoroapp;

    exports lab.visual.pomodoroapp.controller; // <-- Add this line
    opens lab.visual.pomodoroapp.controller to javafx.fxml; // <-- Add this line (for reflection access)
}
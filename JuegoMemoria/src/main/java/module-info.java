module com.mycompany.juegomemoria {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires java.base;

    opens com.mycompany.juegomemoria to javafx.fxml;
    exports com.mycompany.juegomemoria;
}

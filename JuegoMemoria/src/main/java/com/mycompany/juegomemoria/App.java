package com.mycompany.juegomemoria;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class App extends Application {
    private static Scene scene;
    @Override
    public void start(Stage stage) throws IOException {
        // Asegúrate de que este nombre FXML coincida con tu archivo FXML
        // ¡CAMBIA ESTA LÍNEA AQUÍ!
        scene = new Scene(loadFXML("com/mycompany/juegomemoria/Juego_memoria")); // <-- Sin dimensiones fijas
        stage.setTitle("Juego de Memoria");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.show();
    }
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    private static Parent loadFXML(String fxml) throws IOException {
        // ¡Y CAMBIA ESTA LÍNEA TAMBIÉN!
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml")); // <-- Agrega el '/'
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
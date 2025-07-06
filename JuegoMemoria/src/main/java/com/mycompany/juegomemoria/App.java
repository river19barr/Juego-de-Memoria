package com.mycompany.juegomemoria;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;

public class App extends Application {
    private static Scene scene;
    @Override
    public void start(Stage stage) {
        try {
            scene = new Scene(loadFXML("com/mycompany/juegomemoria/MenuPrincipal"));
            stage.setTitle("Juego de Memoria");
            stage.setScene(scene);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al iniciar la aplicación");
            alert.setHeaderText("No se pudo cargar el menú principal");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml")); 
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
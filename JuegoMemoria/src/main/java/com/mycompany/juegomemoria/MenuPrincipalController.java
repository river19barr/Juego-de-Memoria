package com.mycompany.juegomemoria;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javax.sound.sampled.*;
import javafx.stage.Stage;
import javafx.scene.effect.BoxBlur;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuPrincipalController implements Initializable {
    
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImageView;
    @FXML private VBox menuContainer;
    @FXML private Slider volumenSlider;
    @FXML private Label valorVolumen;
    @FXML private Label iconoVolumen;
    
    private static double volumenGlobal = 50.0;
    private static Clip audioClip;
    private static boolean musicaActiva = true;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // imagen de fondo
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);
        
        // efecto glassmorphism
        menuContainer.setEffect(new BoxBlur(24, 24, 3));
        
        try {
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoDePersonajes.jpg").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fondo: " + e.getMessage());
        }
        
        // configuracion del volumen
        volumenSlider.setValue(volumenGlobal);
        volumenSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            valorVolumen.setText(String.valueOf(newVal.intValue()));
            volumenGlobal = newVal.doubleValue();
            if (audioClip != null) {
                FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volumenDB = (float) (20 * Math.log10(volumenGlobal / 100.0));
                volumeControl.setValue(volumenDB);
            }
        });
        
        // inicializa la música si no está inicializada
        if (audioClip == null) {
            inicializarMusica();
        }
    }
    
    private void inicializarMusica() {
        try {
            URL musicaURL = getClass().getResource("/com/mycompany/juegomemoria/audio/MUSICA2.wav");
            if (musicaURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaURL);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
                audioClip.loop(Clip.LOOP_CONTINUOUSLY);
                
                // control de volumen
                FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volumenDB = (float) (20 * Math.log10(volumenGlobal / 100.0));
                volumeControl.setValue(volumenDB);
                
                if (musicaActiva) {
                    audioClip.start();
                }
            }
        } catch (Exception e) {
            System.err.println("Error inicializando música: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleIniciarJuego() {
        try {
            App.setRoot("com/mycompany/juegomemoria/Juego_memoria");
        } catch (Exception e) {
            System.err.println("Error navegando al juego: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHistorial() {
        try {
            App.setRoot("com/mycompany/juegomemoria/HistorialPartidas");
        } catch (Exception e) {
            System.err.println("Error navegando al historial: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVolumenChange() {
        volumenGlobal = volumenSlider.getValue();
        if (audioClip != null) {
            FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumenDB = (float) (20 * Math.log10(volumenGlobal / 100.0));
            volumeControl.setValue(volumenDB);
        }
    }
    
    @FXML
    private void handleSalir() {
        if (audioClip != null) {
            audioClip.stop();
        }
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
        System.exit(0);
    }
    
    public static double getVolumenGlobal() {
        return volumenGlobal;
    }
    
    public static Clip getAudioClip() {
        return audioClip;
    }
} 
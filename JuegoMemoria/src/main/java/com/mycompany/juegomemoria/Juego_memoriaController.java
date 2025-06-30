package com.mycompany.juegomemoria;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.application.Platform;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.*;
import java.util.TimerTask;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class Juego_memoriaController implements Initializable {
    
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImageView;
    @FXML private BorderPane mainContentPane;
    @FXML private GridPane tableroGrid;

    // Elementos de la barra lateral
    @FXML private VBox sideMenuBox;
    @FXML private Label tiempoLabel;
    @FXML private Label intentosLabel;
    @FXML private Label puntajeLabel;
    @FXML private Button menuButton;
    @FXML private Button musicButton;

    // Propiedades y lógica del juego
    private final int filas = 3;
    private final int columnas = 4;
    private final int totalCartas = filas * columnas;

    private StackPane cartaVolteada1 = null;
    private StackPane cartaVolteada2 = null;
    private boolean clickBloqueado = false;

    // Para almacenar solo los nombres de archivo de las imágenes únicas (sin la ruta completa)
    private List<String> nombresImagenesUnicas;
    private List<String> nombresCartasBarajadas; // Nombres de archivo para las 12 cartas (parejas) barajadas

    // Música de fondo
    private Clip audioClip;
    private boolean musicaActiva = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar la imagen de fondo para cubrir toda la pantalla
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        
        // Inicializar música de fondo
        inicializarMusica();
        
        
        try {
            
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoPantalla.jpg").toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar la imagen de fondo: /com/mycompany/juegomemoria/imagenes/fondoPantalla.jpg");
            System.err.println("Detalles: " + e.getMessage());
            e.printStackTrace();
        }

        // Inicializar los textos de las etiquetas
        tiempoLabel.setText("Tiempo: 00:00");
        intentosLabel.setText("Intentos: 0");
        puntajeLabel.setText("Puntos: 0");

        nombresImagenesUnicas = List.of(
            "Canguros1.jpg",
            "Conejo1.png",
            "Igor1.jpg",
            "Piglet1.jpg",
            "Pooh1.jpg",
            "Tiger1.jpg"
        );

        // Prepara las rutas para las cartas del juego (duplicar y barajar)
        nombresCartasBarajadas = new ArrayList<>();
        int paresNecesarios = totalCartas / 2;

        if (nombresImagenesUnicas.size() < paresNecesarios) {
            System.err.println("ADVERTENCIA: No hay suficientes imágenes únicas para crear " + paresNecesarios + " pares.");
            // Manejar este caso: quizá usar un conjunto más pequeño de cartas o repetir más imágenes
            paresNecesarios = nombresImagenesUnicas.size();
        } 

        // Crear pares de cartas (duplicar cada imagen)
        for (int i = 0; i < paresNecesarios; i++) {
            String nombreArchivo = nombresImagenesUnicas.get(i);
            nombresCartasBarajadas.add(nombreArchivo); // Primera carta del par
            nombresCartasBarajadas.add(nombreArchivo); // Segunda carta del par (duplicado)
        }
        Collections.shuffle(nombresCartasBarajadas);


        // Limpia y configura las restricciones del GridPane dinámicamente
        tableroGrid.getChildren().clear();
        tableroGrid.getColumnConstraints().clear();
        tableroGrid.getRowConstraints().clear();

        for (int j = 0; j < columnas; j++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setPercentWidth(100.0 / columnas);
            tableroGrid.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < filas; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setPercentHeight(100.0 / filas);
            tableroGrid.getRowConstraints().add(rowConstraints);
        }

        //  Crea y añade las cartas al GridPane 
        int cartaIndex = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (cartaIndex < nombresCartasBarajadas.size()) {
                    String nombreArchivoFrente = nombresCartasBarajadas.get(cartaIndex++);
                    StackPane cartaVisual = crearCarta(nombreArchivoFrente); // Pasa solo el nombre del archivo
                    tableroGrid.add(cartaVisual, j, i);
                } else {
                    break;
                }
            }
        }
    }

    private void inicializarMusica() {
        try {
            // Verificar formatos de audio soportados
            verificarFormatosAudio();
            
            // Cargar archivo de música WAV
            cargarMusicaWAV();
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo inicializar la música de fondo: " + e.getMessage());
            // Fallback: crear música sintética si no se puede cargar el archivo
            crearTonoMusical();
        }
    }

    private void verificarFormatosAudio() {
        System.out.println("=== FORMATOS DE AUDIO SOPORTADOS ===");
        System.out.println("JavaFX Version: 21");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        
        // Formatos soportados por JavaFX Media
        System.out.println("\nFormatos soportados por JavaFX Media:");
        System.out.println("- MP3 (requiere javafx.media module)");
        System.out.println("- WAV");
        System.out.println("- AIFF");
        System.out.println("- FLV (solo audio)");
        System.out.println("- MP4 (solo audio)");
        System.out.println("- M4A");
        
        
        System.out.println("\nFormatos soportados por javax.sound.sampled:");
        System.out.println("- WAV");
        System.out.println("- AIFF");
        System.out.println("- AU");
        System.out.println("- SND");
        
        System.out.println("\nNOTA: Para MP3 necesitas agregar javafx.media al module-info.java");
        System.out.println("=====================================");
    }

    private void crearTonoMusical() {
        try {
            // Crear una melodía más larga y continua
            float sampleRate = 44100;
            byte[] audioData = new byte[44100 * 30]; // 15 segundos de audio para una melodía más larga
            
            // Crear una secuencia de notas musicales más compleja
            double[] frecuencias = {
                262.0, 294.0, 330.0, 349.0, 392.0, 440.0, 494.0, 523.0, // C, D, E, F, G, A, B, C
                523.0, 494.0, 440.0, 392.0, 349.0, 330.0, 294.0, 262.0, // C, B, A, G, F, E, D, C (descendente)
                330.0, 349.0, 392.0, 440.0, 494.0, 523.0, 587.0, 659.0  // E, F, G, A, B, C, D, E (escala mayor)
            };
            
            int muestrasPorNota = audioData.length / (frecuencias.length * 2);
            
            int index = 0;
            for (int nota = 0; nota < frecuencias.length; nota++) {
                for (int i = 0; i < muestrasPorNota; i += 2) {
                    if (index < audioData.length - 1) {
                        // Agregar variación en la amplitud para hacer la música más dinámica
                        double amplitud = 3000 + Math.sin(nota * Math.PI / 4) * 1000;
                        short sample = (short) (Math.sin(2 * Math.PI * frecuencias[nota] * i / (sampleRate * 2)) * amplitud);
                        audioData[index] = (byte) (sample & 0xFF);
                        audioData[index + 1] = (byte) ((sample >> 8) & 0xFF);
                        index += 2;
                    }
                }
            }
            
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(audioData), format, audioData.length / format.getFrameSize());
            
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.loop(Clip.LOOP_CONTINUOUSLY); // Repetir indefinidamente
            
            // Control de volumen - hacer más audible
            FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-3.0f); // Volumen aún más alto
            
            if (musicaActiva) {
                audioClip.start();
            }
            System.out.println("Música de fondo continua creada exitosamente");
            
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo crear la música de fondo: " + e.getMessage());
        }
    }

    private void cargarMusicaWAV() {
        try {
            // Cargar el archivo de música WAV
            URL musicaURL = getClass().getResource("/com/mycompany/juegomemoria/audio/MUSICA2.wav");
            if (musicaURL != null) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicaURL);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioStream);
                audioClip.loop(Clip.LOOP_CONTINUOUSLY); // Repetir indefinidamente
                
                // Control de volumen
                FloatControl volumeControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(-5.0f); // Volumen al 50% aproximadamente
                
                if (musicaActiva) {
                    audioClip.start();
                }
                System.out.println("Archivo de música WAV cargado exitosamente");
            } else {
                System.err.println("ERROR: No se encontró el archivo de música MUSICA.wav");
                System.err.println("Buscando en: /com/mycompany/juegomemoria/audio/MUSICA.wav");
                crearTonoMusical();
            }
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo cargar el archivo de música WAV: " + e.getMessage());
            crearTonoMusical();
        }
    }

    @FXML
    private void handleMusicButtonAction() {
        if (audioClip != null) {
            if (musicaActiva) {
                audioClip.stop();
                musicButton.setText("🔇 Música OFF");
                musicaActiva = false;
            } else {
                audioClip.start();
                musicButton.setText("🔊 Música ON");
                musicaActiva = true;
            }
        }
    }

    private StackPane crearCarta(String nombreArchivoFrontal) {
        StackPane contenedor = new StackPane();
        contenedor.setAlignment(Pos.CENTER);
        contenedor.getStyleClass().add("card-container");

        // Tamaño dinámico basado en el grid - se ajustará automáticamente
        contenedor.setMinSize(80, 80);
        contenedor.setPrefSize(120, 120);
        contenedor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ImageView frente = new ImageView();
        try {
            String rutaCompletaFrontal = "/com/mycompany/juegomemoria/imagenes/" + nombreArchivoFrontal;
            frente.setImage(new Image(getClass().getResource(rutaCompletaFrontal).toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar imagen frontal: " + nombreArchivoFrontal);
            frente.setImage(null); // null para evitar errores si no carga
        }

        ImageView reverso = new ImageView();
        try {
            // Ruta del reverso
            reverso.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoCartas.jpg").toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar imagen de reverso: /com/mycompany/juegomemoria/imagenes/fondoCartas.jpg");
            reverso.setImage(null);
        }

        // Ajustar el escalado para que la imagen ocupe todo el espacio de la carta
        frente.setPreserveRatio(false); // FALSE para que se estire y llene el espacio
        reverso.setPreserveRatio(false);

        // Vincula el tamaño de la imagen al tamaño del contenedor (StackPane)
        frente.fitWidthProperty().bind(contenedor.widthProperty());
        frente.fitHeightProperty().bind(contenedor.heightProperty());
        reverso.fitWidthProperty().bind(contenedor.widthProperty());
        reverso.fitHeightProperty().bind(contenedor.heightProperty());

        frente.setVisible(false); // La carta inicia volteada 
        contenedor.getChildren().addAll(reverso, frente);

        contenedor.setOnMouseClicked(e -> manejarClick(contenedor, frente, reverso));

        return contenedor;
    }

    private void manejarClick(StackPane cartaClickeada, ImageView frenteCartaClickeada, ImageView reversoCartaClickeada) {
        if (clickBloqueado || (cartaVolteada1 != null && cartaVolteada2 != null)) {
            return;
        }
        if (!reversoCartaClickeada.isVisible()) {
            return;
        }
        reversoCartaClickeada.setVisible(false);
        frenteCartaClickeada.setVisible(true);
        if (cartaVolteada1 == null) {
            cartaVolteada1 = cartaClickeada;
        } else if (cartaVolteada2 == null && cartaClickeada != cartaVolteada1) {
            cartaVolteada2 = cartaClickeada;
            clickBloqueado = true;
            ImageView frente1 = (ImageView) cartaVolteada1.getChildren().get(1);
            ImageView frente2 = (ImageView) cartaVolteada2.getChildren().get(1);
            if (frente1.getImage() != null && frente2.getImage() != null && 
                !frente1.getImage().getUrl().equals(frente2.getImage().getUrl())) {
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        Platform.runLater(() -> {
                            ImageView reverso1 = (ImageView) cartaVolteada1.getChildren().get(0);
                            ImageView frente1_actual = (ImageView) cartaVolteada1.getChildren().get(1);

                            ImageView reverso2 = (ImageView) cartaVolteada2.getChildren().get(0);
                            ImageView frente2_actual = (ImageView) cartaVolteada2.getChildren().get(1);

                            reverso1.setVisible(true);
                            frente1_actual.setVisible(false);

                            reverso2.setVisible(true);
                            frente2_actual.setVisible(false);

                            cartaVolteada1 = null;
                            cartaVolteada2 = null;
                            clickBloqueado = false;
                        });
                    }
                }, 1000);
            } else {
                cartaVolteada1 = null;
                cartaVolteada2 = null;
                clickBloqueado = false;
            }
        }
    }

    @FXML
    private void handleMenuButtonAction() {
        System.out.println("El botón 'Menú Principal' ha sido presionado.");
        
    }

    public void updateTimeDisplay(String newTime) {
        if (tiempoLabel != null) {
            tiempoLabel.setText("Tiempo: " + newTime);
        }
    }

    public void updateAttemptsDisplay(int newAttempts) {
        if (intentosLabel != null) {
            intentosLabel.setText("Intentos: " + newAttempts);
        }
    }

    public void updateScoreDisplay(int newScore) {
        if (puntajeLabel != null) {
            puntajeLabel.setText("Puntos: " + newScore);
        }
    }
}
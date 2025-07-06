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
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Juego_memoriaController implements Initializable {
    
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImageView;
    @FXML private BorderPane mainContentPane;
    @FXML private GridPane tableroGrid;

    
    @FXML private VBox sideMenuBox;
    @FXML private Label tiempoLabel;
    @FXML private Label intentosLabel;
    @FXML private Label puntajeLabel;
    @FXML private Button menuButton;
    @FXML private Button musicButton;
    @FXML private Button pausarButton;
    @FXML private Button reiniciarButton;

    
    private final int filas = 3;
    private final int columnas = 4;
    private final int totalCartas = filas * columnas;

    private StackPane cartaVolteada1 = null;
    private StackPane cartaVolteada2 = null;
    private boolean clickBloqueado = false;

    
    private List<String> nombresImagenesUnicas;
    private List<String> nombresCartasBarajadas; 

    private boolean partidaPausada = false;

    private int parejasEncontradas = 0;

    @FXML private VBox felicidadesPanel;
    @FXML private Button siButton;
    @FXML private Button noButton;
    @FXML private ImageView felicidadesImage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parejasEncontradas = 0;
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        
        try {
            
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoPooh.jpg").toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar la imagen de fondo: /com/mycompany/juegomemoria/imagenes/fondoPooh.jpg");
            System.err.println("Detalles: " + e.getMessage());
            e.printStackTrace();
        }

       
        tiempoLabel.setText("Tiempo: 00:00");
        intentosLabel.setText("Intentos: 0");
        puntajeLabel.setText("Puntos: 0");

        nombresImagenesUnicas = List.of(
            "Canguros.jpg",
            "Conejo.png",
            "Igor.jpg",
            "Piglet.jpg",
            "Pooh.jpg",
            "Tiger.jpg"
        );

        
        nombresCartasBarajadas = new ArrayList<>();
        int paresNecesarios = totalCartas / 2;

        if (nombresImagenesUnicas.size() < paresNecesarios) {
            System.err.println("ADVERTENCIA: No hay suficientes imágenes únicas para crear " + paresNecesarios + " pares.");
            // Manejar este caso: quizá usar un conjunto más pequeño de cartas o repetir más imágenes
            paresNecesarios = nombresImagenesUnicas.size();
        } 

        
        for (int i = 0; i < paresNecesarios; i++) {
            String nombreArchivo = nombresImagenesUnicas.get(i);
            nombresCartasBarajadas.add(nombreArchivo); // Primera carta del par
            nombresCartasBarajadas.add(nombreArchivo); // Segunda carta del par (duplicado)
        }
        Collections.shuffle(nombresCartasBarajadas);


        
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

        if (felicidadesPanel != null) {
            felicidadesPanel.setVisible(false);
            felicidadesPanel.setManaged(false);
        }
        if (siButton != null && noButton != null) {
            siButton.setOnAction(e -> {
                felicidadesPanel.setVisible(false);
                felicidadesPanel.setManaged(false);
                handleReiniciarPartida();
            });
            noButton.setOnAction(e -> {
                felicidadesPanel.setVisible(false);
                felicidadesPanel.setManaged(false);
            });
        }
    }

    private StackPane crearCarta(String nombreArchivoFrontal) {
        StackPane contenedor = new StackPane();
        contenedor.setAlignment(Pos.CENTER);
        contenedor.getStyleClass().add("card-container");
        contenedor.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contenedor.setMinSize(60, 60);

        // para hacer las esquinas redondeadas
        Rectangle clip = new Rectangle();
        clip.arcWidthProperty().set(28);
        clip.arcHeightProperty().set(28);
        clip.widthProperty().bind(contenedor.widthProperty());
        clip.heightProperty().bind(contenedor.heightProperty());
        contenedor.setClip(clip);

        ImageView frente = new ImageView();
        try {
            String rutaCompletaFrontal = "/com/mycompany/juegomemoria/imagenes/" + nombreArchivoFrontal;
            frente.setImage(new Image(getClass().getResource(rutaCompletaFrontal).toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar imagen frontal: " + nombreArchivoFrontal);
            frente.setImage(null);
        }

        ImageView reverso = new ImageView();
        try {
            reverso.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoCartas.jpg").toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar imagen de reverso: /com/mycompany/juegomemoria/imagenes/fondoCartas.jpg");
            reverso.setImage(null);
        }

        // esto hace que ambas imagenes cubran toda la carta
        frente.setPreserveRatio(false);
        reverso.setPreserveRatio(false);
        frente.fitWidthProperty().bind(contenedor.widthProperty());
        frente.fitHeightProperty().bind(contenedor.heightProperty());
        reverso.fitWidthProperty().bind(contenedor.widthProperty());
        reverso.fitHeightProperty().bind(contenedor.heightProperty());

        frente.setVisible(false);
        contenedor.getChildren().addAll(reverso, frente);

        contenedor.setOnMouseClicked(e -> manejarClick(contenedor, frente, reverso));

        return contenedor;
    }

    private void manejarClick(StackPane cartaClickeada, ImageView frenteCartaClickeada, ImageView reversoCartaClickeada) {
        if (clickBloqueado || (cartaVolteada1 != null && cartaVolteada2 != null) || partidaPausada) {
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
            
            // compara las imagenes de las cartas
            if (frente1.getImage() != null && frente2.getImage() != null &&
                !frente1.getImage().getUrl().equals(frente2.getImage().getUrl())) {
                // si no son iguales, voltea de nuevo las cartas después de 1s
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
                // si son iguales, se dejan volteadas
                cartaVolteada1 = null;
                cartaVolteada2 = null;
                clickBloqueado = false;
                parejasEncontradas++;
                if (parejasEncontradas == totalCartas / 2) {
                    mostrarMensajeFinPartida();
                }
            }
        }
    }

    private void mostrarMensajeFinPartida() {
        Platform.runLater(() -> {
            if (felicidadesPanel != null) {
                felicidadesPanel.setVisible(true);
                felicidadesPanel.setManaged(true);
            }
        });
    }
    
    @FXML
    private void handleMenuButtonAction() {
        try {
            App.setRoot("com/mycompany/juegomemoria/MenuPrincipal");
        } catch (Exception e) {
            System.err.println("Error volviendo al menú: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePausarPartida() {
        if (partidaPausada) {
            // reanudar partida
            partidaPausada = false;
            pausarButton.setText(" Pausar Partida");
        } else {
            // pausar partida
            partidaPausada = true;
            pausarButton.setText(" Reanudar Partida");
        }
    }
    
    @FXML
    private void handleReiniciarPartida() {
        // reinicia las variables del juego
        partidaPausada = false;
        parejasEncontradas = 0;
        // reinicia las cartas
        reiniciarCartas();
        pausarButton.setText("Pausar Partida");
    }
    
    private void reiniciarCartas() {
        // baraja  las cartas nuevamente
        Collections.shuffle(nombresCartasBarajadas);
        // Voltea todas las cartas
        tableroGrid.getChildren().clear();
        int cartaIndex = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (cartaIndex < nombresCartasBarajadas.size()) {
                    String nombreArchivoFrente = nombresCartasBarajadas.get(cartaIndex++);
                    StackPane cartaVisual = crearCarta(nombreArchivoFrente);
                    tableroGrid.add(cartaVisual, j, i);
                }
            }
        }
        cartaVolteada1 = null;
        cartaVolteada2 = null;
        clickBloqueado = false;
    }

    @FXML
    private void handleMusicButtonAction() {
        javax.sound.sampled.Clip audioClip = com.mycompany.juegomemoria.MenuPrincipalController.getAudioClip();
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                audioClip.stop();
                musicButton.setText("Encender Música");
            } else {
                audioClip.start();
                musicButton.setText("Apagar Música");
            }
        }
    }
}
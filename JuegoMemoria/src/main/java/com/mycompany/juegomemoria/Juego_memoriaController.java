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
import javafx.scene.layout.Region;

import java.net.URL;
import java.util.*;
import java.util.TimerTask;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
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
    @FXML private Button nuevaPartidaButton;

    
    private final int filas = 3;
    private final int columnas = 4;
    private final int totalCartas = filas * columnas;

    private StackPane cartaVolteada1 = null;
    private StackPane cartaVolteada2 = null;
    private boolean clickBloqueado = false;

    
    private List<String> nombresImagenesUnicas;
    private List<String> nombresCartasBarajadas; 

    private boolean partidaPausada = false;
    private boolean musicaPausada = false;
    private long posicionMusica = 0;

    private int parejasEncontradas = 0;
    private int puntajeTotal = 0;
    private int intentos = 0;

    private String cartaTrampaNombre = null;

    @FXML private VBox felicidadesPanel;
    @FXML private Button siButton;
    @FXML private Button noButton;
    @FXML private ImageView felicidadesImage;
    
    @FXML private Label labelTiempo;

    @FXML private Label labelIntentos;

    private int tiempoRestante = 60;
    private Timer timer;
    
    
    private void iniciarJuego(){
        javax.sound.sampled.Clip audioClip = com.mycompany.juegomemoria.MenuPrincipalController.getAudioClip();
        // reinicia la música desde el inicio
        if (audioClip != null) {
            audioClip.setFramePosition(0);
            audioClip.start();
        }
        tiempoRestante = 60;
        tiempoLabel.setText("Tiempo: 01:00");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // pausa el tiempo si la partida esta pausada
                    if (!partidaPausada) {
                        if (tiempoRestante > 0) {
                            tiempoRestante--;
                            int minutos = tiempoRestante / 60;
                            int segundos = tiempoRestante % 60;
                            tiempoLabel.setText(String.format("Tiempo: %02d:%02d", minutos, segundos));
                        } else {
                            timer.cancel();
                            mostrarAlertaFinYReiniciar("¡Se acabó el tiempo! La partida se reiniciará automáticamente.");
                        }
                    }
                });
            }
        }, 0, 1000);
    }
    
    private void mostrarAlertaFinYReiniciar(String mensaje) {
        if (timer != null) {
            timer.cancel();
        }
        
        // guarda la partida en el historial
        int minutos = (60 - tiempoRestante) / 60;
        int segundos = (60 - tiempoRestante) % 60;
        String tiempoJugado = String.format("%02d:%02d", minutos, segundos);
        com.mycompany.juegomemoria.HistorialPartidasController.PartidaGuardada.guardarPartida(tiempoJugado, intentos, puntajeTotal);
        mostrarDialogoFelicidades();
    }
    
    private void mostrarMensajeFinPartida() {
        if (timer != null) {
            timer.cancel();
        }
        
        // guarda la partida en el historial
        int minutos = (60 - tiempoRestante) / 60;
        int segundos = (60 - tiempoRestante) % 60;
        String tiempoJugado = String.format("%02d:%02d", minutos, segundos);
        com.mycompany.juegomemoria.HistorialPartidasController.PartidaGuardada.guardarPartida(tiempoJugado, intentos, puntajeTotal);

        mostrarDialogoFelicidades();
    }
    
    private void mostrarDialogoFelicidades() {
        try {
            // carga el FXML del diálogo de felicitaciones
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/juegomemoria/FelicidadesDialog.fxml"));
            Parent dialogRoot = loader.load();
            
            // crea el diálogo
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(rootPane.getScene().getWindow());
            dialogStage.setTitle("¡Felicidades!");
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.setResizable(false);
            
            // obtiene los botones del diálogo
            Button siButton = (Button) dialogRoot.lookup("#siButton");
            Button noButton = (Button) dialogRoot.lookup("#noButton");
            
            // configura las acciones de los botones
            siButton.setOnAction(e -> {
                dialogStage.close();
                handleReiniciarPartida();
            });
            
            noButton.setOnAction(e -> {
                dialogStage.close();
                handleMenuButtonAction(); // Volver al menú principal
            });
            
            // mostra el diálogo
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error mostrando diálogo de felicitaciones: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback: mostrar alerta estándar si falla el diálogo personalizado
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Fin del juego");
            alerta.setHeaderText(null);
            alerta.setContentText("¡Felicidades! Has encontrado todas las parejas.");
            alerta.showAndWait();
            handleReiniciarPartida();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: Inicializando Juego_memoriaController");
        parejasEncontradas = 0;
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        iniciarJuego();
        try {
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoPooh.jpg").toExternalForm()));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: No se pudo cargar la imagen de fondo: /com/mycompany/juegomemoria/imagenes/fondoPooh.jpg");
            System.err.println("Detalles: " + e.getMessage());
            e.printStackTrace();
        }
        tiempoLabel.setText("Tiempo: 00:00");
        intentos = 0;
        puntajeTotal = 0;
        actualizarLabels();
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
            paresNecesarios = nombresImagenesUnicas.size();
        }
        for (int i = 0; i < paresNecesarios; i++) {
            String nombreArchivo = nombresImagenesUnicas.get(i);
            nombresCartasBarajadas.add(nombreArchivo);
            nombresCartasBarajadas.add(nombreArchivo);
        }
        Collections.shuffle(nombresCartasBarajadas);
        cartaTrampaNombre = nombresCartasBarajadas.get(nombresCartasBarajadas.size() - 1);
        
        // configura el GridPane
        System.out.println("DEBUG: Configurando GridPane");
        tableroGrid.getChildren().clear();
        tableroGrid.getColumnConstraints().clear();
        tableroGrid.getRowConstraints().clear();
        
        // hace el GridPane más visible
        tableroGrid.setStyle("");
        tableroGrid.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        tableroGrid.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        // configura columnas
        for (int j = 0; j < columnas; j++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setPercentWidth(100.0 / columnas);
            colConstraints.setMinWidth(100);
            tableroGrid.getColumnConstraints().add(colConstraints);
        }
        
        // configura filas
        for (int i = 0; i < filas; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setPercentHeight(100.0 / filas);
            rowConstraints.setMinHeight(100);
            tableroGrid.getRowConstraints().add(rowConstraints);
        }
        
        // crear y agregar cartas
        System.out.println("DEBUG: Creando cartas...");
        int cartaIndex = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (cartaIndex < nombresCartasBarajadas.size()) {
                    String nombreArchivoFrente = nombresCartasBarajadas.get(cartaIndex++);
                    System.out.println("DEBUG: Creando carta " + cartaIndex + " con imagen: " + nombreArchivoFrente);
                    StackPane cartaVisual = crearCarta(nombreArchivoFrente);
                    tableroGrid.add(cartaVisual, j, i);
                } else {
                    break;
                }
            }
        }
        System.out.println("DEBUG: Total de cartas creadas: " + tableroGrid.getChildren().size());
        
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
        System.out.println("DEBUG: Inicialización completada");
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
            intentos++; // incrementa intentos
            actualizarLabels(); // actualiza la vista
            
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
                // si son iguales, se dejan volteadas y se suma el puntaje
                String url1 = frente1.getImage() != null ? frente1.getImage().getUrl() : "";
                // comprobar si es la pareja trampa
                boolean esTrampa = url1.contains(cartaTrampaNombre);
                cartaVolteada1 = null;
                cartaVolteada2 = null;
                clickBloqueado = false;
                parejasEncontradas++;
                if (esTrampa) {
                    // Penalización: restar 2 puntos y mostrar mensaje
                    puntajeTotal = Math.max(0, puntajeTotal - 2);
                    actualizarLabels();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Carta trampa");
                        alert.setHeaderText(null);
                        alert.setContentText("¡Carta trampa! Pierdes 2 puntos.");
                        alert.showAndWait();
                    });
                } else {
                    puntajeTotal += 2;
                    actualizarLabels();
                }
                if (parejasEncontradas == totalCartas / 2) {
                    mostrarMensajeFinPartida();
                }
            }
        }
    }

    private void checkMatch() {
        intentos++;
        if (cartaVolteada1.getChildren().get(1).equals(cartaVolteada2.getChildren().get(1))) {
            parejasEncontradas++;
            puntajeTotal += 10;
            cartaVolteada1.setVisible(false);
            cartaVolteada2.setVisible(false);
            cartaVolteada1.setEffect(null);
            cartaVolteada2.setEffect(null);
            cartaVolteada1 = null;
            cartaVolteada2 = null;
            actualizarLabels();
            if (parejasEncontradas == totalCartas / 2) {
                mostrarAlertaFinYReiniciar("¡Felicidades! Has encontrado todas las parejas.");
            }
        } else {
            clickBloqueado = true;
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            delay.setOnFinished(e -> {
                ImageView imagenFrente1 = (ImageView) cartaVolteada1.getChildren().get(1);
                ImageView imagenReverso1 = (ImageView) cartaVolteada1.getChildren().get(0);
                ImageView imagenFrente2 = (ImageView) cartaVolteada2.getChildren().get(1);
                ImageView imagenReverso2 = (ImageView) cartaVolteada2.getChildren().get(0);
                imagenFrente1.setVisible(false);
                imagenReverso1.setVisible(true);
                imagenFrente2.setVisible(false);
                imagenReverso2.setVisible(true);
                cartaVolteada1.setEffect(null);
                cartaVolteada2.setEffect(null);
                cartaVolteada1 = null;
                cartaVolteada2 = null;
                clickBloqueado = false;
            });
            delay.play();
            actualizarLabels();
        }
    }

    private void reiniciarCartas() {
        tableroGrid.getChildren().clear();
        Collections.shuffle(nombresCartasBarajadas);
        int cartaIndex = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (cartaIndex < nombresCartasBarajadas.size()) {
                    String nombreArchivoFrente = nombresCartasBarajadas.get(cartaIndex++);
                    StackPane cartaVisual = crearCarta(nombreArchivoFrente);
                    tableroGrid.add(cartaVisual, j, i);
                } else {
                    break;
                }
            }
        }
    }

    private void actualizarLabels() {
        tiempoLabel.setText(String.format("Tiempo: %02d:%02d", tiempoRestante / 60, tiempoRestante % 60));
        intentosLabel.setText("Intentos: " + intentos);
        puntajeLabel.setText("Puntaje: " + puntajeTotal);
    }

    @FXML
    private void handleReiniciarPartida() {
        javax.sound.sampled.Clip audioClip = com.mycompany.juegomemoria.MenuPrincipalController.getAudioClip();
        if (timer != null) {
            timer.cancel();
        }
        partidaPausada = false;
        musicaPausada = false;
        posicionMusica = 0;
        parejasEncontradas = 0;
        puntajeTotal = 0;
        intentos = 0;
        reiniciarCartas();
        actualizarLabels();
        pausarButton.setText("Pausar Partida");
        iniciarJuego();
        if (audioClip != null) {
            audioClip.setFramePosition(0);
            audioClip.start();
        }
    }
    
    @FXML
    private void handlePausarPartida() {
        javax.sound.sampled.Clip audioClip = com.mycompany.juegomemoria.MenuPrincipalController.getAudioClip();
        
        partidaPausada = !partidaPausada;
        
        if (partidaPausada) {
            // Pausar la partida y la música
            pausarButton.setText("Reanudar Partida");
            
            if (audioClip != null && audioClip.isRunning()) {
                posicionMusica = audioClip.getMicrosecondPosition();
                audioClip.stop();
                musicaPausada = true;
            }
        } else {
            // Reanudar la partida y la música
            pausarButton.setText("Pausar Partida");
            
            if (audioClip != null && musicaPausada) {
                audioClip.setMicrosecondPosition(posicionMusica);
                audioClip.start();
                musicaPausada = false;
            }
        }
    }
    
    @FXML
    private void handleMusicButtonAction() {
        javax.sound.sampled.Clip audioClip = com.mycompany.juegomemoria.MenuPrincipalController.getAudioClip();
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                // Si la música está corriendo, guardar la posición y parar
                posicionMusica = audioClip.getMicrosecondPosition();
                audioClip.stop();
                musicButton.setText("Encender Música");
            } else {
                // Si la música está parada, reanudar desde la posición guardada
                audioClip.setMicrosecondPosition(posicionMusica);
                audioClip.start();
                musicButton.setText("Apagar Música");
            }
        }
    }
    
    @FXML
    private void handleMenuButtonAction() {
        try {
            App.setRoot("com/mycompany/juegomemoria/MenuPrincipal");
        } catch (Exception e) {
            System.err.println("Error volviendo al menú: " + e.getMessage());
        }
    }
}
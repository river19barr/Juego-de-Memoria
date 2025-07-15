package com.mycompany.juegomemoria;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HistorialPartidasController implements Initializable {
    
    @FXML private StackPane rootPane;
    @FXML private ImageView backgroundImageView;
    @FXML private BorderPane mainContentPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Label mensajeLabel;
    @FXML private Button jugarButton;
    @FXML private TableView<PartidaGuardada> partidasTableView;
    @FXML private TableColumn<PartidaGuardada, String> fechaCol;
    @FXML private TableColumn<PartidaGuardada, String> tiempoCol;
    @FXML private TableColumn<PartidaGuardada, Integer> intentosCol;
    @FXML private TableColumn<PartidaGuardada, Integer> puntosCol;
    
    private List<PartidaGuardada> partidasGuardadas;
    private static final String ARCHIVO_HISTORIAL = "historial_partidas.json";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // imagen de fondo
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        
        try {
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoPooh.jpg").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fondo: " + e.getMessage());
        }
        
        partidasGuardadas = cargarPartidasGuardadas();
        configurarTabla();
        mostrarPartidas();
        
        // deshabilita el botón al iniciar partida
        jugarButton.setDisable(true);
        // se habilita solo si ha seleccionado una partida 
        partidasTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            jugarButton.setDisable(newVal == null);
        });
    }
    
    private List<PartidaGuardada> cargarPartidasGuardadas() {
        List<PartidaGuardada> partidas = new ArrayList<>();
        File archivo = new File(ARCHIVO_HISTORIAL);
        System.out.println("[DEBUG] Leyendo historial desde: " + archivo.getAbsolutePath());
        if (archivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    // permite tanto partidas separadas por salto de línea como todo en una sola línea
                    String[] lineas = linea.split("\\|(?=\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}\\|)");
                    for (String lineaPartida : lineas) {
                        if (!lineaPartida.trim().isEmpty()) {
                            String[] datos = lineaPartida.split("\\|");
                            if (datos.length >= 4) {
                                partidas.add(new PartidaGuardada(
                                    datos[0], // fecha
                                    datos[1], // tiempo
                                    Integer.parseInt(datos[2]), // intentos
                                    Integer.parseInt(datos[3])  // puntos
                                ));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error cargando historial: " + e.getMessage());
            }
        }
        return partidas;
    }
    
    private void configurarTabla() {
        fechaCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFecha()));
        tiempoCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTiempo()));
        intentosCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getIntentos()).asObject());
        puntosCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getPuntos()).asObject());
    }

    private void mostrarPartidas() {
        ObservableList<PartidaGuardada> partidasObs = FXCollections.observableArrayList(partidasGuardadas);
        partidasTableView.setItems(partidasObs);
        if (partidasGuardadas.isEmpty()) {
            mensajeLabel.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            mensajeLabel.setVisible(false);
            scrollPane.setVisible(true);
        }
    }
    
    @FXML
    private void handleVolver() {
        try {
            App.setRoot("com/mycompany/juegomemoria/MenuPrincipal");
        } catch (Exception e) {
            System.err.println("Error volviendo al menú: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleJugarPartida() {
        PartidaGuardada partidaSeleccionada = partidasTableView.getSelectionModel().getSelectedItem();
        if (partidaSeleccionada != null) {
            PartidaGuardada.setPartidaParaCargar(partidaSeleccionada);
            try {
                App.setRoot("com/mycompany/juegomemoria/Juego_memoria");
            } catch (Exception e) {
                System.err.println("Error cargando partida: " + e.getMessage());
            }
        }
    }
    
    // clase interna para representar una partida guardada
    public static class PartidaGuardada {
        private String fecha;
        private String tiempo;
        private int intentos;
        private int puntos;
        private static PartidaGuardada partidaParaCargar;
        
        public PartidaGuardada(String fecha, String tiempo, int intentos, int puntos) {
            this.fecha = fecha;
            this.tiempo = tiempo;
            this.intentos = intentos;
            this.puntos = puntos;
        }
        
        // Getters
        public String getFecha() { return fecha; }
        public String getTiempo() { return tiempo; }
        public int getIntentos() { return intentos; }
        public int getPuntos() { return puntos; }
        
        // metodo para guardar partida en el historial
        public static void guardarPartida(String tiempo, int intentos, int puntos) {
            LocalDateTime ahora = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String fecha = ahora.format(formatter);
            
            try (FileWriter writer = new FileWriter(ARCHIVO_HISTORIAL, true);
                 BufferedWriter bw = new BufferedWriter(writer);
                 PrintWriter out = new PrintWriter(bw)) {
                
                String linea = String.format("%s|%s|%d|%d", fecha, tiempo, intentos, puntos);
                out.println(linea);
                
            } catch (Exception e) {
                System.err.println("Error guardando partida: " + e.getMessage());
            }
        }
        
        public static void setPartidaParaCargar(PartidaGuardada partida) {
            partidaParaCargar = partida;
        }
        
        public static PartidaGuardada getPartidaParaCargar() {
            return partidaParaCargar;
        }
        
        public static void limpiarPartidaParaCargar() {
            partidaParaCargar = null;
        }
    }
} 
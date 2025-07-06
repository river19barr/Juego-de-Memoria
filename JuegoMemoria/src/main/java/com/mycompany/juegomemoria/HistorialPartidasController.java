package com.mycompany.juegomemoria;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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
    @FXML private ListView<String> partidasListView;
    @FXML private ScrollPane scrollPane;
    @FXML private Label mensajeLabel;
    @FXML private Button jugarButton;
    
    private List<PartidaGuardada> partidasGuardadas;
    private static final String ARCHIVO_HISTORIAL = "historial_partidas.json";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar imagen de fondo
        backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
        backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
        
        try {
            backgroundImageView.setImage(new Image(getClass().getResource("/com/mycompany/juegomemoria/imagenes/fondoPooh.jpg").toExternalForm()));
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fondo: " + e.getMessage());
        }
        
        partidasGuardadas = cargarPartidasGuardadas();
        mostrarPartidas();
        
        // Deshabilita el botón al inicio
        jugarButton.setDisable(true);
        // Se habilita solo si ha seleccionado una partida 
        partidasListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            jugarButton.setDisable(newVal == null);
        });
    }
    
    private List<PartidaGuardada> cargarPartidasGuardadas() {
        List<PartidaGuardada> partidas = new ArrayList<>();
        File archivo = new File(ARCHIVO_HISTORIAL);
        
        if (archivo.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                StringBuilder contenido = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    contenido.append(linea);
                }
                
                // Parsear JSON simple (formato: fecha|tiempo|intentos|puntos)
                String[] lineas = contenido.toString().split("\n");
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
            } catch (Exception e) {
                System.err.println("Error cargando historial: " + e.getMessage());
            }
        }
        
        return partidas;
    }
    
    private void mostrarPartidas() {
        partidasListView.getItems().clear();
        
        if (partidasGuardadas.isEmpty()) {
            mensajeLabel.setVisible(true);
            scrollPane.setVisible(false);
        } else {
            mensajeLabel.setVisible(false);
            scrollPane.setVisible(true);
            
            for (PartidaGuardada partida : partidasGuardadas) {
                String textoPartida = String.format("📅 %s | ⏱️ %s | 🎯 %d intentos | 🏆 %d puntos",
                    partida.getFecha(), partida.getTiempo(), partida.getIntentos(), partida.getPuntos());
                partidasListView.getItems().add(textoPartida);
            }
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
        int indiceSeleccionado = partidasListView.getSelectionModel().getSelectedIndex();
        
        if (indiceSeleccionado >= 0 && indiceSeleccionado < partidasGuardadas.size()) {
            PartidaGuardada partidaSeleccionada = partidasGuardadas.get(indiceSeleccionado);
            
            // Guarda la partida seleccionada para que el juego la cargue
            PartidaGuardada.setPartidaParaCargar(partidaSeleccionada);
            
            try {
                App.setRoot("com/mycompany/juegomemoria/Juego_memoria");
            } catch (Exception e) {
                System.err.println("Error cargando partida: " + e.getMessage());
            }
        }
    }
    
    // Clase interna para representar una partida guardada
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
        
        // Método para guardar partida en el historial
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
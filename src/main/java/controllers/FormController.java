package controllers;

import dao.JuegoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Juego;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FormController implements Initializable {
    @FXML private TextField txtTitulo;
    @FXML private TextField txtDesarrolladora;
    @FXML private TextField txtAnio;
    @FXML private ListView<String> listPlataformas;
    @FXML private ListView<String> listGeneros;
    @FXML private Button btnGuardar;
    @FXML private Label  lblRutaImagen;
    @FXML private ImageView imgPortadaPreview;  // NUEVO

    private JuegoDAO juegoDAO = new JuegoDAO();
    private String rutaPortadaSeleccionada = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listPlataformas.getItems().addAll("1 | PC", "2 | PlayStation 5", "3 | Nintendo Switch", "4 | Xbox Series X");
        listGeneros.getItems().addAll("1 | RPG", "2 | Shooter", "3 | Aventura", "4 | Supervivencia");

        listPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listGeneros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        File archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            try {
                Path carpetaDestino = Path.of("portadas");
                if (!Files.exists(carpetaDestino)) {
                    Files.createDirectories(carpetaDestino);
                }

                String nombreArchivo = archivoSeleccionado.getName();
                Path destino = carpetaDestino.resolve(nombreArchivo);

                Files.copy(archivoSeleccionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

                rutaPortadaSeleccionada = "portadas/" + nombreArchivo;
                lblRutaImagen.setText(nombreArchivo);

                // Cargar la imagen en el ImageView (miniatura)
                File archivoCopiado = destino.toFile();
                Image img = new Image(archivoCopiado.toURI().toString());
                imgPortadaPreview.setImage(img);

            } catch (Exception e) {
                mostrarAlerta("Error imagen", "No se pudo copiar o cargar la imagen de portada.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void guardar() {
        try {
            if ( txtTitulo.getText().isEmpty()
                    || listPlataformas.getSelectionModel().getSelectedItems().isEmpty()
                    || listGeneros.getSelectionModel().getSelectedItems().isEmpty() ) {
                mostrarAlerta("Error", "Debe tener título, al menos una plataforma y un género.");
                return;
            }

            String titulo = txtTitulo.getText();
            String desarrolladora = txtDesarrolladora.getText();
            int anio = txtAnio.getText().isEmpty() ? 0 : Integer.parseInt(txtAnio.getText());

            List<Integer> idsPlataformas = extraerIds(listPlataformas.getSelectionModel().getSelectedItems());
            List<Integer> idsGeneros = extraerIds(listGeneros.getSelectionModel().getSelectedItems());

            Juego nuevoJuego = new Juego(0, titulo, desarrolladora, anio, "", "", rutaPortadaSeleccionada);

            boolean exito = juegoDAO.insertarJuegoConTransaccion(nuevoJuego, idsPlataformas, idsGeneros);

            if (exito) cerrarVentana();
            else mostrarAlerta("Error SQL", "Ocurrió un problema. Rollback ejecutado.");

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número.");
        }
    }

    @FXML private void cancelar() { cerrarVentana(); }

    private void cerrarVentana() {
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private List<Integer> extraerIds(List<String> selecciones) {
        List<Integer> ids = new ArrayList<>();
        for (String s : selecciones) {
            String idStr = s.split("\\|")[0].trim(); // "1 | PC" -> "1"
            ids.add(Integer.parseInt(idStr));
        }
        return ids;
    }
}
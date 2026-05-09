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
import models.Genero;
import models.Plataforma;
import utils.ImgBBUploader;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FormController implements Initializable {
    @FXML private TextField txtTitulo;
    @FXML private TextField txtDesarrolladora;
    @FXML private TextField txtAnio;
    // Ahora usan Objetos Reales, no Strings
    @FXML private ListView<Plataforma> listPlataformas;
    @FXML private ListView<Genero> listGeneros;

    @FXML private Button btnGuardar;
    @FXML private Label lblRutaImagen;
    @FXML private ImageView imgPortadaPreview;

    private JuegoDAO juegoDAO = new JuegoDAO();
    private String rutaPortadaSeleccionada = null;

    private boolean modoEdicion = false;
    private Juego juegoEditando = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. CARGA DINÁMICA DESDE BASE DE DATOS
        listPlataformas.getItems().addAll(juegoDAO.obtenerPlataformas());
        listGeneros.getItems().addAll(juegoDAO.obtenerGeneros());

        listPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listGeneros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Image defaultImg = new Image(getClass().getResourceAsStream("/portadas/sin_portada.jpg"));
        imgPortadaPreview.setImage(defaultImg);
    }

    public void setJuegoEditar(Juego juego) {
        this.modoEdicion = true;
        this.juegoEditando = juego;
        btnGuardar.setText("Guardar cambios");

        txtTitulo.setText(juego.getTitulo());
        txtDesarrolladora.setText(juego.getDesarrolladora());
        txtAnio.setText(String.valueOf(juego.getAnioLanzamiento()));

        rutaPortadaSeleccionada = juego.getRutaPortada();
        if (rutaPortadaSeleccionada != null && !rutaPortadaSeleccionada.isBlank()) {
            File archivo = new File(rutaPortadaSeleccionada);
            if (archivo.exists()) {
                imgPortadaPreview.setImage(new Image(archivo.toURI().toString()));
                lblRutaImagen.setText(archivo.getName());
            }
        }

        // Marcar plataformas que ya tenía el juego
        listPlataformas.getSelectionModel().clearSelection();
        List<Integer> platIds = juegoDAO.obtenerIdsPlataformasDeJuego(juego.getId());
        for (int i = 0; i < listPlataformas.getItems().size(); i++) {
            if (platIds.contains(listPlataformas.getItems().get(i).getId())) {
                listPlataformas.getSelectionModel().select(i);
            }
        }

        // Marcar géneros que ya tenía el juego
        listGeneros.getSelectionModel().clearSelection();
        List<Integer> genIds = juegoDAO.obtenerIdsGenerosDeJuego(juego.getId());
        for (int i = 0; i < listGeneros.getItems().size(); i++) {
            if (genIds.contains(listGeneros.getItems().get(i).getId())) {
                listGeneros.getSelectionModel().select(i);
            }
        }
    }

    // Añade esta variable arriba con las demás
    private File archivoFisicoSeleccionado = null;

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar portada");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes PNG y JPG", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        archivoFisicoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoFisicoSeleccionado != null) {
            // Solo mostramos la miniatura localmente, aún no la hemos subido
            lblRutaImagen.setText(archivoFisicoSeleccionado.getName());
            imgPortadaPreview.setImage(new Image(archivoFisicoSeleccionado.toURI().toString()));
        }
    }

    @FXML
    private void guardar() {
        try {
            if (txtTitulo.getText().isEmpty() || listPlataformas.getSelectionModel().getSelectedItems().isEmpty()
                    || listGeneros.getSelectionModel().getSelectedItems().isEmpty()) {
                mostrarAlerta("Error", "Debe tener título, al menos una plataforma y un género.");
                return;
            }

            // Cambiamos el texto del botón para que el usuario sepa que está cargando
            btnGuardar.setText("Subiendo...");
            btnGuardar.setDisable(true);

            // Si hay un archivo nuevo seleccionado, lo subimos a internet!
            if (archivoFisicoSeleccionado != null) {
                try {
                    String urlNube = ImgBBUploader.subirImagen(archivoFisicoSeleccionado);
                    if (urlNube != null) {
                        rutaPortadaSeleccionada = urlNube; // EJ: https://i.ibb.co/123/slime.png
                    }
                } catch (Exception e) {
                    mostrarAlerta("Error", "No se pudo subir la imagen a internet.");
                    btnGuardar.setText("Guardar");
                    btnGuardar.setDisable(false);
                    return;
                }
            }

            String titulo = txtTitulo.getText();
            String desarrolladora = txtDesarrolladora.getText();
            int anio = txtAnio.getText().isEmpty() ? 0 : Integer.parseInt(txtAnio.getText());

            List<Integer> idsPlataformas = new ArrayList<>();
            for (Plataforma p : listPlataformas.getSelectionModel().getSelectedItems()) idsPlataformas.add(p.getId());

            List<Integer> idsGeneros = new ArrayList<>();
            for (Genero g : listGeneros.getSelectionModel().getSelectedItems()) idsGeneros.add(g.getId());

            boolean exito;
            if (modoEdicion) {
                juegoEditando.setTitulo(titulo);
                juegoEditando.setDesarrolladora(desarrolladora);
                juegoEditando.setAnioLanzamiento(anio);
                juegoEditando.setRutaPortada(rutaPortadaSeleccionada);
                exito = juegoDAO.actualizarJuegoConTransaccion(juegoEditando, idsPlataformas, idsGeneros);
            } else {
                Juego nuevoJuego = new Juego(0, titulo, desarrolladora, anio, "", "", rutaPortadaSeleccionada);
                exito = juegoDAO.insertarJuegoConTransaccion(nuevoJuego, idsPlataformas, idsGeneros);
            }

            if (exito) cerrarVentana();
            else mostrarAlerta("Error SQL", "Ocurrió un problema en la base de datos.");

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número válido.");
            btnGuardar.setText("Guardar");
            btnGuardar.setDisable(false);
        }
    }
    @FXML private void cancelar() { cerrarVentana(); }
    private void cerrarVentana() { ((Stage) btnGuardar.getScene().getWindow()).close(); }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.showAndWait();
    }
}
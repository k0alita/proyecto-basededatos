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
import models.Usuario;
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
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtRating;
    @FXML private ListView<Plataforma> listPlataformas;
    @FXML private ListView<Genero> listGeneros;
    @FXML private Button btnGuardar;
    @FXML private Label lblRutaImagen;
    @FXML private ImageView imgPortadaPreview;

    private JuegoDAO juegoDAO = new JuegoDAO();
    private String rutaPortadaSeleccionada = null;
    private boolean modoEdicion = false;
    private Juego juegoEditando = null;


    private Usuario usuarioActual;

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
        txtDescripcion.setText(juego.getDescripcion() != null ? juego.getDescripcion() : "");
        txtRating.setText(juego.getRating() > 0 ? String.valueOf(juego.getRating()) : "");

        rutaPortadaSeleccionada = juego.getRutaPortada();
        if (rutaPortadaSeleccionada != null && !rutaPortadaSeleccionada.isBlank()) {
            if (rutaPortadaSeleccionada.startsWith("http")) {
                imgPortadaPreview.setImage(new Image(rutaPortadaSeleccionada, true));
                lblRutaImagen.setText("Imagen en la nube ☁️");
            } else {
                File archivo = new File(rutaPortadaSeleccionada);
                if (archivo.exists()) {
                    imgPortadaPreview.setImage(new Image(archivo.toURI().toString()));
                    lblRutaImagen.setText(archivo.getName());
                }
            }
        }

        listPlataformas.getSelectionModel().clearSelection();
        List<Integer> platIds = juegoDAO.obtenerIdsPlataformasDeJuego(juego.getId());
        for (int i = 0; i < listPlataformas.getItems().size(); i++) {
            if (platIds.contains(listPlataformas.getItems().get(i).getId()))
                listPlataformas.getSelectionModel().select(i);
        }

        listGeneros.getSelectionModel().clearSelection();
        List<Integer> genIds = juegoDAO.obtenerIdsGenerosDeJuego(juego.getId());
        for (int i = 0; i < listGeneros.getItems().size(); i++) {
            if (genIds.contains(listGeneros.getItems().get(i).getId()))
                listGeneros.getSelectionModel().select(i);
        }
    }

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

            // Validar rating
            double rating = 0.0;
            if (!txtRating.getText().isEmpty()) {
                try {
                    rating = Double.parseDouble(txtRating.getText().replace(",", "."));
                    if (rating < 0 || rating > 5) {
                        mostrarAlerta("Error", "El rating debe estar entre 0 y 5.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    mostrarAlerta("Error", "El rating debe ser un número (ej: 4.5).");
                    return;
                }
            }

            btnGuardar.setText("Subiendo...");
            btnGuardar.setDisable(true);

            if (archivoFisicoSeleccionado != null) {
                try {
                    String urlNube = ImgBBUploader.subirImagen(archivoFisicoSeleccionado);
                    if (urlNube != null) rutaPortadaSeleccionada = urlNube;
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
            String descripcion = txtDescripcion.getText();

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
                juegoEditando.setDescripcion(descripcion);
                juegoEditando.setRating(rating);
                exito = juegoDAO.actualizarJuegoConTransaccion(juegoEditando, idsPlataformas, idsGeneros);
            } else {
                Juego nuevoJuego = new Juego(0, titulo, desarrolladora, anio, "", "", rutaPortadaSeleccionada, descripcion, rating,usuarioActual != null ? usuarioActual.getUsername() : "desconocido");
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
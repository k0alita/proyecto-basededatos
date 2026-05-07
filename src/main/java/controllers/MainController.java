package controllers;

import dao.JuegoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Juego;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML private TextField txtBuscarTitulo;
    @FXML private ComboBox<String> comboBuscarPlataforma;

    @FXML private TableView<Juego> tablaJuegos;
    @FXML private TableColumn<Juego, Integer> colId;
    @FXML private TableColumn<Juego, String> colTitulo;
    @FXML private TableColumn<Juego, String> colDesarrolladora;
    @FXML private TableColumn<Juego, Integer> colAnio;
    @FXML private TableColumn<Juego, String> colPlataforma;
    @FXML private TableColumn<Juego, String> colGeneros;

    @FXML private ImageView imgPortadaMain; // NUEVO

    private JuegoDAO juegoDAO = new JuegoDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboBuscarPlataforma.getItems().addAll("PC", "PlayStation 5", "Nintendo Switch", "Xbox Series X");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrolladora.setCellValueFactory(new PropertyValueFactory<>("desarrolladora"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioLanzamiento"));
        colPlataforma.setCellValueFactory(new PropertyValueFactory<>("plataformas"));
        colGeneros.setCellValueFactory(new PropertyValueFactory<>("generos"));

        cargarDatosTabla();

        // Escuchar cambios de selección en la tabla
        tablaJuegos.getSelectionModel().selectedItemProperty().addListener(
                (obs, juegoAnterior, juegoNuevo) -> mostrarPortada(juegoNuevo)
        );
    }

    private void mostrarPortada(Juego juego) {
        if (juego == null || juego.getRutaPortada() == null || juego.getRutaPortada().isEmpty()) {
            imgPortadaMain.setImage(null);
            return;
        }

        File archivo = new File(juego.getRutaPortada());
        if (archivo.exists()) {
            Image imagen = new Image(archivo.toURI().toString());
            imgPortadaMain.setImage(imagen);
        } else {
            imgPortadaMain.setImage(null);
        }
    }

    private void cargarDatosTabla() {
        String titulo = txtBuscarTitulo.getText();
        String plataforma = comboBuscarPlataforma.getValue();

        List<Juego> resultados = juegoDAO.buscarJuegos(titulo, plataforma);
        ObservableList<Juego> listaObservable = FXCollections.observableArrayList(resultados);
        tablaJuegos.setItems(listaObservable);
    }

    @FXML
    private void filtrarJuegos() { cargarDatosTabla(); }

    @FXML
    private void limpiarFiltros() {
        txtBuscarTitulo.clear();
        comboBuscarPlataforma.setValue(null);
        cargarDatosTabla();
    }

    @FXML
    private void abrirFormularioAlta() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/FormView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Añadir Juego Nuevo");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root, 450, 520));
            stage.showAndWait();

            cargarDatosTabla();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarJuego() {
        Juego juegoSeleccionado = tablaJuegos.getSelectionModel().getSelectedItem();
        if (juegoSeleccionado == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText("Vas a borrar: " + juegoSeleccionado.getTitulo());
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            juegoDAO.eliminarJuego(juegoSeleccionado.getId());
            cargarDatosTabla();
            imgPortadaMain.setImage(null);
        }
    }
}
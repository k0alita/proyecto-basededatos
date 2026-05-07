package controllers;

import dao.JuegoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Juego;

import java.net.URL;
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

    private JuegoDAO juegoDAO = new JuegoDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listPlataformas.getItems().addAll("1 | PC ", "2 | PlayStation 5", "3 | Nintendo Switch", "4 | Xbox Series X");
        listGeneros.getItems().addAll("1 | RPG", "2 | Shooter", "3 | Aventura", "4 | Supervivencia");

        // Permitir selección múltiple manteniendo pulsado CTRL
        listPlataformas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listGeneros.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    private void guardar() {
        try {
            if (txtTitulo.getText().isEmpty() || listPlataformas.getSelectionModel().getSelectedItems().isEmpty() || listGeneros.getSelectionModel().getSelectedItems().isEmpty()) {
                mostrarAlerta("Error", "Debe tener un titulo, plataforma y genero como minimo");
                return;
            }

            String titulo = txtTitulo.getText();
            String desarrolladora = txtDesarrolladora.getText();
            int anio = txtAnio.getText().isEmpty() ? 0 : Integer.parseInt(txtAnio.getText());

            // Extraer las IDs seleccionadas
            List<Integer> idsPlataformas = extraerIds(listPlataformas.getSelectionModel().getSelectedItems());
            List<Integer> idsGeneros = extraerIds(listGeneros.getSelectionModel().getSelectedItems());

            Juego nuevoJuego = new Juego(0, titulo, desarrolladora, anio, "", "");

            boolean exito = juegoDAO.insertarJuegoConTransaccion(nuevoJuego, idsPlataformas, idsGeneros);

            if (exito) cerrarVentana();
            else mostrarAlerta("Error SQL", "Ocurrió un problema. Rollback ejecutado.");

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un numero.");
        }
    }

    @FXML private void cancelar() { cerrarVentana(); }
    private void cerrarVentana() { ((Stage) btnGuardar.getScene().getWindow()).close(); }

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
            if (s.contains("1")) ids.add(1);
            else if (s.contains("2")) ids.add(2);
            else if (s.contains("3")) ids.add(3);
            else if (s.contains("4")) ids.add(4);
        }
        return ids;
    }
}
package controllers;

import dao.JuegoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
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
    // Estas columnas ya no se usan en la tabla principal (solo en el modelo/detalle)
    @FXML private TableColumn<Juego, String> colPlataforma;
    @FXML private TableColumn<Juego, String> colGeneros;

    @FXML private TableColumn<Juego, String> colPortada;

    @FXML private ImageView imgPortadaMain;
    @FXML private Label lblDetalleTitulo;
    @FXML private Label lblDetalleDesarrolladora;
    @FXML private Label lblDetalleAnio;
    @FXML private Label lblDetallePlataformas;
    @FXML private Label lblDetalleGeneros;

    private double xOffset;
    private double yOffset;

    private final JuegoDAO juegoDAO = new JuegoDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        comboBuscarPlataforma.getItems().addAll(
                "PC", "PlayStation 5", "Nintendo Switch", "Xbox Series X"
        );

        // Columnas visibles en la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrolladora.setCellValueFactory(new PropertyValueFactory<>("desarrolladora"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioLanzamiento"));
        colId.setVisible(false);

        // Alinear ID y Año
        colId.setStyle("-fx-alignment: CENTER;");
        colAnio.setStyle("-fx-alignment: CENTER;");

        // Todas las filas con la misma altura
        tablaJuegos.setFixedCellSize(40);

        // Columna de portada (miniatura)
        colPortada.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final javafx.scene.layout.StackPane contenedor = new javafx.scene.layout.StackPane();

            {
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                contenedor.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                contenedor.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                contenedor.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

                contenedor.setAlignment(javafx.geometry.Pos.CENTER); // centro exacto en la celda
                contenedor.getChildren().add(imageView);

                setGraphic(contenedor);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    contenedor.setVisible(false);
                    return;
                }

                Juego juego = getTableView().getItems().get(getIndex());
                String ruta = juego.getRutaPortada();

                if (ruta == null || ruta.isEmpty()) {
                    imageView.setImage(getImagenPorDefecto());
                } else {
                    File archivo = new File(ruta);
                    if (archivo.exists()) {
                        imageView.setImage(new Image(archivo.toURI().toString()));
                    } else {
                        imageView.setImage(getImagenPorDefecto());
                    }
                }

                contenedor.setVisible(true);
            }
        });

        // Cargar datos iniciales
        cargarDatosTabla();

        // Cuando cambie la selección, actualiza detalle y portada grande
        tablaJuegos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, nuevo) -> {
                    mostrarPortada(nuevo);
                    mostrarDetalle(nuevo);
                }
        );
    }

    // ---- LÓGICA DE INTERFAZ ----

    private void mostrarPortada(Juego juego) {
        if (juego == null) {
            imgPortadaMain.setImage(getImagenPorDefecto());
            return;
        }

        String ruta = juego.getRutaPortada();

        if (ruta == null || ruta.isEmpty()) {
            imgPortadaMain.setImage(getImagenPorDefecto());
            return;
        }

        File archivo = new File(ruta);
        if (archivo.exists()) {
            Image imagen = new Image(archivo.toURI().toString());
            imgPortadaMain.setImage(imagen);
        } else {
            imgPortadaMain.setImage(getImagenPorDefecto());
        }
    }

    private void mostrarDetalle(Juego juego) {
        if (juego == null) {
            lblDetalleTitulo.setText("Título: -");
            lblDetalleDesarrolladora.setText("Desarrolladora: -");
            lblDetalleAnio.setText("Año: -");
            lblDetallePlataformas.setText("Plataformas: -");
            lblDetalleGeneros.setText("Géneros: -");
            imgPortadaMain.setImage(getImagenPorDefecto());
            return;
        }

        lblDetalleTitulo.setText("Título: " + juego.getTitulo());
        lblDetalleDesarrolladora.setText("Desarrolladora: " + juego.getDesarrolladora());
        lblDetalleAnio.setText("Año: " + juego.getAnioLanzamiento());
        lblDetallePlataformas.setText("Plataformas: " + juego.getPlataformas());
        lblDetalleGeneros.setText("Géneros: " + juego.getGeneros());

        mostrarPortada(juego);
    }

    private Image getImagenPorDefecto() {
        return new Image(getClass().getResourceAsStream("/portadas/sin_portada.jpg"));
    }

    private void cargarDatosTabla() {
        String titulo = txtBuscarTitulo.getText();
        String plataforma = comboBuscarPlataforma.getValue();

        List<Juego> resultados = juegoDAO.buscarJuegos(titulo, plataforma);
        ObservableList<Juego> listaObservable = FXCollections.observableArrayList(resultados);
        tablaJuegos.setItems(listaObservable);
    }

    // ---- ACCIONES DE BÚSQUEDA ----

    @FXML
    private void filtrarJuegos() {
        cargarDatosTabla();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscarTitulo.clear();
        comboBuscarPlataforma.setValue(null);
        cargarDatosTabla();
    }

    // ---- FORMULARIO ALTA ----

    @FXML
    private void abrirFormularioAlta() {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/FormView.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Añadir Juego Nuevo");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 450, 520);

            // MISMO CSS QUE EL RESTO
            scene.getStylesheets().add(
                    getClass().getResource("/style.css").toExternalForm()
            );

            stage.setScene(scene);
            stage.showAndWait();

            cargarDatosTabla();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- ELIMINAR JUEGO ----

    @FXML
    private void eliminarJuego() {
        Juego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();

        // 1. No hay nada seleccionado → error con estilo unificado
        if (seleccionado == null) {
            Alert alerta = crearAlert(
                    Alert.AlertType.ERROR,
                    "Eliminar juego",
                    "Debes seleccionar un juego de la tabla para poder eliminarlo."
            );
            alerta.showAndWait();
            return;
        }

        // 2. Confirmación antes de borrar (mismo estilo base)
        Alert confirm = crearAlert(
                Alert.AlertType.CONFIRMATION,
                "Confirmar eliminación",
                "¿Seguro que quieres eliminar el juego \"" + seleccionado.getTitulo() + "\"?"
        );

        // Botones personalizados
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnEliminar, btnCancelar);

        // Añadir clases CSS específicas a cada botón
        Button botonEliminar = (Button) confirm.getDialogPane().lookupButton(btnEliminar);
        if (botonEliminar != null) {
            botonEliminar.getStyleClass().addAll("btn-danger");
        }

        Button botonCancelar = (Button) confirm.getDialogPane().lookupButton(btnCancelar);
        if (botonCancelar != null) {
            botonCancelar.getStyleClass().addAll("btn-secondary");
        }

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {

            // Borrado en BD usando JuegoDAO
            juegoDAO.eliminarJuego(seleccionado.getId());

            // Quitar de la tabla
            tablaJuegos.getItems().remove(seleccionado);

            // Limpiar detalle
            mostrarPortada(null);
            mostrarDetalle(null);
        }
    }

    // ---- BARRA DE TÍTULO PERSONALIZADA ----

    @FXML
    private void onTitleBarPressed(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }

    @FXML
    private void onTitleBarDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }

    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) tablaJuegos.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void minimizarVentana() {
        Stage stage = (Stage) tablaJuegos.getScene().getWindow();
        stage.setIconified(true);
    }

    // ---- ESTILO COMÚN PARA ALERTAS ----

    // Crea un Alert ya con el CSS y clase base aplicados
    private Alert crearAlert(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        aplicarEstiloAlert(alert);
        aplicarEstiloBotones(alert);

        return alert;
    }

    private void aplicarEstiloAlert(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/style.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().addAll("root-pane", "custom-alert");
    }

    private void aplicarEstiloBotones(Alert alert) {
        // Botón principal
        Button btOk = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (btOk != null) {
            btOk.getStyleClass().add("btn-primary");
        }

        // Botón cancelar si existe
        Button btCancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (btCancel != null) {
            btCancel.getStyleClass().add("btn-secondary");
        }
    }
}
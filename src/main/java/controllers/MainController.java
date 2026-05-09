package controllers;

import dao.JuegoDAO;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Juego;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @FXML private TableColumn<Juego, String> colPortada;

    @FXML private ImageView imgPortadaMain;
    @FXML private Label lblDetalleTitulo;
    @FXML private Label lblDetalleDesarrolladora;
    @FXML private Label lblDetalleAnio;
    @FXML private Label lblDetallePlataformas;
    @FXML private Label lblDetalleGeneros;

    private double xOffset;
    private double yOffset;

    // Caché de imágenes: URL -> Image ya descargada (para que vaya rapidísimo)
    private final Map<String, Image> cacheImagenes = new HashMap<>();

    private final JuegoDAO juegoDAO = new JuegoDAO();

    // Temporizador de 300 ms para que no se congele al escribir rápido
    private PauseTransition pauseBusqueda = new PauseTransition(Duration.millis(300));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Cargar plataformas desde Base de Datos
        comboBuscarPlataforma.getItems().clear();
        comboBuscarPlataforma.getItems().add("Todas");
        List<models.Plataforma> plataformasBD = juegoDAO.obtenerPlataformas();
        for (models.Plataforma p : plataformasBD) {
            comboBuscarPlataforma.getItems().add(p.getNombre());
        }
        comboBuscarPlataforma.getSelectionModel().selectFirst();

        // 2. Configurar Columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrolladora.setCellValueFactory(new PropertyValueFactory<>("desarrolladora"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioLanzamiento"));
        colId.setVisible(false);
        colId.setStyle("-fx-alignment: CENTER;");
        colAnio.setStyle("-fx-alignment: CENTER;");
        tablaJuegos.setFixedCellSize(72);

        // 3. Columna de miniatura con Caché
        colPortada.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final javafx.scene.layout.StackPane contenedor = new javafx.scene.layout.StackPane();

            // Animaciones de escala
            private final javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(
                    javafx.util.Duration.millis(180), imageView
            );
            private final javafx.animation.ScaleTransition scaleOut = new javafx.animation.ScaleTransition(
                    javafx.util.Duration.millis(180), imageView
            );

            {
                // Tamaño base de la miniatura
                imageView.setFitWidth(44);
                imageView.setFitHeight(44);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                // Clip circular para que la foto quede redonda como un avatar
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(44, 44);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imageView.setClip(clip);

                // Configurar animaciones
                scaleIn.setToX(1.5);
                scaleIn.setToY(1.5);
                scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

                scaleOut.setToX(1.0);
                scaleOut.setToY(1.0);
                scaleOut.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

                // Eventos del ratón sobre la FILA completa (no solo la imagen)
                tableRowProperty().addListener((obs, oldRow, newRow) -> {
                    if (oldRow != null) {
                        oldRow.setOnMouseEntered(null);
                        oldRow.setOnMouseExited(null);
                    }
                    if (newRow != null) {
                        javafx.animation.Timeline expandir = new javafx.animation.Timeline(
                                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                                        new javafx.animation.KeyValue(
                                                newRow.prefHeightProperty(), 110,
                                                javafx.animation.Interpolator.EASE_OUT
                                        )
                                )
                        );

                        javafx.animation.Timeline encoger = new javafx.animation.Timeline(
                                new javafx.animation.KeyFrame(javafx.util.Duration.millis(200),
                                        new javafx.animation.KeyValue(
                                                newRow.prefHeightProperty(), 72,
                                                javafx.animation.Interpolator.EASE_OUT
                                        )
                                )
                        );

                        newRow.setOnMouseEntered(e -> {
                            encoger.stop();
                            expandir.playFromStart();
                        });
                        newRow.setOnMouseExited(e -> {
                            expandir.stop();
                            encoger.playFromStart();
                        });
                    }
                });

                contenedor.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
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
                imageView.setImage(getImagenConCache(juego.getRutaPortada()));
                contenedor.setVisible(true);
            }
        });

        // 4. Doble Clic para Editar
        tablaJuegos.setRowFactory(tv -> {
            TableRow<Juego> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    abrirFormularioEdicion();
                }
            });
            return row;
        });

        // 5. Cargar datos iniciales
        cargarDatosTabla();

        // 6. Al seleccionar un juego, actualizar detalles
        tablaJuegos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            mostrarDetalle(nuevo);
        });

        // 7. Búsqueda fluida (Listenres)
        pauseBusqueda.setOnFinished(event -> cargarDatosTabla());
        txtBuscarTitulo.textProperty().addListener((observable, oldValue, newValue) -> {
            pauseBusqueda.playFromStart();
        });
        comboBuscarPlataforma.valueProperty().addListener((observable, oldValue, newValue) -> {
            cargarDatosTabla();
        });
        // Forzar altura de filas desde Java (el FXML a veces lo ignora)
        tablaJuegos.setFixedCellSize(72);
    }

    // ---- GESTIÓN DE IMÁGENES (LOCAL, INTERNET Y CACHÉ) ----

    private Image getImagenPorDefecto() {
        return new Image(getClass().getResourceAsStream("/portadas/sin_portada.jpg"));
    }

    private Image getImagenConCache(String ruta) {
        if (ruta == null || ruta.isEmpty()) {
            return getImagenPorDefecto();
        }

        // Si ya descargamos esta foto antes, la sacamos de la memoria RAM (caché)
        if (cacheImagenes.containsKey(ruta)) {
            return cacheImagenes.get(ruta);
        }

        // Si es la primera vez, la descargamos de internet o del disco
        Image imagen;
        if (ruta.startsWith("http")) {
            imagen = new Image(ruta, true); // true = descargar de internet sin congelar
        } else {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                imagen = new Image(archivo.toURI().toString());
            } else {
                return getImagenPorDefecto();
            }
        }

        // La guardamos en el caché para la próxima vez
        cacheImagenes.put(ruta, imagen);
        return imagen;
    }

    // ---- LÓGICA DE INTERFAZ ----

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

        // Usamos el caché para cargar la foto principal
        imgPortadaMain.setImage(getImagenConCache(juego.getRutaPortada()));
    }

    private void cargarDatosTabla() {
        String titulo = txtBuscarTitulo.getText();
        String plataforma = comboBuscarPlataforma.getValue();

        if (plataforma != null && plataforma.equals("Todas")) {
            plataforma = null;
        }

        List<Juego> resultados = juegoDAO.buscarJuegos(titulo, plataforma);
        ObservableList<Juego> listaObservable = FXCollections.observableArrayList(resultados);
        tablaJuegos.setItems(listaObservable);
    }

    // ---- FORMULARIOS ----

    @FXML
    private void abrirFormularioAlta() {
        abrirModalFormulario(null, "Añadir Juego Nuevo");
    }

    @FXML
    private void abrirFormularioEdicion() {
        Juego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Alert alerta = crearAlert(Alert.AlertType.ERROR, "Editar juego", "Debes seleccionar un juego.");
            alerta.showAndWait();
            return;
        }
        abrirModalFormulario(seleccionado, "Editar juego");
    }

    private void abrirModalFormulario(Juego juego, String tituloVentana) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormView.fxml"));
            Parent root = loader.load();

            if (juego != null) {
                FormController controller = loader.getController();
                controller.setJuegoEditar(juego);
            }

            Stage stage = new Stage();
            stage.setTitle(tituloVentana);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 450, 520);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            cargarDatosTabla();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---- ELIMINAR ----

    @FXML
    private void eliminarJuego() {
        Juego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            Alert alerta = crearAlert(Alert.AlertType.ERROR, "Eliminar juego", "Selecciona un juego.");
            alerta.showAndWait();
            return;
        }

        Alert confirm = crearAlert(Alert.AlertType.CONFIRMATION, "Confirmar eliminación",
                "¿Seguro que quieres eliminar el juego \"" + seleccionado.getTitulo() + "\"?");
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnEliminar, btnCancelar);

        Button botonEliminar = (Button) confirm.getDialogPane().lookupButton(btnEliminar);
        if (botonEliminar != null) botonEliminar.getStyleClass().addAll("btn-danger");
        Button botonCancelar = (Button) confirm.getDialogPane().lookupButton(btnCancelar);
        if (botonCancelar != null) botonCancelar.getStyleClass().addAll("btn-secondary");

        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {

            // Borrar archivo físico si era local
            String rutaFoto = seleccionado.getRutaPortada();
            if (rutaFoto != null && !rutaFoto.startsWith("http")) {
                File archivoFoto = new File(rutaFoto);
                if (archivoFoto.exists() && archivoFoto.isFile()) archivoFoto.delete();
            }

            juegoDAO.eliminarJuego(seleccionado.getId());
            tablaJuegos.getItems().remove(seleccionado);
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

    @FXML private void cerrarVentana() { ((Stage) tablaJuegos.getScene().getWindow()).close(); }
    @FXML private void minimizarVentana() { ((Stage) tablaJuegos.getScene().getWindow()).setIconified(true); }

    // ---- ALERTAS ----

    private Alert crearAlert(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().addAll("root-pane", "custom-alert");
        return alert;
    }
}
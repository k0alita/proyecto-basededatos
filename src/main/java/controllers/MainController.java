package controllers;

import dao.JuegoDAO;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tooltip;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Juego;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TextField txtBuscarTitulo;
    @FXML private ComboBox<String> comboBuscarPlataforma;
    @FXML private ComboBox<String> comboBuscarGenero;
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
    @FXML private Label lblDetalleDescripcion;
    @FXML private Label lblDetalleRatingEstrellas;
    @FXML private Label lblDetalleRatingNum;
    @FXML private VBox panelDetalle;
    private double xOffset;
    private double yOffset;
    private final Map<String, Image> cacheImagenes = new HashMap<>();
    private final JuegoDAO juegoDAO = new JuegoDAO();
    private final PauseTransition pauseBusqueda = new PauseTransition(Duration.millis(300));

    private static final double ALTURA_BASE = 72;
    private static final double ALTURA_MAX  = 110;

    private TableRow<?> filaExpandida = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // 1. Plataformas
        comboBuscarPlataforma.getItems().clear();
        comboBuscarPlataforma.getItems().add("Plataformas");
        for (models.Plataforma p : juegoDAO.obtenerPlataformas()) {
            comboBuscarPlataforma.getItems().add(p.getNombre());
        }
        comboBuscarPlataforma.getSelectionModel().selectFirst();

        // 2. Géneros
        comboBuscarGenero.getItems().clear();
        comboBuscarGenero.getItems().add("Géneros");
        for (models.Genero g : juegoDAO.obtenerGeneros()) {
            comboBuscarGenero.getItems().add(g.getNombre());
        }
        comboBuscarGenero.getSelectionModel().selectFirst();

        // 3. Columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colDesarrolladora.setCellValueFactory(new PropertyValueFactory<>("desarrolladora"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioLanzamiento"));
        colId.setVisible(false);
        colId.setStyle("-fx-alignment: CENTER;");
        colAnio.setStyle("-fx-alignment: CENTER;");

        // 4. Columna miniatura
        colPortada.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final ImageView tooltipImg = new ImageView();
            private final Tooltip tooltip = new Tooltip();
            private final javafx.scene.layout.StackPane contenedor = new javafx.scene.layout.StackPane();

            {
                imageView.setFitWidth(44);
                imageView.setFitHeight(44);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(44, 44);
                clip.setArcWidth(8);
                clip.setArcHeight(8);
                imageView.setClip(clip);
                contenedor.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
                contenedor.getChildren().add(imageView);
                setGraphic(contenedor);

                tooltipImg.setFitWidth(180);
                tooltipImg.setFitHeight(240);
                tooltipImg.setPreserveRatio(true);
                tooltipImg.setSmooth(true);
                tooltip.setGraphic(tooltipImg);
                tooltip.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 10; -fx-padding: 8;");

                contenedor.setOnMouseEntered(e -> {
                    if (!isEmpty() && getIndex() < getTableView().getItems().size()) {
                        Juego juego = getTableView().getItems().get(getIndex());
                        tooltipImg.setImage(getImagenConCache(juego.getRutaPortada()));
                        tooltip.show(contenedor, e.getScreenX() + 15, e.getScreenY() + 10);
                    }
                });
                contenedor.setOnMouseExited(e -> tooltip.hide());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { contenedor.setVisible(false); tooltip.hide(); return; }
                Juego juego = getTableView().getItems().get(getIndex());
                imageView.setImage(getImagenConCache(juego.getRutaPortada()));
                contenedor.setVisible(true);
            }
        });

        // 5. RowFactory
        tablaJuegos.setRowFactory(tv -> {
            TableRow<Juego> row = new TableRow<>();
            row.setMinHeight(ALTURA_BASE);
            row.setPrefHeight(ALTURA_BASE);
            row.setMaxHeight(ALTURA_BASE);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) abrirFormularioEdicion();
            });
            return row;
        });

        // 6. Animación hover
        tablaJuegos.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            TableRow<?> encontrada = null;
            for (Node node : tablaJuegos.lookupAll(".table-row-cell")) {
                if (node instanceof TableRow<?> r && !r.isEmpty()) {
                    javafx.geometry.Bounds enScene = r.localToScene(r.getBoundsInLocal());
                    javafx.geometry.Bounds enTabla = tablaJuegos.sceneToLocal(enScene);
                    if (e.getY() >= enTabla.getMinY() && e.getY() <= enTabla.getMaxY()) {
                        encontrada = r;
                        break;
                    }
                }
            }
            if (encontrada != filaExpandida) {
                encogerFila(filaExpandida);
                expandirFila(encontrada);
                filaExpandida = encontrada;
            }
        });

        tablaJuegos.addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
            encogerFila(filaExpandida);
            filaExpandida = null;
        });

        // 6.1 Reset al hacer scroll
        tablaJuegos.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin == null) return;
            ScrollBar sb = (ScrollBar) tablaJuegos.lookup(".scroll-bar:vertical");
            if (sb != null) {
                sb.valueProperty().addListener((o, oldVal, newVal) -> resetearFilas());
            }
        });

        // 7. Cargar datos
        cargarDatosTabla();

        // 8. Selección → detalle
        tablaJuegos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) ->
                mostrarDetalle(nuevo));

        // 9. Búsqueda fluida
        pauseBusqueda.setOnFinished(event -> cargarDatosTabla());
        txtBuscarTitulo.textProperty().addListener((obs, oldVal, newVal) -> pauseBusqueda.playFromStart());

        // 10. Listeners combos con estilo activo
        comboBuscarPlataforma.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstiloCombo(comboBuscarPlataforma, "Plataformas");
            cargarDatosTabla();
        });

        comboBuscarGenero.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarEstiloCombo(comboBuscarGenero, "Géneros");
            cargarDatosTabla();
        });
    }

    // Resalta el combo cuando tiene un filtro activo
    private void actualizarEstiloCombo(ComboBox<String> combo, String valorNeutro) {
        if (combo.getValue() == null || combo.getValue().equals(valorNeutro)) {
            combo.getStyleClass().remove("combo-activo");
        } else {
            if (!combo.getStyleClass().contains("combo-activo")) {
                combo.getStyleClass().add("combo-activo");
            }
        }
    }

    private void expandirFila(TableRow<?> row) {
        if (row == null) return;
        new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(row.minHeightProperty(), ALTURA_MAX, Interpolator.EASE_OUT),
                new KeyValue(row.prefHeightProperty(), ALTURA_MAX, Interpolator.EASE_OUT),
                new KeyValue(row.maxHeightProperty(), ALTURA_MAX, Interpolator.EASE_OUT)
        )).play();
    }

    private void encogerFila(TableRow<?> row) {
        if (row == null) return;
        new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(row.minHeightProperty(), ALTURA_BASE, Interpolator.EASE_OUT),
                new KeyValue(row.prefHeightProperty(), ALTURA_BASE, Interpolator.EASE_OUT),
                new KeyValue(row.maxHeightProperty(), ALTURA_BASE, Interpolator.EASE_OUT)
        )).play();
    }

    private void resetearFilas() {
        filaExpandida = null;
        for (Node node : tablaJuegos.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow<?> r) {
                r.setMinHeight(ALTURA_BASE);
                r.setPrefHeight(ALTURA_BASE);
                r.setMaxHeight(ALTURA_BASE);
            }
        }
    }

    private Image getImagenPorDefecto() {
        return new Image(getClass().getResourceAsStream("/portadas/sin_portada.jpg"));
    }

    private Image getImagenConCache(String ruta) {
        if (ruta == null || ruta.isEmpty()) return getImagenPorDefecto();
        if (cacheImagenes.containsKey(ruta)) return cacheImagenes.get(ruta);
        Image imagen;
        if (ruta.startsWith("http")) {
            imagen = new Image(ruta, true);
        } else {
            File archivo = new File(ruta);
            if (archivo.exists()) imagen = new Image(archivo.toURI().toString());
            else return getImagenPorDefecto();
        }
        cacheImagenes.put(ruta, imagen);
        return imagen;
    }

    private void mostrarDetalle(Juego juego) {
        // Fade out primero
        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), panelDetalle);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Rellena los datos
            if (juego == null) {
                lblDetalleTitulo.setText("—");
                lblDetalleDesarrolladora.setText("—");
                lblDetalleAnio.setText("—");
                lblDetallePlataformas.setText("—");
                lblDetalleGeneros.setText("—");
                lblDetalleDescripcion.setText("—");
                lblDetalleRatingEstrellas.setText("—");
                lblDetalleRatingNum.setText("—");
                imgPortadaMain.setImage(getImagenPorDefecto());
            } else {
                lblDetalleTitulo.setText(juego.getTitulo());
                lblDetalleDesarrolladora.setText(juego.getDesarrolladora());
                lblDetalleAnio.setText(String.valueOf(juego.getAnioLanzamiento()));
                lblDetallePlataformas.setText(juego.getPlataformas());
                lblDetalleGeneros.setText(juego.getGeneros());
                lblDetalleDescripcion.setText(
                        juego.getDescripcion() != null && !juego.getDescripcion().isBlank()
                                ? juego.getDescripcion() : "Sin descripción"
                );
                double r = juego.getRating();
                if (r > 0) {
                    StringBuilder estrellas = new StringBuilder();
                    for (int i = 1; i <= 5; i++) {
                        if (r >= i) estrellas.append("★");
                        else if (r >= i - 0.5) estrellas.append("✮");
                        else estrellas.append("☆");
                    }
                    lblDetalleRatingEstrellas.setText(estrellas.toString());
                    lblDetalleRatingNum.setText(r + " / 5");
                } else {
                    lblDetalleRatingEstrellas.setText("☆☆☆☆☆");
                    lblDetalleRatingNum.setText("—");
                }
                imgPortadaMain.setImage(getImagenConCache(juego.getRutaPortada()));
            }

            // Fade in + slide desde abajo
            panelDetalle.setTranslateY(18);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), panelDetalle);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            TranslateTransition slide = new TranslateTransition(Duration.millis(200), panelDetalle);
            slide.setFromY(18);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition entrada = new ParallelTransition(fadeIn, slide);
            entrada.play();
        });
        fadeOut.play();
    }

    private void cargarDatosTabla() {
        resetearFilas();
        String titulo = txtBuscarTitulo.getText();
        String plataforma = comboBuscarPlataforma.getValue();
        String genero = comboBuscarGenero.getValue();
        if (plataforma != null && plataforma.equals("Plataformas")) plataforma = null;
        if (genero != null && genero.equals("Géneros")) genero = null;
        ObservableList<Juego> lista = FXCollections.observableArrayList(
                juegoDAO.buscarJuegos(titulo, plataforma, genero)
        );
        tablaJuegos.setItems(lista);
    }

    @FXML
    private void refrescarTabla() {
        cacheImagenes.clear();
        comboBuscarPlataforma.getItems().clear();
        comboBuscarPlataforma.getItems().add("Plataformas");
        for (models.Plataforma p : juegoDAO.obtenerPlataformas()) {
            comboBuscarPlataforma.getItems().add(p.getNombre());
        }
        comboBuscarPlataforma.getSelectionModel().selectFirst();

        comboBuscarGenero.getItems().clear();
        comboBuscarGenero.getItems().add("Géneros");
        for (models.Genero g : juegoDAO.obtenerGeneros()) {
            comboBuscarGenero.getItems().add(g.getNombre());
        }
        comboBuscarGenero.getSelectionModel().selectFirst();

        cargarDatosTabla();
    }

    @FXML private void abrirFormularioAlta() { abrirModalFormulario(null, "Añadir Juego Nuevo"); }

    @FXML
    private void abrirFormularioEdicion() {
        Juego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            crearAlert(Alert.AlertType.ERROR, "Editar juego", "Debes seleccionar un juego.").showAndWait();
            return;
        }
        abrirModalFormulario(seleccionado, "Editar juego");
    }

    private void abrirModalFormulario(Juego juego, String tituloVentana) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormView.fxml"));
            Parent root = loader.load();
            if (juego != null) loader.<FormController>getController().setJuegoEditar(juego);
            Stage stage = new Stage();
            stage.setTitle(tituloVentana);
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 450, 520);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            cargarDatosTabla();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void eliminarJuego() {
        Juego seleccionado = tablaJuegos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            crearAlert(Alert.AlertType.ERROR, "Eliminar juego", "Selecciona un juego.").showAndWait();
            return;
        }
        Alert confirm = crearAlert(Alert.AlertType.CONFIRMATION, "Confirmar eliminación",
                "¿Seguro que quieres eliminar \"" + seleccionado.getTitulo() + "\"?");
        ButtonType btnEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnEliminar, btnCancelar);
        Button botonEliminar = (Button) confirm.getDialogPane().lookupButton(btnEliminar);
        if (botonEliminar != null) botonEliminar.getStyleClass().add("btn-danger");
        Button botonCancelar = (Button) confirm.getDialogPane().lookupButton(btnCancelar);
        if (botonCancelar != null) botonCancelar.getStyleClass().add("btn-secondary");
        Optional<ButtonType> resultado = confirm.showAndWait();
        if (resultado.isPresent() && resultado.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            String rutaFoto = seleccionado.getRutaPortada();
            if (rutaFoto != null && !rutaFoto.startsWith("http")) {
                File f = new File(rutaFoto);
                if (f.exists()) f.delete();
            }
            juegoDAO.eliminarJuego(seleccionado.getId());
            tablaJuegos.getItems().remove(seleccionado);
            mostrarDetalle(null);
        }
    }

    @FXML private void onTitleBarPressed(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        xOffset = stage.getX() - event.getScreenX();
        yOffset = stage.getY() - event.getScreenY();
    }

    @FXML private void onTitleBarDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }

    @FXML private void cerrarVentana() { ((Stage) tablaJuegos.getScene().getWindow()).close(); }
    @FXML private void minimizarVentana() { ((Stage) tablaJuegos.getScene().getWindow()).setIconified(true); }

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
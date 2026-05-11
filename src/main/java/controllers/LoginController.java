package controllers;

import dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;
import models.Usuario;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private TextField txtRegUsername;
    @FXML private PasswordField txtRegPassword;
    @FXML private PasswordField txtRegPassword2;
    @FXML private TextField txtRegNombre;
    @FXML private Label lblErrorRegistro;

    // Para mover la ventana
    private double xOffset;
    private double yOffset;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        // Permitir hacer login con Enter
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) hacerLogin();
        });
        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) hacerLogin();
        });
    }

    @FXML
    private void hacerLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("Rellena usuario y contraseña.");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuario = dao.login(username, password);

        if (usuario != null) {
            abrirMainView(usuario);
        } else {
            mostrarError("Usuario o contraseña incorrectos.");
            txtPassword.clear();
        }
    }

    private void abrirMainView(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            // Pasarle el usuario al MainController
            MainController mainController = loader.getController();
            mainController.setUsuarioActual(usuario);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle("RobloxGames");

            try {
                Image icon = new Image(getClass().getResourceAsStream("/portadas/robloxapp.png"));
                stage.getIcons().add(icon);
            } catch (Exception ignored) {}

            Scene scene = new Scene(root, 1400, 800);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

            // Cerrar ventana de login
            ((Stage) btnLogin.getScene().getWindow()).close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText("⚠ " + mensaje);
        lblError.setVisible(true);
    }

    @FXML private void onTitleBarPressed(javafx.scene.input.MouseEvent e) {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        xOffset = stage.getX() - e.getScreenX();
        yOffset = stage.getY() - e.getScreenY();
    }

    @FXML private void onTitleBarDragged(javafx.scene.input.MouseEvent e) {
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        stage.setX(e.getScreenX() + xOffset);
        stage.setY(e.getScreenY() + yOffset);
    }

    @FXML private void cerrarVentana() {
        ((Stage) btnLogin.getScene().getWindow()).close();
    }
    @FXML
    private void hacerRegistro() {
        String username = txtRegUsername.getText().trim();
        String password = txtRegPassword.getText();
        String password2 = txtRegPassword2.getText();
        String nombre = txtRegNombre.getText().trim();

        if (username.isEmpty() || password.isEmpty() || nombre.isEmpty()) {
            lblErrorRegistro.setText("⚠ Rellena todos los campos.");
            lblErrorRegistro.setVisible(true);
            return;
        }
        if (!password.equals(password2)) {
            lblErrorRegistro.setText("⚠ Las contraseñas no coinciden.");
            lblErrorRegistro.setVisible(true);
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        boolean ok = dao.registrar(username, password, nombre);
        if (ok) {
            lblErrorRegistro.setStyle("-fx-text-fill: #a6e3a1;");
            lblErrorRegistro.setText("✓ Usuario creado correctamente.");
            lblErrorRegistro.setVisible(true);
        } else {
            lblErrorRegistro.setText("⚠ El usuario ya existe.");
            lblErrorRegistro.setVisible(true);
        }
    }
}
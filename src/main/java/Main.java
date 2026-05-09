import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));

        primaryStage.setTitle("RobloxGames - Gestor de Videojuegos de Roblox");

        // Ponemos iconno
        try {
            Image icon = new Image(getClass().getResourceAsStream("portadas/robloxapp.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
           System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        Scene scene = new Scene(root, 1400, 800);

        // Quitar barra de título del sistema
        primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
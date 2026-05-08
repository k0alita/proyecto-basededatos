import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));

        primaryStage.setTitle("RobloxGames - Gestor de Videojuegos de Roblox");
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
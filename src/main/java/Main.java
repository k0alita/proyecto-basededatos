import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el FXML de la ventana principal
        Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));

        primaryStage.setTitle("RobloxGames - Gestor de Videojuegos de Roblox");
        primaryStage.setScene(new Scene(root, 900, 600));

        // Centrar en pantalla y mostrar
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
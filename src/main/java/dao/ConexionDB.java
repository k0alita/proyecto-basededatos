package dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {
    private static final Properties properties = new Properties();

    // Este bloque static se ejecuta una sola vez al arrancar la app
    static {
        try (InputStream input = ConexionDB.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.err.println("Error: No se encontró el archivo config.properties en resources.");
            } else {
                properties.load(input);
            }
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("Error al cargar la configuración de la base de datos.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}
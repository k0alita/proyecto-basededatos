package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL = "jdbc:mariadb://localhost:3306/robuxgames";
    private static final String USER = "root";
    private static final String PASS = "root"; // Asegúrate de que este es tu password real de MariaDB

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de MariaDB no encontrado.");
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
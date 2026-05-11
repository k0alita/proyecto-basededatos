package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionDB {

    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream input = ConexionDB.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("No se encontró config.properties");
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar config.properties", e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(3000);
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
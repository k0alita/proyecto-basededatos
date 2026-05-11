package dao;

import models.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario login(String username, String password) {
        String sql = "SELECT id_usuario, username, password, nombre_completo FROM usuarios WHERE username = ? AND password = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("nombre_completo")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // null = credenciales incorrectas
    }
    public boolean registrar(String username, String password, String nombreCompleto) {
        String sql = "INSERT INTO usuarios (username, password, nombre_completo) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, nombreCompleto);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Username duplicado u otro error
            return false;
        }
    }
}
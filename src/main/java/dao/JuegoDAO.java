package dao;

import models.Genero;
import models.Juego;
import models.Plataforma;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {

    public List<Juego> buscarJuegos(String titulo, String nombrePlataforma, String nombreGenero) {
        List<Juego> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento, j.ruta_portada, " +
                        "GROUP_CONCAT(DISTINCT p.nombre SEPARATOR ', ') AS plataformas_juego, " +
                        "GROUP_CONCAT(DISTINCT g.nombre SEPARATOR ', ') AS generos_juego " +
                        "FROM juegos j " +
                        "LEFT JOIN juegos_plataformas jp ON j.id_juego = jp.id_juego " +
                        "LEFT JOIN plataformas p ON jp.id_plataforma = p.id_plataforma " +
                        "LEFT JOIN juegos_generos jg ON j.id_juego = jg.id_juego " +
                        "LEFT JOIN generos g ON jg.id_genero = g.id_genero " +
                        "WHERE j.titulo LIKE ? "
        );

        if (nombrePlataforma != null && !nombrePlataforma.trim().isEmpty()) {
            sql.append("AND j.id_juego IN (" +
                    "SELECT jp2.id_juego FROM juegos_plataformas jp2 " +
                    "JOIN plataformas p2 ON jp2.id_plataforma = p2.id_plataforma " +
                    "WHERE p2.nombre = ?) ");
        }

        if (nombreGenero != null && !nombreGenero.trim().isEmpty()) {
            sql.append("AND j.id_juego IN (" +
                    "SELECT jg2.id_juego FROM juegos_generos jg2 " +
                    "JOIN generos g2 ON jg2.id_genero = g2.id_genero " +
                    "WHERE g2.nombre = ?) ");
        }

        sql.append("GROUP BY j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento, j.ruta_portada");

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setString(paramIndex++, "%" + (titulo != null ? titulo : "") + "%");

            if (nombrePlataforma != null && !nombrePlataforma.trim().isEmpty()) {
                ps.setString(paramIndex++, nombrePlataforma);
            }

            if (nombreGenero != null && !nombreGenero.trim().isEmpty()) {
                ps.setString(paramIndex++, nombreGenero);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String plataformas = rs.getString("plataformas_juego");
                    String generos = rs.getString("generos_juego");
                    lista.add(new Juego(
                            rs.getInt("id_juego"),
                            rs.getString("titulo"),
                            rs.getString("desarrolladora"),
                            rs.getInt("anio_lanzamiento"),
                            plataformas != null ? plataformas : "Sin plataforma",
                            generos != null ? generos : "Sin género",
                            rs.getString("ruta_portada")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar juegos: " + e.getMessage());
        }
        return lista;
    }

    public boolean insertarJuegoConTransaccion(Juego juego, List<Integer> idsPlataformas, List<Integer> idsGeneros) {
        String sqlJuego = "INSERT INTO juegos (titulo, desarrolladora, anio_lanzamiento, ruta_portada) VALUES (?, ?, ?, ?)";
        String sqlPlataforma = "INSERT INTO juegos_plataformas (id_juego, id_plataforma) VALUES (?, ?)";
        String sqlGenero = "INSERT INTO juegos_generos (id_juego, id_genero) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            int idJuego;
            try (PreparedStatement ps = conn.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, juego.getTitulo());
                ps.setString(2, juego.getDesarrolladora());
                ps.setInt(3, juego.getAnioLanzamiento());
                ps.setString(4, juego.getRutaPortada());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No se obtuvo el ID generado.");
                    idJuego = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlPlataforma)) {
                for (int id : idsPlataformas) {
                    ps.setInt(1, idJuego);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlGenero)) {
                for (int id : idsGeneros) {
                    ps.setInt(1, idJuego);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean actualizarJuegoConTransaccion(Juego juego, List<Integer> idsPlataformas, List<Integer> idsGeneros) {
        String sqlUpdate = "UPDATE juegos SET titulo=?, desarrolladora=?, anio_lanzamiento=?, ruta_portada=? WHERE id_juego=?";
        String sqlDeletePlat = "DELETE FROM juegos_plataformas WHERE id_juego=?";
        String sqlDeleteGen = "DELETE FROM juegos_generos WHERE id_juego=?";
        String sqlInsertPlat = "INSERT INTO juegos_plataformas (id_juego, id_plataforma) VALUES (?, ?)";
        String sqlInsertGen = "INSERT INTO juegos_generos (id_juego, id_genero) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, juego.getTitulo());
                ps.setString(2, juego.getDesarrolladora());
                ps.setInt(3, juego.getAnioLanzamiento());
                ps.setString(4, juego.getRutaPortada());
                ps.setInt(5, juego.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDeletePlat)) {
                ps.setInt(1, juego.getId()); ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteGen)) {
                ps.setInt(1, juego.getId()); ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertPlat)) {
                for (int id : idsPlataformas) { ps.setInt(1, juego.getId()); ps.setInt(2, id); ps.addBatch(); }
                ps.executeBatch();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertGen)) {
                for (int id : idsGeneros) { ps.setInt(1, juego.getId()); ps.setInt(2, id); ps.addBatch(); }
                ps.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void eliminarJuego(int idJuego) {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM juegos WHERE id_juego=?")) {
            ps.setInt(1, idJuego);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Integer> obtenerIdsPlataformasDeJuego(int idJuego) {
        return obtenerIds("SELECT id_plataforma FROM juegos_plataformas WHERE id_juego=?", idJuego);
    }

    public List<Integer> obtenerIdsGenerosDeJuego(int idJuego) {
        return obtenerIds("SELECT id_genero FROM juegos_generos WHERE id_juego=?", idJuego);
    }

    private List<Integer> obtenerIds(String sql, int idJuego) {
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ids;
    }

    public List<Plataforma> obtenerPlataformas() {
        List<Plataforma> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_plataforma, nombre, fabricante FROM plataformas");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Plataforma(rs.getInt(1), rs.getString(2), rs.getString(3)));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Genero> obtenerGeneros() {
        List<Genero> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_genero, nombre FROM generos");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Genero(rs.getInt(1), rs.getString(2)));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}
package dao;

import models.Genero;
import models.Juego;
import models.Plataforma;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JuegoDAO {

    public List<Juego> buscarJuegos(String titulo, String nombrePlataforma) {
        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento, j.ruta_portada, " +
                "GROUP_CONCAT(DISTINCT p.nombre SEPARATOR ', ') AS plataformas_juego, " +
                "GROUP_CONCAT(DISTINCT g.nombre SEPARATOR ', ') AS generos_juego " +
                "FROM juegos j " +
                "LEFT JOIN juegos_plataformas jp ON j.id_juego = jp.id_juego " +
                "LEFT JOIN plataformas p ON jp.id_plataforma = p.id_plataforma " +
                "LEFT JOIN juegos_generos jg ON j.id_juego = jg.id_juego " +
                "LEFT JOIN generos g ON jg.id_genero = g.id_genero " +
                "WHERE j.titulo LIKE ? " +
                "GROUP BY j.id_juego, j.titulo, j.desarrolladora, j.anio_lanzamiento, j.ruta_portada ";

        // Si han seleccionado una plataforma, añadimos el filtro HAVING a la SQL
        if (nombrePlataforma != null && !nombrePlataforma.trim().isEmpty()) {
            sql += "HAVING plataformas_juego LIKE ? ";
        }

        try (Connection conexion = ConexionDB.getConnection();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            // Parámetro 1: El título (si es null, busca todos)
            ps.setString(1, "%" + (titulo != null ? titulo : "") + "%");

            // Parámetro 2: Si hay plataforma, le pasamos el string
            if (nombrePlataforma != null && !nombrePlataforma.trim().isEmpty()) {
                ps.setString(2, "%" + nombrePlataforma + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String plataformas = rs.getString("plataformas_juego");
                    String generos = rs.getString("generos_juego");

                    if (plataformas == null) plataformas = "Sin plataforma";
                    if (generos == null) generos = "Sin género";

                    // Ya no hacemos el filtrado en Java
                    // Lo que llega en el ResultSet

                    Juego juego = new Juego(
                            rs.getInt("id_juego"),
                            rs.getString("titulo"),
                            rs.getString("desarrolladora"),
                            rs.getInt("anio_lanzamiento"),
                            plataformas,
                            generos,
                            rs.getString("ruta_portada")
                    );
                    lista.add(juego);
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

        Connection conexion = null;
        try {
            conexion = ConexionDB.getConnection();
            conexion.setAutoCommit(false);

            try (PreparedStatement psJuego = conexion.prepareStatement(sqlJuego, Statement.RETURN_GENERATED_KEYS)) {
                psJuego.setString(1, juego.getTitulo());
                psJuego.setString(2, juego.getDesarrolladora());
                psJuego.setInt(3, juego.getAnioLanzamiento());
                psJuego.setString(4, juego.getRutaPortada()); // NUEVO
                psJuego.executeUpdate();

                try (ResultSet rs = psJuego.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idJuego = rs.getInt(1);

                        try (PreparedStatement psPlat = conexion.prepareStatement(sqlPlataforma)) {
                            for (int idPlat : idsPlataformas) {
                                psPlat.setInt(1, idJuego);
                                psPlat.setInt(2, idPlat);
                                psPlat.executeUpdate();
                            }
                        }

                        try (PreparedStatement psGen = conexion.prepareStatement(sqlGenero)) {
                            for (int idGen : idsGeneros) {
                                psGen.setInt(1, idJuego);
                                psGen.setInt(2, idGen);
                                psGen.executeUpdate();
                            }
                        }
                    }
                }
            }

            conexion.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error en la transacción. Haciendo rollback ...");
            if (conexion != null) {
                try { conexion.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conexion != null) {
                try { conexion.setAutoCommit(true); conexion.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public void eliminarJuego(int idJuego) {
        String sql = "DELETE FROM juegos WHERE id_juego = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJuego);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Integer> obtenerIdsPlataformasDeJuego(int idJuego) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_plataforma FROM juegos_plataformas WHERE id_juego = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJuego);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_plataforma"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public List<Integer> obtenerIdsGenerosDeJuego(int idJuego) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_genero FROM juegos_generos WHERE id_juego = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJuego);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_genero"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public boolean actualizarJuegoConTransaccion(Juego juego, List<Integer> idsPlataformas, List<Integer> idsGeneros) {
        String sqlUpdateJuego = """
        UPDATE juegos
        SET titulo = ?, desarrolladora = ?, anio_lanzamiento = ?, ruta_portada = ?
        WHERE id_juego = ?
    """;

        String sqlDeletePlataformas = "DELETE FROM juegos_plataformas WHERE id_juego = ?";
        String sqlDeleteGeneros = "DELETE FROM juegos_generos WHERE id_juego = ?";
        String sqlInsertPlataforma = "INSERT INTO juegos_plataformas (id_juego, id_plataforma) VALUES (?, ?)";
        String sqlInsertGenero = "INSERT INTO juegos_generos (id_juego, id_genero) VALUES (?, ?)";

        Connection conn = null;

        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psJuego = conn.prepareStatement(sqlUpdateJuego)) {
                psJuego.setString(1, juego.getTitulo());
                psJuego.setString(2, juego.getDesarrolladora());
                psJuego.setInt(3, juego.getAnioLanzamiento());
                psJuego.setString(4, juego.getRutaPortada());
                psJuego.setInt(5, juego.getId());
                psJuego.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDeletePlataformas)) {
                ps.setInt(1, juego.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteGeneros)) {
                ps.setInt(1, juego.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertPlataforma)) {
                for (int idPlat : idsPlataformas) {
                    ps.setInt(1, juego.getId());
                    ps.setInt(2, idPlat);
                    ps.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsertGenero)) {
                for (int idGen : idsGeneros) {
                    ps.setInt(1, juego.getId());
                    ps.setInt(2, idGen);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public List<Plataforma> obtenerPlataformas() {
        List<Plataforma> lista = new ArrayList<>();
        String sql = "SELECT id_plataforma, nombre, fabricante FROM plataformas";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Si 'fabricante' no existe en tu tabla, puedes poner "" en su lugar
                lista.add(new Plataforma(
                        rs.getInt("id_plataforma"),
                        rs.getString("nombre"),
                        rs.getString("fabricante")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Genero> obtenerGeneros() {
        List<Genero> lista = new ArrayList<>();
        String sql = "SELECT id_genero, nombre FROM generos";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Genero(
                        rs.getInt("id_genero"),
                        rs.getString("nombre")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
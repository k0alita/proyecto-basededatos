package models;

public class Juego {
    private int id;
    private String titulo;
    private String desarrolladora;
    private int anioLanzamiento;
    private String plataformas;
    private String generos;
    private String rutaPortada; // NUEVO

    public Juego(int id, String titulo, String desarrolladora, int anioLanzamiento,
                 String plataformas, String generos, String rutaPortada) {
        this.id = id;
        this.titulo = titulo;
        this.desarrolladora = desarrolladora;
        this.anioLanzamiento = anioLanzamiento;
        this.plataformas = plataformas;
        this.generos = generos;
        this.rutaPortada = rutaPortada;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDesarrolladora() { return desarrolladora; }
    public int getAnioLanzamiento() { return anioLanzamiento; }
    public String getPlataformas() { return plataformas; }
    public String getGeneros() { return generos; }
    public String getRutaPortada() { return rutaPortada; }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDesarrolladora(String desarrolladora) {
        this.desarrolladora = desarrolladora;
    }

    public void setAnioLanzamiento(int anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }

    public void setPlataformas(String plataformas) {
        this.plataformas = plataformas;
    }

    public void setGeneros(String generos) {
        this.generos = generos;
    }

    public void setRutaPortada(String rutaPortada) {
        this.rutaPortada = rutaPortada;
    }
}
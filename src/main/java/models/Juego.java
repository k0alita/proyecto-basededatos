package models;

public class Juego {
    private int id;
    private String titulo;
    private String desarrolladora;
    private int anioLanzamiento;
    private String plataformas;
    private String generos;
    private String rutaPortada;
    private String descripcion;
    private double rating;
    private String creadoPor;

    public Juego(int id, String titulo, String desarrolladora, int anioLanzamiento,
                 String plataformas, String generos, String rutaPortada,
                 String descripcion, double rating, String creadoPor) {
        this.id = id;
        this.titulo = titulo;
        this.desarrolladora = desarrolladora;
        this.anioLanzamiento = anioLanzamiento;
        this.plataformas = plataformas;
        this.generos = generos;
        this.rutaPortada = rutaPortada;
        this.descripcion = descripcion;
        this.rating = rating;
        this.creadoPor = creadoPor;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDesarrolladora() { return desarrolladora; }
    public int getAnioLanzamiento() { return anioLanzamiento; }
    public String getPlataformas() { return plataformas; }
    public String getGeneros() { return generos; }
    public String getRutaPortada() { return rutaPortada; }
    public String getDescripcion() { return descripcion; }
    public double getRating() { return rating; }

    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDesarrolladora(String desarrolladora) { this.desarrolladora = desarrolladora; }
    public void setAnioLanzamiento(int anioLanzamiento) { this.anioLanzamiento = anioLanzamiento; }
    public void setPlataformas(String plataformas) { this.plataformas = plataformas; }
    public void setGeneros(String generos) { this.generos = generos; }
    public void setRutaPortada(String rutaPortada) { this.rutaPortada = rutaPortada; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRating(double rating) { this.rating = rating; }
    public String getCreadoPor() { return creadoPor; }
}
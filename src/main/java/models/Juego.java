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
}
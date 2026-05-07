package models;

public class Plataforma {
    private int id;
    private String nombre;
    private String fabricante;

    public Plataforma(int id, String nombre, String fabricante) {
        this.id = id;
        this.nombre = nombre;
        this.fabricante = fabricante;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    @Override
    public String toString() {
        return nombre;
    }
}
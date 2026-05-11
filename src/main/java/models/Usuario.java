package models;

public class Usuario {
    private int id;
    private String username;
    private String password;
    private String nombreCompleto;

    public Usuario(int id, String username, String password, String nombreCompleto) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getNombreCompleto() { return nombreCompleto; }
}
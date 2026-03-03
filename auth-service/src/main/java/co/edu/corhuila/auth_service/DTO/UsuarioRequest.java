package co.edu.corhuila.auth_service.DTO;

public class UsuarioRequest {

    private String nombre;
    private String email;
    private String password;
    private String rol;

    public UsuarioRequest() {}

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRol() { return rol; }
}

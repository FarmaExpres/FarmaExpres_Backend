package co.edu.corhuila.auth_service.DTO;

public class CambiarPasswordRequest {

    private String passwordActual;
    private String nuevaPassword;

    public String getPasswordActual() {
        return passwordActual;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setPasswordActual(String passwordActual) {
        this.passwordActual = passwordActual;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}

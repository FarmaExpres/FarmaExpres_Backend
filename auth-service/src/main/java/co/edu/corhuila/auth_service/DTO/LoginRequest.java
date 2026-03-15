package co.edu.corhuila.auth_service.DTO;

public class LoginRequest {

    private String email;
    private String password;

    public LoginRequest() {}

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

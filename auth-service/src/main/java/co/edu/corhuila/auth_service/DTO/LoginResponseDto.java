package co.edu.corhuila.auth_service.DTO;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String token;
    private String type;
    private String email;
    private String role;

    public LoginResponseDto() {}

    public LoginResponseDto(String token, String type, String email, String role) {
        this.token = token;
        this.type = type;
        this.email = email;
        this.role = role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

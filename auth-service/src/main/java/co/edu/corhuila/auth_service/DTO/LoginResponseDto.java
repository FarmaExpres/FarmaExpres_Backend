package co.edu.corhuila.auth_service.DTO;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    private String token;
    private String accessToken;
    private String refreshToken;
    private String type;
    private String email;
    private String role;
    private String name;

    public LoginResponseDto() {}

    public LoginResponseDto(String accessToken, String refreshToken, String type, String email, String role, String name) {
        this.token = accessToken;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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

    public void setName(String name) {
        this.name = name;
    }
}

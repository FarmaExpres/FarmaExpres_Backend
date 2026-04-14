package co.edu.corhuila.auth_service.DTO;

public class UpdateUserRequest {

    private String name;
    private String email;
    private String role;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

}

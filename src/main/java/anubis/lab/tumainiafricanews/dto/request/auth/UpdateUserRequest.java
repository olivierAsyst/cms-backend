package anubis.lab.tumainiafricanews.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Email(message = "Format d'email invalide")
         String email,

        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName,

        @Size(max = 1000)
        String bio,

        String avatarUrl
) {
}

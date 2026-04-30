package anubis.lab.tumainiafricanews.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "L'identifiant est obligatoire")
        String username,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password,

        boolean rememberMe
) {
}

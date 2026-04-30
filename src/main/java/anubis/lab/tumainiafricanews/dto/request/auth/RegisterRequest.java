package anubis.lab.tumainiafricanews.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
        String username,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password,

        @NotBlank(message = "La confirmation du mot de passe est obligatoire")
        String confirmPassword,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 50)
        String lastName
) {
}

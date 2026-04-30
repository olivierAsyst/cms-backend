package anubis.lab.tumainiafricanews.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Le token de rafraîchissement est obligatoire")
        String refreshToken
) {
}

package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.request.auth.*;
import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.service.AuthService;
import anubis.lab.tumainiafricanews.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Requête d'enregistrement reçue pour: {}", request.username());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inscription réussie",
                        authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Connexion réussie",
                        authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Token rafraîchi avec succès",
                        authService.refreshToken(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(
                ApiResponse.success("Déconnexion réussie", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        Long userId = userService.findByUsername(principal.getName()).getId();
        authService.changePassword(userId, request);
        return ResponseEntity.ok(
                ApiResponse.success("Mot de passe changé avec succès", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            Principal principal) {
        var user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(
                ApiResponse.success(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRoles().stream().findFirst().get().getName())
                        .build()));
    }

}

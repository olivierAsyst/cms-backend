package anubis.lab.tumainiafricanews.service;

import anubis.lab.tumainiafricanews.dto.request.auth.AuthResponse;
import anubis.lab.tumainiafricanews.dto.request.auth.ChangePasswordRequest;
import anubis.lab.tumainiafricanews.dto.request.auth.LoginRequest;
import anubis.lab.tumainiafricanews.dto.request.auth.RegisterRequest;
import anubis.lab.tumainiafricanews.entity.auth.RefreshToken;
import anubis.lab.tumainiafricanews.entity.auth.Role;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.exception.BusinessException;
import anubis.lab.tumainiafricanews.exception.ResourceNotFoundException;
import anubis.lab.tumainiafricanews.repository.RefreshTokenRepository;
import anubis.lab.tumainiafricanews.repository.RoleRepository;
import anubis.lab.tumainiafricanews.repository.UserRepository;
import anubis.lab.tumainiafricanews.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    //private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        log.info("Tentative d'enregistrement pour: {}", request.username());

        // Vérifications
        if (!request.password().equals(request.confirmPassword())) {
            throw new BusinessException("Les mots de passe ne correspondent pas");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Ce nom d'utilisateur est déjà pris");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Cet email est déjà utilisé");
        }

        // Création de l'utilisateur
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rôle USER non trouvé"));

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .roles(Set.of(userRole))
                .enabled(true) // À mettre false si vérification email
                .build();

        user = userRepository.save(user);

        log.info("Utilisateur créé avec succès: {}", user.getUsername());

        // Génération des tokens
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("=== DÉBUT LOGIN === username: {}", request.username());
        try {
            log.info("Appel de authenticationManager.authenticate() pour : {}", request.username());
            AuthenticationManager authManager = authenticationConfiguration.getAuthenticationManager();
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

        } catch (BadCredentialsException e) {
            log.info("Erreur pour login: {}", e.getMessage());
            log.error("=== BAD CREDENTIALS === pour username: {}", request.username());
            log.error("Message exception: {}", e.getMessage());
            throw new BusinessException("Identifiants invalides");
        }catch (Exception e) {
            log.error("=== AUTRE ERREUR AUTHENTIFICATION ===", e);
            log.error("Erreur lors de l'authentification", e);
            throw new BusinessException("Erreur lors de la connexion");
        }
        User user = userRepository.findByUsernameOrEmail(request.username(), request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!user.isEnabled()) {
            throw new BusinessException("Compte non activé. Veuillez vérifier votre email.");
        }

        if (!user.isAccountNonLocked()) {
            throw new BusinessException("Compte verrouillé. Veuillez contacter l'administrateur.");
        }

        // Mise à jour dernière connexion
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        System.out.println("Utilisateur connecté: " + user.getUsername() + " à " + user.getLastLogin());
        return generateAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshTokenStr) {
        log.info("Tentative de rafraîchissement du token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException("Token de rafraîchissement invalide"));

        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Token de rafraîchissement expiré ou révoqué");
        }

        User user = refreshToken.getUser();

        // Révoquer l'ancien token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(user);
    }

    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("Ancien mot de passe incorrect");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException("Les nouveaux mots de passe ne correspondent pas");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Révoquer tous les tokens de rafraîchissement
        refreshTokenRepository.deleteAllByUser(user);

        log.info("Mot de passe changé pour l'utilisateur: {}", user.getUsername());
    }

    private AuthResponse generateAuthResponse(User user) {
        String role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        String accessToken = jwtService.generateToken(user.getUsername(), role);
        String refreshTokenStr = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24h
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(role)
                        .build())
                .build();
    }

    private String createRefreshToken(User user) {
        // Supprimer les anciens tokens
        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7)) // 7 jours
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

}

package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.request.auth.UpdateUserRequest;
import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole(EDITOR)")
    public ResponseEntity<ApiResponse<User>> getProfile(Principal principal) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.findByUsername(principal.getName())));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole(EDITOR)")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(
                ApiResponse.success("Profil mis à jour",
                        userService.updateProfile(user.getId(), request)));
    }

    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(
            @RequestParam String username) {
        return ResponseEntity.ok(
                ApiResponse.success(!userService.existsByUsername(username)));
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(
                ApiResponse.success(!userService.existsByEmail(email)));
    }

    // Endpoints Admin
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(userService.findAll()));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.findById(id)));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("Utilisateur supprimé", null));
    }

    @PatchMapping("/admin/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        userService.toggleUserStatus(id, enabled);
        return ResponseEntity.ok(
                ApiResponse.success("Statut de l'utilisateur modifié", null));
    }
}

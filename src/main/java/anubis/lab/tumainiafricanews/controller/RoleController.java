package anubis.lab.tumainiafricanews.controller;

import anubis.lab.tumainiafricanews.dto.response.ApiResponse;
import anubis.lab.tumainiafricanews.entity.auth.Role;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final UserService userService;

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Role>>> getAllUsers() {
        return ResponseEntity.ok(
                ApiResponse.success(userService.findAllRole()));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.findRoleById(id)));
    }

}

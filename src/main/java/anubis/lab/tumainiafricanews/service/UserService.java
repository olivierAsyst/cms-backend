package anubis.lab.tumainiafricanews.service;

import anubis.lab.tumainiafricanews.dto.request.auth.UpdateUserRequest;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.exception.BusinessException;
import anubis.lab.tumainiafricanews.exception.ResourceNotFoundException;
import anubis.lab.tumainiafricanews.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé: " + username));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(Long userId, UpdateUserRequest request) {
        User user = findById(userId);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new BusinessException("Cet email est déjà utilisé");
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }

        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.bio() != null) {
            user.setBio(request.bio());
        }

        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        user = userRepository.save(user);
        log.info("Profil mis à jour pour l'utilisateur: {}", user.getUsername());

        return user;
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("Utilisateur supprimé: {}", userId);
    }

    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = findById(userId);
        user.setEnabled(enabled);
        userRepository.save(user);
        log.info("Statut de l'utilisateur {} changé à: {}", user.getUsername(), enabled);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}

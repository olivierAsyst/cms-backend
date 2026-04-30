package anubis.lab.tumainiafricanews.config;

import anubis.lab.tumainiafricanews.entity.auth.Role;
import anubis.lab.tumainiafricanews.entity.auth.User;
import anubis.lab.tumainiafricanews.repository.RoleRepository;
import anubis.lab.tumainiafricanews.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info("Initialisation des rôles...");
            roleRepository.save(new Role("USER", "Utilisateur standard"));
            roleRepository.save(new Role("EDITOR", "Éditeur de contenu"));
            roleRepository.save(new Role("ADMIN", "Administrateur système"));
            log.info("Rôles initialisés avec succès");
        }

        if (!userRepository.existsByUsername("admin")) {
            log.info("Création de l'administrateur par défaut...");
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow();

            User admin = User.builder()
                    .username("admin")
                    .email("admin@newsplatform.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .firstName("Admin")
                    .lastName("System")
                    .roles(Set.of(adminRole))
                    .enabled(true)
                    .build();
            //bonix@123, uzia@123
            userRepository.save(admin);
            log.info("Administrateur créé: admin / Admin123!");
        }
    }
}

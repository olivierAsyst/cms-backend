package anubis.lab.tumainiafricanews.repository;

import anubis.lab.tumainiafricanews.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

//    @Query("SELECT u FROM User u WHERE u.verificationToken = :token")
//    Optional<User> findByVerificationToken(@Param("token") String token);
//
//    @Query("SELECT u FROM User u WHERE u.resetPasswordToken = :token")
//    Optional<User> findByResetPasswordToken(@Param("token") String token);
//
//    long countByLastLoginAfter(LocalDateTime dateTime);
//
//    @Query("SELECT u FROM User u WHERE u.verificationTokenExpiry < :now AND u.enabled = false")
//    List<User> findUnverifiedExpiredUsers(@Param("now") LocalDateTime now);
}

package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> getById(long id);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    void deleteById(long id);

    boolean existsById(long id);
}

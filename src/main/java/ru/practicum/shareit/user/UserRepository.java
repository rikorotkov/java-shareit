package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();

    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);

    User save(User user);

    User update(User user);

    void deleteById(long id);

    boolean existById(long id);
}

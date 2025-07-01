package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    @Query("SELECT r FROM ItemRequest r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<ItemRequest> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT r FROM ItemRequest r LEFT JOIN FETCH r.items WHERE r.author.id = :userId")
    List<ItemRequest> findAllByAuthorIdWithItems(@Param("userId") Long userId);

    @Query("SELECT r FROM ItemRequest r LEFT JOIN FETCH r.items WHERE r.author.id <> :userId ORDER BY r.created")
    List<ItemRequest> findAllByAuthorIdNotOrderByCreatedWithItems(@Param("userId") Long userId);
}

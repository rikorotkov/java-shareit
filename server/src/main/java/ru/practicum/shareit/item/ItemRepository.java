package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(long userId);

    @Query("""
            SELECT i FROM Item i
                WHERE available = TRUE AND (i.name ILIKE %:text% OR i.description ILIKE %:text%)
            """)
    List<Item> findByText(@Param("text") String text);
}

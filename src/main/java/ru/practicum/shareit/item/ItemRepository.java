package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> findAll();

    List<Item> findByUserId(long userId);

    Optional<Item> findById(long id);

    Item save(Item item);

    Item update(Item item);

    boolean existById(long id);

    List<Item> findByDescription(String desc);
}

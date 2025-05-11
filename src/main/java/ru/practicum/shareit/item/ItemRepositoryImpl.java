package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0L);

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> findByUserId(long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwner(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Item save(Item item) {
        long id = nextId();
        if (item.getId() == null) {
            item.setId(id);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new ResourceNotFoundException("Предмет не найден id - " + item.getId());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean existById(long id) {
        return items.containsKey(id);
    }

    private long nextId() {
        return ID_GENERATOR.getAndIncrement();
    }
}

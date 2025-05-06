package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> findByUserId(long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(idCounter++);
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

    @Override
    public List<Item> findByDescription(String desc) {
        if (desc == null || desc.isEmpty()) {
            return List.of();
        }
        String lowerCaseDesc = desc.toLowerCase();
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(lowerCaseDesc) ||
                        item.getDescription().toLowerCase().contains(lowerCaseDesc)) &&
                        item.getAvailable())
                .collect(Collectors.toList());
    }
}

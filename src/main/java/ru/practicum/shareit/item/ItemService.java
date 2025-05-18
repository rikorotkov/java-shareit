package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemDto create(Long userId, ItemCreateDto itemDto) {
        userService.getById(userId);
        Item item = itemMapper.toItem(itemDto, userId);
        log.info("Creating new item: {}", item);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new UserIsNotOwnerException("User is not owner id - " + userId);
        }

        itemMapper.updateItem(itemDto, existingItem);
        log.info("Updating existing item: {}", existingItem);
        return itemMapper.toDto(itemRepository.save(existingItem));
    }

    public ItemDto getById(Long itemId, Long userId) {
        userService.getById(userId);
        log.info("Getting item by id: {}", itemId);
        return itemRepository.findById(itemId)
                .map(itemMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));
    }

    public List<ItemDto> getAllByUser(Long userId) {
        log.info("Getting all items by user: {}", userId);
        return itemRepository.findByOwnerIdOrderById(userId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.info("Searching for items with text {}", text);
        return itemRepository.searchAvailableItems(text.toLowerCase()).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

}

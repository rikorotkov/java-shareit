package ru.practicum.shareit.item;

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

    public ItemDto create(Long userId, ItemCreateDto itemDto) {
        userService.getById(userId);
        Item item = ItemMapper.toItem(itemDto, userId);
        log.info("Creating new item: {}", item);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    public ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));

        if (!existingItem.getOwner().equals(userId)) {
            throw new UserIsNotOwnerException("User is not owner id - " + userId);
        }

        ItemMapper.updateItem(existingItem, itemDto);
        log.info("Updating existing item: {}", existingItem);
        return ItemMapper.toItemDto(itemRepository.update(existingItem));
    }

    public ItemDto getById(Long itemId, Long userId) {
        userService.getById(userId);
        log.info("Getting item by id: {}", itemId);
        return itemRepository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));
    }

    public List<ItemDto> getAllByUser(Long userId) {
        log.info("Getting all items by user: {}", userId);
        return itemRepository.findByUserId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.info("Searching for items with text {}", text);
        return itemRepository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

}

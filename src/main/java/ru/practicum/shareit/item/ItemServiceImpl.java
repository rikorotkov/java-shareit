package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
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
            throw new UserIsNotOwnerException("User is not owner");
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
        return itemRepository.findByOwnerId(userId).stream()
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

    @Override
    public List<ItemDto> getItemsByUserId(long userId) {
        log.info("Getting items by user: {}", userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(itemMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto createComment(CommentDto dto, long userId, long itemId) {
        log.info("Creating comment for user: {} item: {}", userId, itemId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));

        List<Booking> byBookerIdAndItemId = bookingRepository.findByBookerIdAndItemId(userId, itemId);

        boolean hasPastBooking = byBookerIdAndItemId.stream()
                .anyMatch(booking -> booking.getEnd().isBefore(LocalDateTime.now()));

        if (hasPastBooking) {
            Comment saved = commentRepository.save(itemMapper.toComment(dto, item, user));
            return itemMapper.toCommentDto(saved);
        } else {
            throw new ValidationException("User has not ever booked item");
        }
    }

}

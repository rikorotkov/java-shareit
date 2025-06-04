package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
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

    @Override
    public ItemDto create(Long userId, ItemCreateDto itemDto) {
        User owner = userService.getEntityById(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        log.info("Creating new item: {}", item);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new UserIsNotOwnerException("User is not owner id - " + userId);
        }

        ItemMapper.updateItem(existingItem, itemDto);
        log.info("Updating existing item: {}", existingItem);
        return ItemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        userService.getById(userId);
        log.info("Getting item by id: {}", itemId);
        return itemRepository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId) {
        log.info("Getting all items by user: {}", userId);
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        log.info("Searching for items with text {}", text);
        return itemRepository.findByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto pastComment(CommentDto commentDto, Long userId, Long itemId) {
        log.info("Posting comment to item: {}", commentDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found id - " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found id - " + itemId));

        List<Booking> findByBookerAndItem = bookingRepository.findByBookerIdAndItemId(user.getId(), item.getId());

        boolean wasBooked = findByBookerAndItem.stream()
                .anyMatch(booking -> booking.getEnd().isBefore(LocalDateTime.now()));

        if (wasBooked) {
            Comment saved = commentRepository.save(ItemMapper.toComment(commentDto, user, item));
            return ItemMapper.toCommentDto(saved);
        } else {
            throw new ValidationException("User has not booked item");
        }
    }

}

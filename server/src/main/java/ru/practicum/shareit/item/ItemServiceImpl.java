package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto createItem(ItemCreateDto itemDto, long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("There is no user with id=" + userId);
        }

        Item item = ItemMapper.toItem(itemDto, userId);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found with id=" + itemDto.getRequestId()));
            item.setItemRequest(request);
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(ItemCreateDto itemDto, long userId, long itemId) {
        Item toUpdate = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no item with id=" + itemId));
        if (toUpdate.getOwner().getId() != userId) {
            throw new ValidationException("Can't change item's owner");
        }
        updateNotNullFields(itemDto, toUpdate);
        Item saved = itemRepository.save(toUpdate);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto getItemById(long id, long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no item with id=" + id));

        if (item.getOwner().getId() == userId) {
            return ItemMapper.toItemDtoForOwner(item);
        } else {
            if (!userRepository.existsById(userId)) {
                throw new ResourceNotFoundException("There is no user with id=" + userId);
            }
            return ItemMapper.toItemDto(item);
        }
    }

    @Override
    public List<ItemDto> getItemByUserId(long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDtoForOwner)
                .toList();
    }

    @Override
    public List<ItemDto> searchByText(String text, long userId) {
        return itemRepository.findByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();

    }

    @Override
    public CommentDto createComment(CommentDto dto, long userId, long itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id=" + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no item with id=" + itemId));

        List<Booking> byBookerIdAndItemId = bookingRepository.findByBookerIdAndItemId(userId, itemId);

        boolean hasPastBooking = byBookerIdAndItemId.stream()
                .anyMatch(booking -> booking.getEnd().isBefore(LocalDateTime.now()));

        if (hasPastBooking) {
            Comment saved = commentRepository.save(ItemMapper.toComment(dto, item, user));
            return ItemMapper.toCommentDto(saved);
        } else {
            throw new ValidationException("User has not ever booked item");
        }
    }

    private static void updateNotNullFields(ItemCreateDto itemDto, Item toUpdate) {
        if (itemDto.getName() != null) {
            toUpdate.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            toUpdate.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            toUpdate.setAvailable(itemDto.getAvailable());
        }
    }

}

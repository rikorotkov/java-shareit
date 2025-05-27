package ru.practicum.shareit.item.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ItemMapper {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                item.getLastBooking() != null ? BookingMapper.toBookingDto(item.getLastBooking()) : null,
                item.getNextBooking() != null ? BookingMapper.toBookingDto(item.getNextBooking()) : null,
                item.getComments() != null ? CommentMapper.toDtoList(item.getComments()) : null
        );
    }

    public Item toItem(ItemDto itemDto) {
        Item item = new Item();

        User user = userRepository.findById(itemDto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        item.setOwner(user);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

    public Comment toComment(CommentDto dto, long userId, long itemId) {
        Comment comment = new Comment();

        comment.setText(dto.getText());
        comment.setAuthor(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));

        comment.setItem(itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found")));

        return comment;
    }

    public CommentDto postComment(CommentDto dto, long userId, long itemId) {
        if (!bookingRepository.existsByItem_IdAndBooker_IdAndEndBefore(itemId, userId, LocalDateTime.now())) {
            throw new ValidationException("User cannot book comment");
        }

        Comment comment = toComment(dto, userId, itemId);

        commentRepository.save(comment);
        return CommentMapper.toDto(comment);
    }
}

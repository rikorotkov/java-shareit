package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {

    public Item toItem(ItemCreateDto dto, Long ownerId) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());

        User owner = new User();
        owner.setId(ownerId);
        item.setOwner(owner);

        if (dto.getRequestId() != null) {
            ItemRequest request = new ItemRequest();
            request.setId(dto.getRequestId());
            item.setItemRequest(request);
        }

        return item;
    }

    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(toCommentDtoList(item.getComments()))
                .build();
    }

    public ItemDto toItemDtoForOwner(Item item) {
        BookingDto last = item.getBookings() != null
                ? item.getBookings().stream()
                .filter(b -> b.getStart() != null && b.getStart().isBefore(java.time.LocalDateTime.now()))
                .map(ItemMapper::toBookingDto)
                .max((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                .orElse(null)
                : null;

        BookingDto next = item.getBookings() != null
                ? item.getBookings().stream()
                .filter(b -> b.getStart() != null && b.getStart().isAfter(java.time.LocalDateTime.now()))
                .map(ItemMapper::toBookingDto)
                .min((b1, b2) -> b1.getStart().compareTo(b2.getStart()))
                .orElse(null)
                : null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(toCommentDtoList(item.getComments()))
                .lastBooking(last)
                .nextBooking(next)
                .build();
    }

    public Comment toComment(CommentDto dto, Item item, User author) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(java.time.LocalDateTime.now());
        return comment;
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public List<CommentDto> toCommentDtoList(List<Comment> comments) {
        return comments == null ? List.of() :
                comments.stream().map(ItemMapper::toCommentDto).collect(Collectors.toList());
    }

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}
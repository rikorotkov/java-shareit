package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.CommentDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private List<CommentDto> comments;

    private BookingDto lastBooking;

    private BookingDto nextBooking;
}

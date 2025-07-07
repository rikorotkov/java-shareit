package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemCreateDto itemDto, long userId);

    ItemDto update(ItemCreateDto itemDto, long userId, long itemId);

    ItemDto getItemById(long id, long userId);

    List<ItemDto> getItemByUserId(long userId);

    List<ItemDto> searchByText(String text, long userId);

    CommentDto createComment(CommentDto dto, long userId, long itemId);
}

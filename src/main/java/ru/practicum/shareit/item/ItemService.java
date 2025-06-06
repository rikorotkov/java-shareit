package ru.practicum.shareit.item;

import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemCreateDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemUpdateDto itemDto);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getAllByUser(Long userId);

    List<ItemDto> searchItems(String text);

    CommentDto pastComment(CommentDto commentDto, Long userId, Long itemId);
}

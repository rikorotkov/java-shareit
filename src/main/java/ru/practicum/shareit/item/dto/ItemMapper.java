package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);

    Item toItem(ItemCreateDto dto, Long userId);

    void updateItem(ItemUpdateDto dto, @MappingTarget Item item);

    CommentDto toCommentDto(Comment comment);

    Comment toComment(CommentDto dto, Item item, User author);
}

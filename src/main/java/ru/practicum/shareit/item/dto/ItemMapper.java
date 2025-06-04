package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        List<CommentDto> commentDtos = item.getComments() != null
                ? item.getComments().stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList())
                : List.of();

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                commentDtos,
                null,
                null
        );
    }

    public static Item toItem(ItemCreateDto dto, User owner) {
        return new Item(
                null,
                dto.getName(),
                dto.getDescription(),
                dto.getAvailable(),
                owner,
                null,
                null,
                null
        );
    }

    public static void updateItem(Item item, ItemUpdateDto dto) {
        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentDto dto, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setAuthor(author);
        comment.setItem(item);
        return comment;
    }
}

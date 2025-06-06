package ru.practicum.shareit.item.comment;

public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getAuthor().getName(),
                comment.getText(),
                comment.getCreated()
        );
    }
}

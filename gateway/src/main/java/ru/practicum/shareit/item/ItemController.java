package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Creating item {}", itemDto);
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody @Validated(Update.class) ItemDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                             @PathVariable @Positive long itemId) {
        log.info("Updating item {}", itemDto);
        return itemClient.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable @Positive long itemId,
                                              @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Getting item id={}", itemId);
        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByUserId(@RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Getting items user id={}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemByText(@RequestParam(required = false) String text,
                                                   @RequestHeader("X-Sharer-User-Id") @Positive long userId) {
        log.info("Searching items by text={}", text);
        return itemClient.getItemsByText(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestBody @Valid CommentDto dto,
                                              @RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                              @PathVariable @Positive long itemId) {
        log.info("Posting comment {} for item id{} by user id{}", dto, itemId, userId);
        return itemClient.postComment(userId, dto, itemId);
    }
}

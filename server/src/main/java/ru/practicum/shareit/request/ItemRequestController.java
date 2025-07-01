package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(@RequestBody ItemRequestDto dto,
                                        @RequestHeader("X-Sharer-User-Id") long userId) {

        return itemRequestService.createRequest(dto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") long userId) {

        return itemRequestService.getOwnersRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable long requestId,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {

        return itemRequestService.getRequestById(requestId, userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {

        return itemRequestService.getAllRequests(userId);
    }
}

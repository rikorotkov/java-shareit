package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(ItemRequestDto dto, long userId);

    List<ItemRequestDto> getOwnersRequests(long userId);

    ItemRequestDto getRequestById(long requestId, long userId);

    List<ItemRequestDto> getAllRequests(long userId, int from, int size);
}

package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, Long userId) {
        if (dto == null) return null;

        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .created(dto.getCreated())
                .author(User.builder().id(userId).build())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest request) {
        if (request == null) return null;

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestDto toDtoWithItems(ItemRequest request) {
        if (request == null) return null;

        List<ItemCreateDto> items = request.getItems() == null ? List.of() :
                request.getItems().stream()
                        .map(ItemRequestMapper::mapItemToCreateDto)
                        .collect(Collectors.toList());

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }

    private static ItemCreateDto mapItemToCreateDto(Item item) {
        if (item == null) return null;

        return ItemCreateDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getItemRequest() != null ? item.getItemRequest().getId() : null)
                .build();
    }
}
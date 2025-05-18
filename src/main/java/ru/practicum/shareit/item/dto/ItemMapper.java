package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toDto(Item item);

    Item toItem(ItemCreateDto dto, Long userId);

    void updateItem(ItemUpdateDto dto, @MappingTarget Item item);
}

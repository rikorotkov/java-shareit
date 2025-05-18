package ru.practicum.shareit.user.dto;

import org.mapstruct.Mapper;
import ru.practicum.shareit.user.model.User;

@Mapper
public interface UserMapper {
    UpdateUserDto toUpdateUserDto(User user);

    User toUser(UpdateUserDto updateUserDto);
}

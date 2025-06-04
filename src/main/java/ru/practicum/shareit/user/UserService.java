package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(Long userId, UpdateUserDto userDto);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);

    User getEntityById(Long userId);
}

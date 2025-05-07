package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto create(UserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = UserMapper.toUser(userDto);
        log.info("Creating user: {}", user);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public UserDto update(Long userId, UpdateUserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        String newEmail = userDto.getEmail();
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new EmailAlreadyExistsException("Email already exists");
            }
            existingUser.setEmail(newEmail);
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.update(existingUser));
    }


    public UserDto getById(Long userId) {
        log.info("Getting user: {}", userId);
        return UserMapper.toUserDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public List<UserDto> getAll() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public void delete(Long userId) {
        if (!userRepository.existById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
        log.info("Deleting user: {}", userId);
        userRepository.deleteById(userId);
    }
}
package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = userMapper.toUser(userDto);
        log.info("Creating user: {}", user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long userId, UpdateUserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String newEmail = userDto.getEmail();
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new EmailAlreadyExistsException("Email already exists");
            }
        }

        userMapper.updateUser(userDto, existingUser);
        log.info("Updating user with id: {}", userId);
        return userMapper.toDto(userRepository.save(existingUser));
    }

    @Transactional
    public UserDto getById(Long userId) {
        log.info("Getting user by id: {}", userId);
        return userMapper.toDto(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)));
    }

    @Transactional
    public List<UserDto> getAll() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        log.info("Deleting user with id: {}", userId);
        userRepository.deleteById(userId);
    }
}
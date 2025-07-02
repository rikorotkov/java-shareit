package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User(1L, "Ivan", "ivan@example.com");
        userDto = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@example.com")
                .build();
    }

    @Test
    void create_whenEmailNotExists_shouldCreateUser() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.create(userDto);

        assertNotNull(created);
        assertEquals(userDto.getEmail(), created.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void create_whenEmailExists_shouldThrowException() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenUserExistsAndEmailNotChanged_shouldUpdateName() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Updated Name")
                .email(user.getEmail())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updated = userService.update(user.getId(), updateDto);

        assertEquals("Updated Name", updated.getName());
        assertEquals(user.getEmail(), updated.getEmail());
    }

    @Test
    void update_whenEmailChangedAndNewEmailNotExists_shouldUpdateEmail() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .name("Ivan")
                .email("newemail@example.com")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updated = userService.update(user.getId(), updateDto);

        assertEquals("newemail@example.com", updated.getEmail());
    }

    @Test
    void update_whenEmailChangedAndNewEmailExists_shouldThrowException() {
        UpdateUserDto updateDto = UpdateUserDto.builder()
                .email("existing@example.com")
                .build();

        User anotherUser = new User(2L, "Ivan Second", "existing@example.com");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(Optional.of(anotherUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.update(user.getId(), updateDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateUserDto updateDto = new UpdateUserDto();
        assertThrows(ResourceNotFoundException.class, () -> userService.update(1L, updateDto));
    }

    @Test
    void getById_whenUserExists_shouldReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto found = userService.getById(1L);

        assertEquals(user.getName(), found.getName());
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void getById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void getAll_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.getAll();

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
    }

    @Test
    void delete_whenUserNotFound_shouldThrowException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getEntityById_whenUserExists_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getEntityById(1L);

        assertNotNull(found);
        assertEquals(user.getId(), found.getId());
    }

    @Test
    void getEntityById_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getEntityById(1L));
    }
}
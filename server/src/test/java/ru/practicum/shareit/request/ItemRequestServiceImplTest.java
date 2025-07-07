package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private ItemRequestDto sampleDto;
    private ItemRequest sampleEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleDto = ItemRequestDto.builder()
                .id(null)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();

        sampleEntity = ItemRequest.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_whenUserExists_shouldSaveAndReturnDto() {
        long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(sampleEntity);

        ItemRequestDto result = service.createRequest(sampleDto, userId);

        assertNotNull(result);
        assertEquals(sampleEntity.getDescription(), result.getDescription());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_whenUserNotFound_shouldThrowException() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.createRequest(sampleDto, userId));

        assertTrue(ex.getMessage().contains("There is no user with id=" + userId));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void getOwnersRequests_whenUserExists_shouldReturnList() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findAllByAuthorIdWithItems(userId)).thenReturn(List.of(sampleEntity));

        List<ItemRequestDto> result = service.getOwnersRequests(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(sampleEntity.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getOwnersRequests_whenUserNotFound_shouldThrowException() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getOwnersRequests(userId));
    }

    @Test
    void getRequestById_whenUserExistsAndRequestFound_shouldReturnDto() {
        long userId = 1L;
        long requestId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findByIdWithItems(requestId)).thenReturn(Optional.of(sampleEntity));

        ItemRequestDto dto = service.getRequestById(requestId, userId);

        assertNotNull(dto);
        assertEquals(sampleEntity.getDescription(), dto.getDescription());
    }

    @Test
    void getRequestById_whenUserNotFound_shouldThrowException() {
        long userId = 1L;
        long requestId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getRequestById(requestId, userId));
    }

    @Test
    void getRequestById_whenRequestNotFound_shouldThrowException() {
        long userId = 1L;
        long requestId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findByIdWithItems(requestId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getRequestById(requestId, userId));
    }

    @Test
    void getAllRequests_whenUserExists_shouldReturnList() {
        long userId = 1L;
        int from = 0;
        int size = 10;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(requestRepository.findByAuthorIdNot(userId, PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(List.of(sampleEntity));

        List<ItemRequestDto> result = service.getAllRequests(userId, from, size);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(sampleEntity.getDescription(), result.get(0).getDescription());
    }

    @Test
    void getAllRequests_whenUserNotFound_shouldThrowException() {
        long userId = 1L;
        int from = 0;
        int size = 10;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.getAllRequests(userId, from, size));
    }
}
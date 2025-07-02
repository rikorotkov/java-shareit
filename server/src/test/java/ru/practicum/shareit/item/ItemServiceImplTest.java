package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemCreateDto itemCreateDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setName("Ivan");
        user.setEmail("ivan@example.com");

        item = new Item();
        item.setId(10L);
        item.setName("Дрерь");
        item.setDescription("Ручная дрель 2000 вт");
        item.setAvailable(true);
        item.setOwner(user);

        itemCreateDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Ручная дрель 2000 вт")
                .available(true)
                .build();
    }

    @Test
    void createItem_WhenUserExists_SavesItem() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setId(10L);
            return i;
        });

        ItemDto created = itemService.createItem(itemCreateDto, user.getId());

        assertNotNull(created);
        assertEquals(itemCreateDto.getName(), created.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_WhenUserNotFound_ThrowsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> itemService.createItem(itemCreateDto, 999L));
        assertTrue(ex.getMessage().contains("There is no user with id="));
    }

    @Test
    void updateItem_WhenOwnerMatches_UpdatesItem() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemCreateDto updateDto = ItemCreateDto.builder()
                .name("Updated Drill")
                .description(null)
                .available(null)
                .build();

        ItemDto updated = itemService.update(updateDto, user.getId(), item.getId());

        assertEquals("Updated Drill", updated.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_WhenOwnerMismatch_ThrowsException() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> itemService.update(itemCreateDto, 999L, item.getId()));
        assertTrue(ex.getMessage().contains("Can't change item's owner"));
    }

    @Test
    void createComment_WhenUserHasPastBooking_SavesComment() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Nice item!");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        Booking pastBooking = new Booking();
        pastBooking.setId(1L);
        pastBooking.setStart(LocalDateTime.now().minusDays(5));
        pastBooking.setEnd(LocalDateTime.now().minusDays(2));
        pastBooking.setItem(item);
        pastBooking.setBooker(user);
        pastBooking.setStatus(Status.APPROVED);

        when(bookingRepository.findByBookerIdAndItemId(user.getId(), item.getId()))
                .thenReturn(List.of(pastBooking));

        when(commentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommentDto savedComment = itemService.createComment(commentDto, user.getId(), item.getId());

        assertNotNull(savedComment);
        assertEquals(commentDto.getText(), savedComment.getText());
        verify(commentRepository).save(any());
    }

    @Test
    void createComment_WhenUserHasNoPastBooking_ThrowsException() {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Классная дрель!");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        when(bookingRepository.findByBookerIdAndItemId(user.getId(), item.getId()))
                .thenReturn(List.of());

        ValidationException ex = assertThrows(ValidationException.class,
                () -> itemService.createComment(commentDto, user.getId(), item.getId()));

        assertTrue(ex.getMessage().contains("User has not ever booked item"));
    }
}
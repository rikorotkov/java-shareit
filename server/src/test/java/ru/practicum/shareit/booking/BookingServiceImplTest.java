package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class BookingServiceImplTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private AutoCloseable closeable;

    private User owner;
    private User booker;
    private Item item;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        owner = new User(1L, "собсвтенник", "owner@mail.com");
        booker = new User(2L, "арендатор", "booker@mail.com");
        item = new Item(1L, "Дрель", "Ручная дрель 2000 вт", true, owner, null, null, null, null, null);

        bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
    }

    @Test
    void createBooking_shouldReturnResponse_whenBookingIsValid() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any())).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        BookingDtoResponse response = bookingService.createBooking(bookingDto);

        assertNotNull(response);
        assertEquals(item.getId(), response.getItem().getId());
        assertEquals(booker.getId(), response.getBooker().getId());
        assertEquals(Status.WAITING, response.getStatus());
    }

    @Test
    void createBooking_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenBookerNotFound() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Booker not found", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenItemIsNotAvailable() {
        item.setAvailable(false);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Item is not available", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenBookerIsOwner() {
        bookingDto.setBookerId(owner.getId());
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Owner cant booking itself item", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenStartInPast() {
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Start date cannot be in the past", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenStartAfterEnd() {
        bookingDto.setStart(LocalDateTime.now().plusHours(3));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Start date cannot be after end date", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenStartEqualsEnd() {
        LocalDateTime now = LocalDateTime.now().plusHours(2);
        bookingDto.setStart(now);
        bookingDto.setEnd(now);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDto));
        assertEquals("Start date cannot be equal to end date", ex.getMessage());
    }
}

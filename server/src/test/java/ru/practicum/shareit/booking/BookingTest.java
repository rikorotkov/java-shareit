package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final User user1 = new User(1L, "User1", "user1@mail.com");
    private final Item item1 = new Item(1L, "Item1", "Description1", true, user1, null, null, null, null, null);

    @Test
    void testGettersAndSetters() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setItem(item1);
        booking.setBooker(user1);
        booking.setStatus(Status.WAITING);
        assertEquals(1L, booking.getId());
        assertEquals(now.plusHours(1), booking.getStart());
        assertEquals(now.plusHours(2), booking.getEnd());
        assertEquals(item1, booking.getItem());
        assertEquals(user1, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        Booking booking = new Booking(1L, now.plusHours(1), now.plusHours(2), item1, user1, Status.WAITING);
        assertEquals(1L, booking.getId());
        assertEquals(now.plusHours(1), booking.getStart());
        assertEquals(now.plusHours(2), booking.getEnd());
        assertEquals(item1, booking.getItem());
        assertEquals(user1, booking.getBooker());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void testNoArgsConstructor() {
        Booking booking = new Booking();
        assertNotNull(booking);
    }

    @Test
    void testToString() {
        Booking booking = new Booking(1L, now.plusHours(1), now.plusHours(2), item1, user1, Status.WAITING);
        assertNotNull(booking.toString());
        assertTrue(booking.toString().contains("Booking"));
    }
}

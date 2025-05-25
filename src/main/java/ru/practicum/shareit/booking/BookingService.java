package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    List<BookingDto> getAllBookings();

    BookingDto createBooking(BookingCreateDto dto, long bookerId);

    BookingDto approveBooking(long ownerId, long bookingId, boolean isApproved);

    BookingDto getBooking(long userId, long bookingId);

    List<BookingDto> getBookingsByBookerId(long bookerId, State state);

    List<BookingDto> getBookingsByItemOwnerId(long ownerId, State state);
}

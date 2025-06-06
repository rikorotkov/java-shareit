package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoResponse createBooking(@RequestBody @Valid BookingDto dto,
                                            @RequestHeader("X-Sharer-User-Id") long userId) {
        dto.setBookerId(userId);
        return bookingService.createBooking(dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@PathVariable long bookingId,
                                      @RequestParam boolean approved,
                                      @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse findBookingById(@PathVariable long bookingId,
                                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> findAllBookings(@RequestParam(defaultValue = "ALL") State state,
                                                    @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.getBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> findUserBookings(@RequestHeader("X-Sharer-User-Id") long userId,
                                                     @RequestParam(required = false, defaultValue = "ALL") State state) {
        return bookingService.getCurrentUserBookings(userId, state);
    }
}

package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.UserIsNotOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    @Transactional
    public BookingDtoResponse createBooking(BookingDto bookingDto) {
        log.info("Create booking: {}", bookingDto);
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        User booker = userRepository.findById(bookingDto.getBookerId())
                .orElseThrow(() -> new ResourceNotFoundException("Booker not found"));
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available");
        }
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new ValidationException("Owner cant booking itself item");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date cannot be in the past");
        }
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Start date cannot be after end date");
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new ValidationException("Start date cannot be equal to end date");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());

        bookingRepository.save(booking);

        ItemDto itemDto = ItemMapper.toItemDto(item);
        UserDto bookerDto = UserMapper.toUserDto(booker);

        return BookingMapper.toBookingDtoResponse(booking, itemDto, bookerDto);
    }

    @Override
    @Transactional
    public BookingDtoResponse approveBooking(long bookingId, long userId, boolean approved) {
        log.info("Approve booking: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        User owner = booking.getItem().getOwner();

        if (owner.getId() != userId) {
            throw new ValidationException("User is not owner of booking");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking saved = bookingRepository.save(booking);
        ItemDto itemDto = ItemMapper.toItemDto(saved.getItem());
        UserDto userDto = UserMapper.toUserDto(saved.getBooker());
        return BookingMapper.toBookingDtoResponse(saved, itemDto, userDto);
    }

    @Override
    public BookingDtoResponse getBookingById(long bookingId, long userId) {
        log.info("Get booking: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        long owner = booking.getItem().getOwner().getId();
        long booker = booking.getBooker().getId();
        if (owner != userId && userId != booker) {
            throw new UserIsNotOwnerException("User is not owner of booking");
        }

        ItemDto itemDto = ItemMapper.toItemDto(booking.getItem());
        UserDto userDto = UserMapper.toUserDto(booking.getBooker());

        return BookingMapper.toBookingDtoResponse(booking, itemDto, userDto);
    }

    @Override
    public List<BookingDtoResponse> getBookings(long userId, State state) {
        log.info("Get bookings: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Booking> bookings = bookingRepository.findByBookerId(userId);
        return predicateState(bookings, state);
    }

    @Override
    public List<BookingDtoResponse> getCurrentUserBookings(long userId, State state) {
        log.info("Get current user bookings: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Booking> userBookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
        return predicateState(userBookings, state);
    }

    private List<BookingDtoResponse> predicateState(List<Booking> bookings, State state) {
        LocalDateTime now = LocalDateTime.now();

        Predicate<Booking> filter = switch (state) {
            case ALL -> b -> true;
            case CURRENT -> b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now);
            case PAST -> b -> b.getEnd().isBefore(now);
            case FUTURE -> b -> b.getStart().isAfter(now);
            case WAITING -> b -> b.getStatus() == Status.WAITING;
            case REJECTED -> b -> b.getStatus() == Status.REJECTED;
        };

        return bookings.stream()
                .filter(filter)
                .map(b -> BookingMapper.toBookingDtoResponse(
                        b,
                        ItemMapper.toItemDto(b.getItem()),
                        UserMapper.toUserDto(b.getBooker())
                ))
                .sorted(Comparator.comparing(BookingDtoResponse::getStart).reversed())
                .collect(Collectors.toList());
    }
}

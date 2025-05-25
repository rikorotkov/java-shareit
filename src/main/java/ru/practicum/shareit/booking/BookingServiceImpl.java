package ru.practicum.shareit.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    public List<BookingDto> getAllBookings() {
        log.info("Getting all bookings");
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    @Transactional
    public BookingDto createBooking(BookingCreateDto dto, long bookerId) {
        validateBooking(dto);

        Item item = itemRepository.findById(dto.getItemId()).orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getOwner().getId() != bookerId) {
            throw new ValidationException("You are not owner of this booking");
        }

        if (!userRepository.existsById(bookerId)) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available");
        }

        boolean isCrossing = bookingRepository.findByItemIdOrderByStart(item.getId()).stream()
                .anyMatch(booking -> isCrossWithOther(booking, dto));

        if (isCrossing) {
            throw new ValidationException("Crossing booking");
        }

        Booking booking = bookingRepository.save(bookingMapper.toBooking(dto, bookerId, item.getName()));
        log.info("Booking created: {}", booking);
        return bookingMapper.toBookingDto(booking);
    }

    public BookingDto approveBooking(long ownerId, long bookingId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no booking with id=" + bookingId));

        if (booking.getItem().getOwner().getId() != ownerId) {
            throw new ValidationException("User don't own item to approve booking");
        }

        if (isApproved && booking.getStatus() == BookingStatus.APPROVED) {
            throw new ValidationException("Booking is already approved");
        } else if (!isApproved && booking.getStatus() == BookingStatus.REJECTED) {
            throw new ValidationException("Booking is already rejected");
        } else {
            booking.setStatus(isApproved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Booking approved: {}", saved);
        return bookingMapper.toBookingDto(saved);
    }

    public BookingDto getBooking(long userId, long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getBooker().getId() != userId && booking.getItem().getOwner().getId() != userId) {
            throw new ValidationException("Can't get booking by user" + userId);
        }

        log.info("Getting booking: {}", booking);
        return bookingMapper.toBookingDto(booking);
    }

    public List<BookingDto> getBookingsByBookerId(long bookerId, State state) {
        Predicate<Booking> predicate = getPredicateByState(state);

        log.info("Getting bookings by booker id {}", bookerId);
        return bookingRepository.findByBookerId(bookerId)
                .stream()
                .filter(predicate)
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    public List<BookingDto> getBookingsByItemOwnerId(long ownerId, State state) {
        if (!userRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("User is not found");
        }

        if (itemRepository.findByOwnerId(ownerId).isEmpty()) {
            throw new ResourceNotFoundException("User don't have any items");
        }

        Predicate<Booking> predicateByState = getPredicateByState(state);
        return bookingRepository.findByItemOwnerId(ownerId).stream()
                .filter(predicateByState)
                .map(bookingMapper::toBookingDto)
                .toList();
    }

    private Predicate<Booking> getPredicateByState(State state) {
        LocalDateTime current = LocalDateTime.now();
        return switch (state) {
            case ALL -> booking -> true;
            case PAST -> booking -> booking.getEnd().isBefore(current);
            case FUTURE -> booking -> booking.getStart().isAfter(current);
            case CURRENT -> booking -> booking.getStart().isBefore(current) && booking.getEnd().isAfter(current);
            case WAITING -> booking -> booking.getStatus() == BookingStatus.WAITING;
            case REJECTED -> booking -> booking.getStatus() == BookingStatus.REJECTED;
        };
    }

    private void validateBooking(BookingCreateDto dto) {
        if (dto.getStart().isBefore(LocalDateTime.now()) ||
                dto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Can't booking item in past");
        }

        if (dto.getStart().isAfter(dto.getEnd())) {
            throw new ValidationException("Start booking can't be after end booking");
        }

        if (dto.getStart().equals(dto.getEnd())) {
            throw new ValidationException("Start booking can't be equal to end booking");
        }
    }

    private boolean isCrossWithOther(Booking booking, BookingCreateDto dto) {
        if (booking.getStart().isBefore(dto.getStart())) {
            return booking.getEnd().isAfter(dto.getStart());
        } else if (dto.getStart().isBefore(booking.getStart())) {
            return dto.getEnd().isAfter(booking.getStart());
        } else {
            return true;
        }
    }
}

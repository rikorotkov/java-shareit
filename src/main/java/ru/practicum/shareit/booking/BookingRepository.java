package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookingId);

    List<Booking> findByItemIdOrderByStart(Long itemId);

    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    List<Booking> findByItemOwnerId(Long ownerId);

    boolean existsByItem_IdAndBooker_IdAndEndBefore(Long itemId, Long bookerId, LocalDateTime endBefore);
}

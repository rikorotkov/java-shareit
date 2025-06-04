package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long itemOwnerId);

    List<Booking> findByBookerId(Long bookerId);
}

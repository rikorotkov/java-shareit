package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ResourceNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createRequest(ItemRequestDto dto, long userId) {
        log.info("Create new request: {}", dto);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("There is no user with id=" + userId);
        }

        ItemRequest entity = ItemRequestMapper.toItemRequest(dto, userId);
        ItemRequest saved = requestRepository.save(entity);

        return ItemRequestMapper.toDto(saved);
    }

    @Override
    public List<ItemRequestDto> getOwnersRequests(long userId) {
        log.info("Get owner requests");
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("There is no user with id=" + userId);
        }

        List<ItemRequest> usersRequests = requestRepository.findAllByAuthorIdWithItems(userId);
        return usersRequests.stream()
                .map(ItemRequestMapper::toDtoWithItems)
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(long requestId, long userId) {
        log.info("Get request by id={}", requestId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("There is no user with id=" + userId);
        }

        ItemRequest request = requestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("There is no request with id=" + requestId));

        return ItemRequestMapper.toDtoWithItems(request);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId, int from, int size) {
        log.info("Get all requests with pagination (not user's own)");
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("There is no user with id=" + userId);
        }

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> otherRequests = requestRepository.findByAuthorIdNot(userId, pageRequest);

        return otherRequests.stream()
                .map(ItemRequestMapper::toDtoWithItems)
                .toList();
    }
}
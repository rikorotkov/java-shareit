package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Slf4j
@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> postRequest(@Positive long userId, @Valid ItemRequestDto dto) {
        log.info("Post request: {}", dto);
        return post("", userId, dto);
    }

    public ResponseEntity<Object> getUsersRequests(long userId) {
        log.info("Get requests: {}", userId);
        return get("", userId);
    }

    public ResponseEntity<Object> getRequestById(@Positive long userId, @Positive long requestId) {
        log.info("Get request by id: {}", requestId);
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getRequestsByUserId(long userId, int from, int size) {
        log.info("Get paged requests by userId={}, from={}, size={}", userId, from, size);
        return get("/all?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }
}

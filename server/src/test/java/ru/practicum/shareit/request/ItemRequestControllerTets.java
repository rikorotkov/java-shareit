package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemRequestControllerTest {

    @InjectMocks
    private ItemRequestController controller;

    @Mock
    private ItemRequestService itemRequestService;

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

    private ItemRequestDto sampleRequestDto;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        sampleRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Test request")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequest_ReturnsCreatedRequest() throws Exception {
        when(itemRequestService.createRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(sampleRequestDto);

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sampleRequestDto))
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sampleRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(sampleRequestDto.getDescription()));

        verify(itemRequestService, times(1)).createRequest(any(ItemRequestDto.class), eq(1L));
    }

    @Test
    void getUserRequests_ReturnsList() throws Exception {
        when(itemRequestService.getOwnersRequests(anyLong()))
                .thenReturn(List.of(sampleRequestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleRequestDto.getId()));

        verify(itemRequestService, times(1)).getOwnersRequests(eq(1L));
    }

    @Test
    void getRequestById_ReturnsRequest() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenReturn(sampleRequestDto);

        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleRequestDto.getId()));

        verify(itemRequestService, times(1)).getRequestById(eq(1L), eq(1L));
    }

    @Test
    void getRequestsByUserId_ReturnsList() throws Exception {
        when(itemRequestService.getAllRequests(anyLong()))
                .thenReturn(List.of(sampleRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleRequestDto.getId()));

        verify(itemRequestService, times(1)).getAllRequests(eq(1L));
    }

    @Test
    void createRequest_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(sampleRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestsByUserId_withoutUserIdHeader_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_whenInvalidRequestId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", "abc")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }
}
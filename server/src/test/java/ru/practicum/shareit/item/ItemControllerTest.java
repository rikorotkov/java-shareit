package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private final ItemDto sampleItem = ItemDto.builder()
            .id(1L)
            .name("Item 1")
            .description("Item description")
            .available(true)
            .build();

    private final ItemCreateDto sampleCreateDto = ItemCreateDto.builder()
            .name("Item 1")
            .description("Item description")
            .available(true)
            .build();

    @Test
    @DisplayName("POST /items - should create item and return 201")
    void createItem_shouldReturnCreatedItem() throws Exception {
        Mockito.when(itemService.createItem(Mockito.any(), Mockito.eq(1L))).thenReturn(sampleItem);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(sampleItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is(sampleItem.getName())))
                .andExpect(jsonPath("$.description", is(sampleItem.getDescription())))
                .andExpect(jsonPath("$.available", is(sampleItem.getAvailable())));
    }

    @Test
    @DisplayName("PATCH /items/{itemId} - should update item")
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        Mockito.when(itemService.update(Mockito.any(), Mockito.eq(1L), Mockito.eq(2L))).thenReturn(sampleItem);

        mockMvc.perform(patch("/items/2")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sampleItem.getId().intValue())));
    }

    @Test
    @DisplayName("GET /items/{id} - should return item by id")
    void getItemById_shouldReturnItem() throws Exception {
        Mockito.when(itemService.getItemById(1L, 1L)).thenReturn(sampleItem);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sampleItem.getId().intValue())));
    }

    @Test
    @DisplayName("GET /items - should return list of items")
    void getItemsByUserId_shouldReturnList() throws Exception {
        Mockito.when(itemService.getItemByUserId(1L)).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /items/search?text=abc - should return matched items")
    void searchItemByText_shouldReturnMatchedItems() throws Exception {
        Mockito.when(itemService.searchByText("abc", 1L)).thenReturn(List.of(sampleItem));

        mockMvc.perform(get("/items/search")
                        .param("text", "abc")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(sampleItem.getId().intValue())));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - should add comment")
    void postComment_shouldReturnCreatedComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Nice item")
                .authorName("User")
                .created(LocalDateTime.now())
                .build();

        Mockito.when(itemService.createComment(Mockito.any(), Mockito.eq(1L), Mockito.eq(2L)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/2/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(commentDto.getId().intValue())))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }

    @Test
    @DisplayName("GET /items/search - empty result should return empty list")
    void searchItemByText_noMatches_shouldReturnEmptyList() throws Exception {
        Mockito.when(itemService.searchByText("nothing", 1L)).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "nothing")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("POST /items - verify service call")
    void createItem_verifyServiceCall() throws Exception {
        Mockito.when(itemService.createItem(Mockito.any(), Mockito.eq(1L)))
                .thenReturn(sampleItem);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateDto)))
                .andExpect(status().isCreated());

        Mockito.verify(itemService).createItem(Mockito.any(ItemCreateDto.class), Mockito.eq(1L));
    }
}

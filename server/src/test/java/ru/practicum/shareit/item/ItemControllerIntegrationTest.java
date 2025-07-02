package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setup() {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user = userRepository.save(user);
    }

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        ItemCreateDto dto = ItemCreateDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        // Сначала создаем объект Item в БД
        Item item = new Item();
        item.setName("Old Name");
        item.setDescription("Old Description");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        ItemCreateDto updateDto = ItemCreateDto.builder()
                .name("New Name")
                .description("New Description")
                .available(false)
                .build();

        mockMvc.perform(patch("/items/" + item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        Item item = new Item();
        item.setName("Item Name");
        item.setDescription("Item Description");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        mockMvc.perform(get("/items/" + item.getId())
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.name").value("Item Name"));
    }

    @Test
    void getItemsByUserId_ShouldReturnList() throws Exception {
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setAvailable(true);
        item1.setOwner(user);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setAvailable(true);
        item2.setOwner(user);
        itemRepository.save(item2);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", anyOf(is("Item 1"), is("Item 2"))))
                .andExpect(jsonPath("$[1].name", anyOf(is("Item 1"), is("Item 2"))));
    }

    @Test
    void searchItemByText_ShouldReturnMatchingItems() throws Exception {
        Item item1 = new Item();
        item1.setName("Screwdriver");
        item1.setDescription("Tool for screwing");
        item1.setAvailable(true);
        item1.setOwner(user);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Hammer");
        item2.setDescription("Tool for hammering");
        item2.setAvailable(true);
        item2.setOwner(user);
        itemRepository.save(item2);

        mockMvc.perform(get("/items/search")
                        .param("text", "screw")
                        .header("X-Sharer-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Screwdriver"));
    }
}
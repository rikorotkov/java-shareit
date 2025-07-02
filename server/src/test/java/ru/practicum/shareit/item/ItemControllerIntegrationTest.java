package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private BookingRepository bookingRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Item item;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user = userRepository.save(user);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);
        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        bookingRepository.save(booking);
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
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.name == 'Test Item')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Item 1')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Item 2')]").exists());
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

    @Test
    void createComment_ShouldReturnCreatedComment() throws Exception {

        String commentJson = "{ \"text\": \"This is a test comment\" }";

        mockMvc.perform(post("/items/" + item.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(commentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("This is a test comment"))
                .andExpect(jsonPath("$.authorName").value(user.getName()))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void createComment_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        String commentJson = "{ \"text\": \"Valid comment\" }";

        mockMvc.perform(post("/items/" + item.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 9999L)
                        .content(commentJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_ShouldReturnNotFound_WhenItemNotFound() throws Exception {
        String commentJson = "{ \"text\": \"Valid comment\" }";

        mockMvc.perform(post("/items/" + 9999L + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", user.getId())
                        .content(commentJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_ShouldReturnBadRequest_WhenUserHasNoPastBooking() throws Exception {
        User noBookingUser = new User();
        noBookingUser.setName("No Booking");
        noBookingUser.setEmail("nobooking@example.com");
        noBookingUser = userRepository.save(noBookingUser);

        String commentJson = "{ \"text\": \"Valid comment\" }";

        mockMvc.perform(post("/items/" + item.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", noBookingUser.getId())
                        .content(commentJson))
                .andExpect(status().isBadRequest());
    }
}
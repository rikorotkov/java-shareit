package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateUserAndGetAll() throws Exception {
        UserDto newUser = UserDto.builder()
                .name("Иван")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                // Проверяем, что в списке есть пользователь с email "ivan@example.com"
                .andExpect(jsonPath("$[*].email", hasItem("ivan@example.com")));
    }

    @Test
    void testUpdateUser() throws Exception {
        UserDto newUser = UserDto.builder()
                .name("Андрей")
                .email("andrey@example.com")
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto createdUser = objectMapper.readValue(response, UserDto.class);

        String updateJson = "{"
                + "\"name\": \"Жанна Обновленная\","
                + "\"email\": \"zhanna.updated@example.com\""
                + "}";

        mockMvc.perform(patch("/users/" + createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Жанна Обновленная"))
                .andExpect(jsonPath("$.email").value("zhanna.updated@example.com"));
    }

    @Test
    void testDeleteUser() throws Exception {
        UserDto newUser = UserDto.builder()
                .name("Мария")
                .email("maria.delete@example.com")
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto createdUser = objectMapper.readValue(response, UserDto.class);

        mockMvc.perform(delete("/users/" + createdUser.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/users/" + createdUser.getId()))
                .andExpect(status().isNotFound());
    }
}
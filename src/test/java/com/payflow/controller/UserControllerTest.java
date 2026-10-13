
package com.payflow.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.payflow.entity.User;
import com.payflow.exception.EmailAlreadyExistsException;
import com.payflow.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {

        User user = new User();
        user.setName("Matheus");
        user.setEmail("matheus@teste.com");

        given(userService.save(org.mockito.ArgumentMatchers.any(User.class)))
                .willReturn(user);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Matheus",
                            "email": "matheus@teste.com"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Matheus"))
                .andExpect(jsonPath("$.email").value("matheus@teste.com"));
    }

    @Test
    void shouldRejectInvalidUser() throws Exception {

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "",
                            "email": "email-invalido"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Nome é obrigatório"))
                .andExpect(jsonPath("$.email").value("E-mail inválido"));
    }

    @Test
    void shouldRejectDuplicatedEmail() throws Exception {

        given(userService.save(org.mockito.ArgumentMatchers.any(User.class)))
                .willThrow(new EmailAlreadyExistsException("E-mail já cadastrado"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Outro Matheus",
                            "email": "matheus@teste.com"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("E-mail já cadastrado"));
    }
}

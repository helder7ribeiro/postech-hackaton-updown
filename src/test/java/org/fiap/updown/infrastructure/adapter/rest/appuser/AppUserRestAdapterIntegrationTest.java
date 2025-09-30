package org.fiap.updown.infrastructure.adapter.rest.appuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.fiap.updown.infrastructure.adapter.persistence.repository.JobRepository;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.CreateAppUserRequest;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.UpdateAppUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class AppUserRestAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobRepository jobRepository;

    private AppUserEntity testUser;

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        appUserRepository.deleteAll();

        testUser = appUserRepository.save(AppUserEntity.builder()
                .email("usuario.existente@teste.com")
                .username("usuario.existente")
                .build());
    }

    @Test
    void deveCriarUsuarioComSucesso() throws Exception {
        CreateAppUserRequest request = new CreateAppUserRequest("novo.usuario.api@teste.com", "novo.usuario.api");

        mockMvc.perform(post("/api/v1/app-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("novo.usuario.api@teste.com"))
                .andExpect(jsonPath("$.username").value("novo.usuario.api"));
    }

    @Test
    void naoDeveCriarUsuario_QuandoEmailJaExiste() throws Exception {
        CreateAppUserRequest request = new CreateAppUserRequest("usuario.existente@teste.com", "usuario.existente");

        mockMvc.perform(post("/api/v1/app-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveBuscarUsuarioPorId() throws Exception {
        mockMvc.perform(get("/api/v1/app-users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.email").value("usuario.existente@teste.com"))
                .andExpect(jsonPath("$.username").value("usuario.existente"));
    }

    @Test
    void deveRetornarNotFound_AoBuscarUsuarioInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/app-users/{id}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarUsuario() throws Exception {
        UpdateAppUserRequest request = new UpdateAppUserRequest("email.atualizado@teste.com", "email.atualizado");

        mockMvc.perform(put("/api/v1/app-users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(jsonPath("$.email").value("email.atualizado@teste.com"))
                .andExpect(jsonPath("$.username").value("email.atualizado"));

        AppUserEntity updatedUser = appUserRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("email.atualizado@teste.com");
        assertThat(updatedUser.getUsername()).isEqualTo("email.atualizado");
    }

    @Test
    void deveDeletarUsuario() throws Exception {
        mockMvc.perform(delete("/api/v1/app-users/{id}", testUser.getId()))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("X-updown-alert"));

        assertThat(appUserRepository.existsById(testUser.getId())).isFalse();
    }
}
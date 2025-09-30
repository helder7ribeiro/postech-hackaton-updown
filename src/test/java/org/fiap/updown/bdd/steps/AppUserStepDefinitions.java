package org.fiap.updown.bdd.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import org.fiap.updown.bdd.TestContext;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.fiap.updown.infrastructure.adapter.persistence.repository.JobRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AppUserStepDefinitions {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private TestContext testContext;

    private final Map<String, AppUserEntity> userCache = new HashMap<>();

    @Dado("que o sistema está limpo")
    public void que_o_sistema_esta_limpo() {
        // Limpa os jobs primeiro devido à chave estrangeira
        jobRepository.deleteAll();
        appUserRepository.deleteAll();
        userCache.clear();
    }

    @Dado("que já existe um usuário com e-mail {string}")
    public void que_ja_existe_um_usuario_com_email(String email) {
        String username = email.split("@")[0];
        AppUserEntity user = AppUserEntity.builder()
                .email(email)
                .username(username)
                .build();
        AppUserEntity savedUser = appUserRepository.save(user);
        userCache.put(email, savedUser);
    }

    @Quando("um usuário envia uma requisição {word} para {string} com o corpo:")
    public void um_usuario_envia_uma_requisicao_com_corpo(String method, String path, String body) throws Exception {
        switch (method.toUpperCase()) {
            case "POST" -> testContext.setResultActions(mockMvc.perform(post(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)));
            case "PUT" -> testContext.setResultActions(mockMvc.perform(put(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)));
            default -> throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        }
    }

    @Quando("um usuário envia uma requisição {word} para a URL do usuário com e-mail {string}")
    public void um_usuario_envia_uma_requisicao_para_url_do_usuario(String method, String email) throws Exception {
        AppUserEntity user = userCache.get(email);
        Assertions.assertNotNull(user, "Usuário com e-mail " + email + " não encontrado no cache do teste.");
        String path = "/api/v1/app-users/" + user.getId();

        switch (method.toUpperCase()) {
            case "GET" -> testContext.setResultActions(mockMvc.perform(get(path)));
            case "DELETE" -> testContext.setResultActions(mockMvc.perform(delete(path)));
            default -> throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        }
    }

    @Quando("um usuário envia uma requisição GET para {string}")
    public void um_usuario_envia_uma_requisicao_get_para(String path) throws Exception {
        testContext.setResultActions(mockMvc.perform(get(path)));
    }

    @Quando("um usuário envia uma requisição PUT para a URL do usuário com e-mail {string} com o corpo:")
    public void um_usuario_envia_uma_requisicao_put_para_url_do_usuario(String email, String body) throws Exception {
        AppUserEntity user = userCache.get(email);
        Assertions.assertNotNull(user, "Usuário com e-mail " + email + " não encontrado no cache do teste.");
        String path = "/api/v1/app-users/" + user.getId();
        testContext.setResultActions(mockMvc.perform(put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)));
    }

    @E("o corpo da resposta deve conter o e-mail {string}")
    public void o_corpo_da_resposta_deve_conter_o_email(String email) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.email").value(email));
    }

    @E("um usuário com e-mail {string} deve existir no banco de dados")
    public void um_usuario_com_email_deve_existir(String email) {
        Assertions.assertTrue(appUserRepository.existsByEmailIgnoreCase(email));
    }

    @E("um usuário com e-mail {string} não deve existir no banco de dados")
    public void um_usuario_com_email_nao_deve_existir(String email) {
        Assertions.assertFalse(appUserRepository.existsByEmailIgnoreCase(email));
    }
}


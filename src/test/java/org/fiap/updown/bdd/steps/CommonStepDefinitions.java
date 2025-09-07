package org.fiap.updown.bdd.steps;

import io.cucumber.java.pt.Entao;
import org.fiap.updown.bdd.TestContext;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class CommonStepDefinitions extends AbstractIntegrationTest {

    @Autowired
    private TestContext testContext;

    @Entao("o status da resposta deve ser {int}")
    public void o_status_da_resposta_deve_ser(int statusCode) throws Exception {
        ResultActions resultActions = testContext.getResultActions();
        Assertions.assertNotNull(resultActions, "ResultActions não foi inicializado. O passo 'Quando' esqueceu de atribuí-lo ao TestContext?");
        resultActions.andExpect(status().is(statusCode));
    }
}
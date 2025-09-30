package org.fiap.updown.bdd;

import io.cucumber.spring.ScenarioScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Um bean com escopo de cenário para compartilhar o estado entre os passos do Cucumber.
 * Uma nova instância desta classe será criada para cada cenário executado.
 */
@Setter
@Getter
@Component
@ScenarioScope
public class TestContext {

    private ResultActions resultActions;

}


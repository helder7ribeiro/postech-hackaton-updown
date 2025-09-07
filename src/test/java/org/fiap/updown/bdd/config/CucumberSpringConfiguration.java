package org.fiap.updown.bdd.config;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@CucumberContextConfiguration
@AutoConfigureMockMvc
public class CucumberSpringConfiguration extends AbstractIntegrationTest {

}


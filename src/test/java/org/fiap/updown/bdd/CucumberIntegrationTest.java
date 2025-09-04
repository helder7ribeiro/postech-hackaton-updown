package org.fiap.updown.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "org.fiap.updown.bdd",
        plugin = {"pretty", "html:target/cucumber-reports/report.html"},
        monochrome = true
)
public class CucumberIntegrationTest {

    @BeforeClass
    public static void setup() {
        System.setProperty("spring.profiles.active", "test");
    }
}


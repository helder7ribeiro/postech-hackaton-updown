package org.fiap.updown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class UpDownApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpDownApplication.class, args);
    }

}

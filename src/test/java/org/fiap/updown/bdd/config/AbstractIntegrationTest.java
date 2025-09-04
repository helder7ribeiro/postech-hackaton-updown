package org.fiap.updown.bdd.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe base abstrata para testes de integração.
 * Inicia o contêiner Docker (LocalStack) uma vez para todos os testes
 * que herdarem dela, e configura dinamicamente as propriedades da aplicação.
 * Esta implementação usa o padrão "Singleton Container" para garantir que o contêiner
 * seja compartilhado entre diferentes classes de teste, resolvendo problemas de
 * tempo de inicialização.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final LocalStackContainer localStackContainer;

    static {
        localStackContainer = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest-amd64"))
                .withServices(LocalStackContainer.Service.SQS)
                .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3);

        localStackContainer.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Aponta para os endpoints do contêiner
        registry.add("app.storage.s3.endpoint", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("app.messaging.sqs.endpoint", () -> localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS).toString());

        // Usa as credenciais e região geradas pelo contêiner
        registry.add("app.storage.s3.accessKey", localStackContainer::getAccessKey);
        registry.add("app.storage.s3.secretKey", localStackContainer::getSecretKey);
        registry.add("app.storage.s3.region", localStackContainer::getRegion);
        registry.add("app.messaging.sqs.accessKey", localStackContainer::getAccessKey);
        registry.add("app.messaging.sqs.secretKey", localStackContainer::getSecretKey);
        registry.add("app.messaging.sqs.region", localStackContainer::getRegion);
    }
}
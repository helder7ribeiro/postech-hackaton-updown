package org.fiap.updown.infrastructure.adapter.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
@Validated
@ConfigurationProperties(prefix = "app.messaging.sqs")
public class SqsMessagingProperties {

    /** Nome da fila (ex.: jobs-events.fifo ou jobs-events) */
    @NotBlank
    private String queueName;

    /** Região (LocalStack usa us-east-1 por padrão) */
    @NotBlank
    private String region = "us-east-1";

    /** Endpoint do SQS (LocalStack): http://localhost:4566 ou http://localstack:4566 */
    private String endpoint;

    /** Criar fila automaticamente se não existir */
    private boolean createQueueIfMissing = true;

    /** Força path para fila FIFO (se terminar com .fifo já será detectado) */
    private Boolean fifo; // se null, infere de queueName

    /** Habilita deduplicação por conteúdo ao criar fila FIFO */
    private boolean contentBasedDeduplication = true;

    /** Credenciais */
    private String accessKey;
    private String secretKey;
    private String sessionToken;

    /** Opcional: delay padrão para mensagens (0-900) */
    private Integer defaultDelaySeconds = 0;

    /** Estratégia simples para MessageGroupId em FIFO: "user", "static" */
    private String messageGroupStrategy = "user"; // user -> por userId, static -> valor fixo abaixo

    /** Se strategy=static, usar esse valor */
    private String staticMessageGroupId = "jobs";
}

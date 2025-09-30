package org.fiap.updown.infrastructure.adapter.message.consumer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter @Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.messaging.sqs.consumer")
public class SqsConsumerProperties {

    /** Nome da fila OU URL. Se queueUrl estiver vazio, o adapter resolve via queueName. */
    private String queueName;
    private String queueUrl;

    /** Endpoint (LocalStack): http://localhost:4566 */
    private String endpoint;

    /** Região */
    @NotBlank
    private String region = "us-east-1";

    /** Credenciais fake p/ LocalStack */
    private String accessKey = "test";
    private String secretKey = "test";

    /** Polling */
    @Min(1) @Max(10)
    private int maxMessages = 10;

    /** Long polling (0..20s) */
    @Min(0) @Max(20)
    private int waitTimeSeconds = 10;

    /** Visibilidade aplicada às mensagens recebidas (se >0) */
    @Min(0) @Max(43200)
    private int visibilityTimeoutSeconds = 0;

    /** Extensão periódica do visibility enquanto processa (0=desabilita) */
    @Min(0) @Max(43200)
    private int extendVisibilitySeconds = 0;

    /** Intervalo do scheduler de poll em ms */
    @Min(100)
    private long pollIntervalMillis = 1000L;

    /** Tamanho do pool de threads de processamento */
    @Min(1)
    private int concurrency = 4;

    /** Habilita/desabilita o consumidor */
    private boolean enabled = true;
}

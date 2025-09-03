package org.fiap.updown.infrastructure.adapter.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.model.Job;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsEventPublisherAdapter implements EventPublisher {

    private final SqsClient sqs;
    private final SqsMessagingProperties props;
    private final ObjectMapper objectMapper;

    private volatile String queueUrl;

    @PostConstruct
    void ensureQueue() {
        this.queueUrl = resolveOrCreateQueue(props.getQueueName(), isFifo());
        log.info("SQS pronto. queueName={}, fifo={}, queueUrl={}", props.getQueueName(), isFifo(), queueUrl);
    }

    @Override
    public void novoVideoRecebido(Job saved) {
        if (queueUrl == null) {
            ensureQueue();
        }

        String body = serialize(saved);

        SendMessageRequest.Builder b = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body);

        if (props.getDefaultDelaySeconds() != null) {
            b.delaySeconds(props.getDefaultDelaySeconds());
        }

        if (isFifo()) {
            String groupId = computeGroupId(saved);
            String dedupId = saved.getId() != null ? saved.getId().toString() : String.valueOf(body.hashCode());
            b.messageGroupId(groupId)
                    .messageDeduplicationId(dedupId);
        }

        SendMessageResponse resp = sqs.sendMessage(b.build());
        log.info("Evento publicado em SQS: messageId={}, jobId={}, userId={}",
                resp.messageId(),
                Optional.ofNullable(saved.getId()).orElse(null),
                saved.getUser() != null ? saved.getUser().getId() : null);
    }

    private String serialize(Job payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new FalhaInfraestruturaException("Falha ao serializar payload SQS", e);
        }
    }

    private String computeGroupId(Job saved) {
        boolean fifo = isFifo();
        if (!fifo) return null;
        String strategy = Optional.ofNullable(props.getMessageGroupStrategy()).orElse("user");
        return switch (strategy) {
            case "static" -> Optional.ofNullable(props.getStaticMessageGroupId()).orElse("jobs");
            case "user" -> (saved.getUser() != null && saved.getUser().getId() != null)
                    ? "user-" + saved.getUser().getId()
                    : "jobs";
            default -> "jobs";
        };
    }

    private boolean isFifo() {
        if (props.getFifo() != null) return props.getFifo();
        return props.getQueueName() != null && props.getQueueName().endsWith(".fifo");
    }

    private String resolveOrCreateQueue(String queueName, boolean fifo) {
        // Tenta obter a URL da fila, pois é a operação mais comum e rápida.
        try {
            GetQueueUrlResponse r = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
            return r.queueUrl();
        } catch (QueueDoesNotExistException e) {
            // Se a fila não existe, verifica se a aplicação deve criá-la.
            if (!props.isCreateQueueIfMissing()) {
                throw new FalhaInfraestruturaException("Fila SQS não existe: " + queueName, e);
            }

            // Inicia a construção da requisição para criar uma nova fila.
            CreateQueueRequest.Builder cb = CreateQueueRequest.builder().queueName(queueName);

            // Utiliza EnumMap para maior eficiência ao trabalhar com chaves do tipo enum (QueueAttributeName).
            Map<QueueAttributeName, String> attrs = new EnumMap<>(QueueAttributeName.class);

            // Adiciona atributos específicos para filas FIFO.
            if (fifo) {
                attrs.put(QueueAttributeName.FIFO_QUEUE, "true");
                if (props.isContentBasedDeduplication()) {
                    attrs.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
                }
            }

            // Adiciona o atraso padrão (delay) na entrega de mensagens, se configurado.
            if (props.getDefaultDelaySeconds() != null && props.getDefaultDelaySeconds() > 0) {
                attrs.put(QueueAttributeName.DELAY_SECONDS, String.valueOf(props.getDefaultDelaySeconds()));
            }

            // Associa os atributos à requisição de criação, se houver algum.
            if (!attrs.isEmpty()) {
                cb.attributes(attrs);
            }

            // Executa a criação da fila no SQS.
            CreateQueueResponse created = sqs.createQueue(cb.build());

            // Retorna a URL da fila recém-criada.
            return created.queueUrl();
        }
    }
}

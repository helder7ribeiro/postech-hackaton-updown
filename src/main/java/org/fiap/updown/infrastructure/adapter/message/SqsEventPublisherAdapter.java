package org.fiap.updown.infrastructure.adapter.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.domain.model.Job;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsEventPublisherAdapter implements EventPublisher {

    private final SqsClient sqs;
    private final SqsMessagingProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            throw new RuntimeException("Falha ao serializar payload SQS", e);
        }
    }

    private String computeGroupId(Job saved) {
        boolean fifo = isFifo();
        if (!fifo) return null;
        String strategy = Optional.ofNullable(props.getMessageGroupStrategy()).orElse("user");
        return switch (strategy) {
            case "static" -> Optional.ofNullable(props.getStaticMessageGroupId()).orElse("jobs");
            case "user"   -> (saved.getUser() != null && saved.getUser().getId() != null)
                                ? "user-" + saved.getUser().getId()
                                : "jobs";
            default       -> "jobs";
        };
    }

    private boolean isFifo() {
        if (props.getFifo() != null) return props.getFifo();
        return props.getQueueName() != null && props.getQueueName().endsWith(".fifo");
    }

    private String resolveOrCreateQueue(String queueName, boolean fifo) {
        // tenta obter a URL
        try {
            GetQueueUrlResponse r = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
            return r.queueUrl();
        } catch (QueueDoesNotExistException e) {
            if (!props.isCreateQueueIfMissing()) {
                throw new IllegalStateException("Fila SQS não existe: " + queueName);
            }
            // cria com atributos
            CreateQueueRequest.Builder cb = CreateQueueRequest.builder().queueName(queueName);
            Map<QueueAttributeName, String> attrs = new HashMap<>();
            if (fifo) {
                attrs.put(QueueAttributeName.FIFO_QUEUE, "true");
                if (props.isContentBasedDeduplication()) {
                    attrs.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
                }
            }
            if (props.getDefaultDelaySeconds() != null && props.getDefaultDelaySeconds() > 0) {
                attrs.put(QueueAttributeName.DELAY_SECONDS, String.valueOf(props.getDefaultDelaySeconds()));
            }
            if (!attrs.isEmpty()) cb = cb.attributes(attrs);

            CreateQueueResponse created = sqs.createQueue(cb.build());
            return created.queueUrl();
        }
    }
}

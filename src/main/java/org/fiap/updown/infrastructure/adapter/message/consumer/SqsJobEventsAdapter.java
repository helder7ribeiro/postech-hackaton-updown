package org.fiap.updown.infrastructure.adapter.message.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driven.UpdateJobUseCase;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.model.Job;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsJobEventsAdapter {

    private final SqsClient sqs;
    private final SqsConsumerProperties props;
    private final UpdateJobUseCase updateUseCase;

    private final ObjectMapper objectMapper;

    private volatile String queueUrl;
    private ExecutorService executor;

    @PostConstruct
    void init() {
        if (!props.isEnabled()) {
            log.warn("SQS consumer DISABLED via config.");
            return;
        }
        this.queueUrl = resolveOrCreateQueue(props.getQueueName());
        this.executor = new ThreadPoolExecutor(
                props.getConcurrency(),
                props.getConcurrency(),
                30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(props.getConcurrency() * 10),
                new ThreadFactory() {
                    private final ThreadFactory tf = Executors.defaultThreadFactory();
                    @Override public Thread newThread(Runnable r) {
                        Thread t = tf.newThread(r);
                        t.setName("sqs-consumer-" + t.getId());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("SQS consumer inicializado. queueUrl={}, concurrency={}, pollInterval={}ms",
                queueUrl, props.getConcurrency(), props.getPollIntervalMillis());
    }

    @PreDestroy
    void shutdown() {
        if (executor != null) executor.shutdownNow();
    }

    @Scheduled(fixedDelayString = "${app.messaging.sqs.consumer.poll-interval-millis:1000}")
    public void poll() {
        if (!props.isEnabled()) return;

        try {
            ReceiveMessageRequest.Builder rb = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(props.getMaxMessages())
                    .waitTimeSeconds(props.getWaitTimeSeconds())
                    .messageAttributeNames("All")
                    .attributeNames(QueueAttributeName.ALL);

            if (props.getVisibilityTimeoutSeconds() > 0) {
                rb = rb.visibilityTimeout(props.getVisibilityTimeoutSeconds());
            }

            ReceiveMessageResponse resp = sqs.receiveMessage(rb.build());
            List<Message> messages = resp.messages();
            if (messages == null || messages.isEmpty()) return;

            for (Message m : messages) {
                executor.submit(() -> processMessage(m));
            }
        } catch (Exception e) {
            log.error("Falha no poll do SQS: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message m) {
        ScheduledExecutorService visExt = null;
        ScheduledFuture<?> visTask = null;
        String body = null;

        try {
            String receipt = m.receiptHandle();
            JsonNode json = objectMapper.readTree(m.body());
            body = json.get("Message").asText();

            Job job = objectMapper.readValue(body, Job.class);

            if (props.getExtendVisibilitySeconds() > 0) {
                visExt = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "sqs-visibility-extender");
                    t.setDaemon(true);
                    return t;
                });
                int extendBy = props.getExtendVisibilitySeconds();
                visTask = visExt.scheduleAtFixedRate(
                        () -> safeExtendVisibility(receipt, extendBy),
                        extendBy / 2L,
                        extendBy / 2L,
                        TimeUnit.SECONDS
                );
            }

            updateUseCase.execute(job);

            deleteMessage(receipt);
            log.info("Mensagem processada e removida. messageId={}, jobId={}",
                    m.messageId(), job.getId());

        } catch (Throwable ex) {
            log.error("Erro ao processar mensagem: {} | body={}", ex.getMessage(), body, ex);
        } finally {
            if (visTask != null) visTask.cancel(true);
            if (visExt != null) visExt.shutdownNow();
        }
    }

    private void deleteMessage(String receipt) {
        try {
            sqs.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receipt)
                    .build());
        } catch (Exception e) {
            log.warn("Falha ao deletar mensagem: {}", e.getMessage(), e);
        }
    }

    private void safeExtendVisibility(String receipt, int seconds) {
        try {
            sqs.changeMessageVisibility(ChangeMessageVisibilityRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receipt)
                    .visibilityTimeout(seconds)
                    .build());
        } catch (Exception e) {
            log.debug("Falha ao mudar visibility: {}", e.getMessage());
        }
    }

    private String resolveOrCreateQueue(String queueName) {
        // Tenta obter a URL da fila, pois é a operação mais comum e rápida.
        try {
            GetQueueUrlResponse r = sqs.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());
            return r.queueUrl();
        } catch (QueueDoesNotExistException e) {
            // Inicia a construção da requisição para criar uma nova fila.
            CreateQueueRequest.Builder cb = CreateQueueRequest.builder().queueName(queueName);

            // Utiliza EnumMap para maior eficiência ao trabalhar com chaves do tipo enum (QueueAttributeName).
            Map<QueueAttributeName, String> attrs = new EnumMap<>(QueueAttributeName.class);

            // Adiciona atributos específicos para filas FIFO.
            attrs.put(QueueAttributeName.FIFO_QUEUE, "true");

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

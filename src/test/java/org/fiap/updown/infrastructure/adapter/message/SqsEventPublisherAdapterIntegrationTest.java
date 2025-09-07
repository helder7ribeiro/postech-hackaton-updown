package org.fiap.updown.infrastructure.adapter.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class SqsEventPublisherAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SqsEventPublisherAdapter sqsEventPublisherAdapter;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private SqsMessagingProperties sqsMessagingProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Limpa a fila SQS antes de cada teste para remover mensagens antigas
        String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsMessagingProperties.getQueueName()).build()).queueUrl();
        sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(queueUrl).build());
    }

    @Test
    void devePublicarEventoDeNovoVideoRecebidoComSucesso() throws Exception {
        AppUser testUser = new AppUser();
        testUser.setId(UUID.randomUUID());
        Job testJob = Job.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .status(JobStatus.RECEIVED)
                .sourceObject("s3://bucket/test.mp4")
                .build();

        sqsEventPublisherAdapter.novoVideoRecebido(testJob);

        String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(sqsMessagingProperties.getQueueName()).build()).queueUrl();
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        assertThat(messages).hasSize(1);
        Message receivedMessage = messages.getFirst();

        Job receivedJob = objectMapper.readValue(receivedMessage.body(), Job.class);
        assertThat(receivedJob.getId()).isEqualTo(testJob.getId());
        assertThat(receivedJob.getUser().getId()).isEqualTo(testUser.getId());
    }
}
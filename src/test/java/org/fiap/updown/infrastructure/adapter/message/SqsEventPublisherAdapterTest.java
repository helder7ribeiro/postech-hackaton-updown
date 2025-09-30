package org.fiap.updown.infrastructure.adapter.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsEventPublisherAdapterTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private SqsMessagingProperties props;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsEventPublisherAdapter sqsEventPublisherAdapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sqsEventPublisherAdapter, "queueUrl", "http://fake-queue-url");
    }

    @Test
    void deveLancarFalhaInfraestruturaException_QuandoOcorrerErroDeSerializacaoJson() throws Exception {
        Job testJob = new Job();
        String errorMessage = "Falha na serialização";

        when(objectMapper.writeValueAsString(any(Job.class)))
                .thenThrow(new JsonProcessingException(errorMessage) {
                });

        assertThatThrownBy(() -> sqsEventPublisherAdapter.novoVideoRecebido(testJob))
                .isInstanceOf(FalhaInfraestruturaException.class)
                .hasMessageContaining("Falha ao serializar payload SQS");
    }
}
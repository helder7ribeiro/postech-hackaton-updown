package org.fiap.updown.application.port.driver;

import org.fiap.updown.domain.model.Job;

public interface EventPublisher {

    void novoVideoRecebido(Job saved);
}

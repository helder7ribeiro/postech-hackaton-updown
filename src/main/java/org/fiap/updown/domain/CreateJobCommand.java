// src/main/java/org/fiap/updown/application/usecase/job/command/CreateJobCommand.java
package org.fiap.updown.domain;

import java.io.InputStream;
import java.util.UUID;

public record CreateJobCommand(
        UUID userId,
        String originalFilename,
        String contentType,
        InputStream data
) {}

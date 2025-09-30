// src/main/java/org/fiap/updown/application/usecase/job/command/CreateJobCommand.java
package org.fiap.updown.domain;

import java.io.InputStream;

public record CreateJobCommand(
        String username,
        String originalFilename,
        String contentType,
        InputStream data
) {}

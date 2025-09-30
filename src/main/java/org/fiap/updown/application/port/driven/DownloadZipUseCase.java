package org.fiap.updown.application.port.driven;

import java.io.IOException;

public interface DownloadZipUseCase {
    byte[] execute(String path) throws IOException;
}

package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.DownloadZipUseCase;
import org.fiap.updown.application.port.driver.VideoStorage;

import java.io.IOException;

@RequiredArgsConstructor
public class DownloadZipUseCaseImpl implements DownloadZipUseCase {

    private final VideoStorage videoStorage;

    @Override
    public byte[] execute(String path) throws IOException {
        return videoStorage.download(path);
    }
}

package org.fiap.updown.application.port.driver;

import java.io.InputStream;
import java.util.UUID;

public interface VideoStorage {

    String store(UUID uuid, String s, String s1, InputStream data);
}

package org.fiap.updown.application.port.driver;

import org.fiap.updown.domain.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AppUserPersistencePort {

    AppUser save(AppUser user);

    Optional<AppUser> findById(UUID id);

    Optional<AppUser> findByEmail(String email);

    boolean existsById(UUID id);

    boolean existsByEmail(String email);

    void deleteById(UUID id);

    Page<AppUser> findAll(Pageable pageable);
}

package org.fiap.updown.infrastructure.adapter.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.persistence.mapper.AppUserMapper;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppUserPersistenceAdapter implements AppUserPersistencePort {

    private final AppUserRepository repository;
    private final AppUserMapper mapper; // entidade ↔ domínio (MapStruct)

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public AppUser save(AppUser user) {
        AppUserEntity entity = mapper.toEntity(user);
        AppUserEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return repository.existsByUsernameIgnoreCase(username);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppUser> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDomain);
    }
}

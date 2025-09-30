package org.fiap.updown.infrastructure.adapter.persistence;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AppUserPersistenceAdapter.class, org.fiap.updown.infrastructure.adapter.persistence.mapper.AppUserMapperImpl.class})
class AppUserPersistenceAdapterTest {

    @Autowired
    private AppUserPersistenceAdapter appUserPersistenceAdapter;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void deveSalvarEEncontrarUsuarioPorId() {
        AppUser newUser = new AppUser();
        newUser.setEmail("integracao@teste.com");

        AppUser savedUser = appUserPersistenceAdapter.save(newUser);
        Optional<AppUser> foundUserOpt = appUserPersistenceAdapter.findById(savedUser.getId());

        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUserOpt).isPresent();

        AppUser foundUser = foundUserOpt.get();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("integracao@teste.com");
    }

    @Test
    void deveVerificarExistenciaDeUsuarioPorEmail() {
        AppUser newUser = new AppUser();
        newUser.setEmail("existente@teste.com");
        newUser.setUsername("existente");
        appUserPersistenceAdapter.save(newUser);

        boolean existe = appUserPersistenceAdapter.existsByEmail("existente@teste.com");
        boolean naoExiste = appUserPersistenceAdapter.existsByEmail("nao.existente@teste.com");

        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }

    @Test
    void deveDeletarUsuarioPorId() {
        AppUser newUser = new AppUser();
        newUser.setEmail("deletar@teste.com");
        newUser.setUsername("deletar");
        AppUser savedUser = appUserPersistenceAdapter.save(newUser);
        UUID userId = savedUser.getId();

        appUserPersistenceAdapter.deleteById(userId);

        boolean userExists = appUserRepository.existsById(userId);
        assertThat(userExists).isFalse();
    }
}
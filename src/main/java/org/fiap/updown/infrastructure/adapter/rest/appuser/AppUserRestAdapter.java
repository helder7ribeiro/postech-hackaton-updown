package org.fiap.updown.infrastructure.adapter.rest.appuser;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driven.*;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.infrastructure.adapter.HeaderUtil;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.AppUserExistsResponse;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.AppUserResponse;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.CreateAppUserRequest;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.UpdateAppUserRequest;
import org.fiap.updown.infrastructure.adapter.rest.appuser.mapper.AppUserRestMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.fiap.updown.util.EntityUtils.getBaseNameFromEntity;

@Slf4j
@RestController
@RequestMapping("/api/v1/app-users")
@RequiredArgsConstructor
public class AppUserRestAdapter {

    private final CreateAppUserUseCase createUseCase;
    private final GetAppUserByIdUseCase getByIdUseCase;
    private final UpdateAppUserUseCase updateUseCase;
    private final DeleteAppUserUseCase deleteUseCase;
    private final ExistsAppUserByEmailUseCase existsByEmailUseCase;

    private final AppUserRestMapper mapper;

    @Value("${spring.application.name}")
    private String applicationName;

    public static final String ENTITY_NAME = getBaseNameFromEntity(AppUserEntity.class.getName());

    // ------------------------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Cria um AppUser")
    public ResponseEntity<AppUserResponse> create(@RequestBody @Valid CreateAppUserRequest request) {
        AppUser toCreate = mapper.toDomain(request);
        AppUser created = createUseCase.execute(toCreate);
        AppUserResponse resp = mapper.toResponse(created);
        HttpHeaders headers = HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, resp.id().toString());

        return ResponseEntity.created(URI.create("/api/v1/app-users/" + resp.id()))
                .headers(headers)
                .body(resp);
    }

    // ------------------------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Consulta um AppUser por id")
    public ResponseEntity<AppUserResponse> findById(@PathVariable("id") UUID id) {
        AppUser user = getByIdUseCase.execute(id); // lança se não achar
        return ResponseEntity.ok(mapper.toResponse(user));
    }

    // ------------------------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um AppUser (e-mail)")
    public ResponseEntity<AppUserResponse> update(@PathVariable("id") UUID id,
                                                  @RequestBody @Valid UpdateAppUserRequest request) {
        AppUser toUpdate = mapper.toDomain(request);
        toUpdate.setId(id);
        AppUser updated = updateUseCase.execute(toUpdate);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, updated.getId().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(mapper.toResponse(updated));
    }

    // ------------------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um AppUser por id")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        deleteUseCase.execute(id); // lança se não achar
        HttpHeaders headers = HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString());

        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    // ------------------------------------------------------------------------------------
    // EXISTS
    // ------------------------------------------------------------------------------------
    @GetMapping("/exists")
    @Operation(summary = "Verifica se já existe AppUser por e-mail")
    public ResponseEntity<AppUserExistsResponse> exists(@RequestParam("email") String email) {
        boolean exists = existsByEmailUseCase.execute(email);
        return ResponseEntity.ok(new AppUserExistsResponse(email, exists));
    }
}

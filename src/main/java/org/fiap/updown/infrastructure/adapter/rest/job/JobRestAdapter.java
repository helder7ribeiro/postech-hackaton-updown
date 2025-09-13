package org.fiap.updown.infrastructure.adapter.rest.job;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driven.*;
import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.infrastructure.adapter.HeaderUtil;
import org.fiap.updown.infrastructure.adapter.rest.util.JwtUtils;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobExistsByIdResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.UpdateJobRequest;
import org.fiap.updown.infrastructure.adapter.rest.job.mapper.JobRestMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static org.fiap.updown.util.EntityUtils.getBaseNameFromEntity;

@Slf4j
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobRestAdapter {

    private final CreateJobUseCase createUseCase;
    private final GetJobByIdUseCase getByIdUseCase;
    private final UpdateJobUseCase updateUseCase;
    private final DeleteJobUseCase deleteUseCase;
    private final ExistsJobByIdUseCase existsByIdUseCase;

    private final JobRestMapper mapper;
    private final JwtUtils jwtUtils;

    @Value("${spring.application.name}")
    private String applicationName;

    public static final String ENTITY_NAME = getBaseNameFromEntity(JobEntity.class.getName());

    // ------------------------------------------------------------------------------------
    // CREATE (multipart/form-data)
    // ------------------------------------------------------------------------------------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cria um Job recebendo um arquivo de vídeo (multipart/form-data)")
    public ResponseEntity<JobResponse> create(
            @RequestPart("video") MultipartFile video,
            HttpServletRequest httpRequest) {

        try {
            // Extrai username do JWT
            String username = jwtUtils.extractUsernameFromToken(httpRequest);
            
            // monta command sem acoplar a frameworks no use case
            CreateJobCommand cmd = new CreateJobCommand(
                    username,
                    video.getOriginalFilename(),
                    video.getContentType(),
                    video.getInputStream() // Esta linha pode lançar IOException
            );

            Job created = createUseCase.execute(cmd);
            JobResponse resp = mapper.toResponse(created);
            HttpHeaders headers = HeaderUtil.createEntityCreationAlert(applicationName, false, "job", resp.id().toString());

            return ResponseEntity.created(URI.create("/api/v1/jobs/" + resp.id()))
                    .headers(headers)
                    .body(resp);

        } catch (IOException e) {
            throw new FalhaInfraestruturaException("Não foi possível ler o arquivo de vídeo enviado.", e);
        }
    }

    // ------------------------------------------------------------------------------------
    // READ
    // ------------------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Consulta um Job por id")
    public ResponseEntity<JobResponse> findById(@PathVariable("id") UUID id) {
        Job job = getByIdUseCase.execute(id); // lança se não achar
        return ResponseEntity.ok(mapper.toResponse(job));
    }

    // ------------------------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza status/result/error de um Job")
    public ResponseEntity<JobResponse> update(@PathVariable("id") UUID id,
                                              @RequestBody @Valid UpdateJobRequest request) {
        Job patch = mapper.toDomain(request);
        patch.setId(id);
        Job updated = updateUseCase.execute(patch);
        HttpHeaders headers = HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, updated.getId().toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(mapper.toResponse(updated));
    }

    // ------------------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um Job por id")
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
    @GetMapping("/exists/{id}")
    @Operation(summary = "Verifica se existe Job por id")
    public ResponseEntity<JobExistsByIdResponse> existsById(@PathVariable("id") UUID id) {
        boolean exists = existsByIdUseCase.execute(id);
        return ResponseEntity.ok(new JobExistsByIdResponse(id, exists));
    }
}

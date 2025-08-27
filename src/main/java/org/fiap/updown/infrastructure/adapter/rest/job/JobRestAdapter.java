package org.fiap.updown.infrastructure.adapter.rest.job;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driven.*;
import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.CreateJobRequest;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobExistsByIdResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.UpdateJobRequest;
import org.fiap.updown.infrastructure.adapter.rest.job.mapper.JobRestMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

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

    // ------------------------------------------------------------------------------------
    // CREATE (multipart/form-data)
    // ------------------------------------------------------------------------------------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cria um Job recebendo um arquivo de vídeo (multipart/form-data)")
    public ResponseEntity<JobResponse> create(
            @RequestPart("payload") @Valid CreateJobRequest request,
            @RequestPart("video") MultipartFile video) throws Exception {

        // monta command sem acoplar a frameworks no use case
        CreateJobCommand cmd = new CreateJobCommand(
                request.userId(),
                video.getOriginalFilename(),
                video.getContentType(),
                video.getInputStream()
        );

        Job created = createUseCase.execute(cmd);
        JobResponse resp = mapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/v1/jobs/" + resp.id())).body(resp);
    }

//    @PostMapping(consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
//    public ResponseEntity<JobResponse> createFromOctet(
//            @RequestParam("userId") UUID userId,
//            @RequestHeader(value = "X-Filename", required = false) String filename,
//            @RequestHeader(value = "Content-Type", required = false) String contentType,
//            InputStream body // stream bruto do request
//    ) throws Exception {
//
//        // Monte o command sem acoplar frameworks
//        CreateJobCommand cmd = new CreateJobCommand(
//                userId,
//                (filename == null || filename.isBlank()) ? "upload.bin" : filename,
//                (contentType == null || contentType.isBlank()) ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType,
//                body
//        );
//
//        Job created = createUseCase.execute(cmd);
//        JobResponse resp = mapper.toResponse(created);
//        return ResponseEntity.created(URI.create("/api/v1/jobs/" + resp.id())).body(resp);
//    }


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
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    // ------------------------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um Job por id")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        deleteUseCase.execute(id); // lança se não achar
        return ResponseEntity.noContent().build();
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

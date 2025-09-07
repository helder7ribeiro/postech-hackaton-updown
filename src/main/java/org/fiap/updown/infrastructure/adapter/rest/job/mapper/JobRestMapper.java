package org.fiap.updown.infrastructure.adapter.rest.job.mapper;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.CreateJobRequest;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.JobUserResponse;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.UpdateJobRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface JobRestMapper {

    // ---- DTO -> Domínio (CREATE) ----
    // Mapeia apenas userId -> user; demais campos ficam null e serão definidos no controller.
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    Job toDomain(CreateJobRequest req);

    // ---- DTO -> Domínio (UPDATE/PATCH) ----
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "status", source = "status")
    @Mapping(target = "resultObject", source = "resultObject")
    @Mapping(target = "errorMsg", source = "errorMsg")
    Job toDomain(UpdateJobRequest req);

    // ---- Domínio -> DTO (READ) ----
    @Mapping(target = "user", source = "user", qualifiedByName = "toJobUserResponse")
    JobResponse toResponse(Job domain);

    // ---------- Helpers ----------
    @Named("userFromId")
    default AppUser userFromId(UUID id) {
        if (id == null) return null;
        AppUser u = new AppUser();
        u.setId(id);
        return u;
    }

    @Named("toJobUserResponse")
    default JobUserResponse toJobUserResponse(AppUser user) {
        if (user == null) return null;
        return new JobUserResponse(user.getId(), user.getEmail());
    }
}

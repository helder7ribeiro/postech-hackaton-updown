package org.fiap.updown.infrastructure.adapter.rest.appuser.mapper;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.AppUserResponse;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.CreateAppUserRequest;
import org.fiap.updown.infrastructure.adapter.rest.appuser.dto.UpdateAppUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AppUserRestMapper {


    // DTO -> Domain
    @Mapping(target = "id", ignore = true)
    AppUser toDomain(CreateAppUserRequest req);

    @Mapping(target = "id", ignore = true)
    AppUser toDomain(UpdateAppUserRequest req);

    // Domain -> DTO
    AppUserResponse toResponse(AppUser domain);
}
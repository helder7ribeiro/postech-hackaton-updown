package org.fiap.updown.infrastructure.adapter.persistence.mapper;

import org.fiap.updown.domain.model.Job;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring",
        uses = { AppUserMapper.class },
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface JobMapper {

    // --------- entidade -> domínio ----------
    @Mapping(target = "user", source = "user") // usa AppUserMapper.toDomain
    Job toDomain(JobEntity entity);

    // --------- domínio -> entidade ----------
    // Mapear user como referência leve (só id) para não forçar carga completa
    @Mapping(target = "user", source = "user", qualifiedByName = "appUserRef")
    JobEntity toEntity(Job domain);

    // --------- update parcial ----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", source = "user", qualifiedByName = "appUserRef")
    void updateEntityFromDomain(Job source, @MappingTarget JobEntity target);
}

package org.fiap.updown.infrastructure.adapter.persistence.mapper;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AppUserMapper {

    // --------- entidade -> domínio ----------
    AppUser toDomain(AppUserEntity entity);

    // --------- domínio -> entidade ----------
    AppUserEntity toEntity(AppUser domain);

    // --------- referência leve para FK (só id) ----------
    @Named("appUserRef")
    default AppUserEntity toEntityRef(AppUser domain) {
        if (domain == null || domain.getId() == null) return null;
        AppUserEntity e = new AppUserEntity();
        e.setId(domain.getId());
        return e;
    }

    @Named("appUserRefFromId")
    default AppUserEntity toEntityRef(UUID id) {
        if (id == null) return null;
        AppUserEntity e = new AppUserEntity();
        e.setId(id);
        return e;
    }

    // --------- update parcial ----------
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(AppUser source, @MappingTarget AppUserEntity target);
}

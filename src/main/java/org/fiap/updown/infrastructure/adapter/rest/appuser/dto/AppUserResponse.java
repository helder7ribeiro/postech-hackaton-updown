package org.fiap.updown.infrastructure.adapter.rest.appuser.dto;

import java.util.UUID;

public record AppUserResponse(
        UUID id,
        String email
) {}
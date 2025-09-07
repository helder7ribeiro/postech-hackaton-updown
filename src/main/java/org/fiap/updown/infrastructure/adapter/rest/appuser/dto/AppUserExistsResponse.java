package org.fiap.updown.infrastructure.adapter.rest.appuser.dto;

public record AppUserExistsResponse(
        String email,
        boolean exists
) {}
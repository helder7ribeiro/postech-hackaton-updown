package org.fiap.updown.domain.service;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;

public interface JobService {
    Job createJob(AppUser owner, String sourceObject);
}

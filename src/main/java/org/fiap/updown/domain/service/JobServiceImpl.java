package org.fiap.updown.domain.service;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;

public class JobServiceImpl implements JobService {

    public Job createJob(AppUser user, String sourceObject) {
        return Job.builder()
                .user(user)
                .sourceObject(sourceObject)
                .status(JobStatus.RECEIVED)
                .build();
    }
}

package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.exception.NegocioException;
import org.fiap.updown.domain.model.Job;
import org.springframework.web.multipart.MultipartFile;

public interface CriarJob {


    public Job execute(Job job, MultipartFile video) throws NegocioException;

}

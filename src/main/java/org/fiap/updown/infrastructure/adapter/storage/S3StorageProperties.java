package org.fiap.updown.infrastructure.adapter.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "app.storage.s3")
public class S3StorageProperties {

    private String bucket;
    private String inputPrefix;
    private String region;
    private String endpoint;
    private boolean pathStyle;
    private boolean createBucketIfMissing ;
    private String accessKey;
    private String secretKey;
    private String sessionToken;
}

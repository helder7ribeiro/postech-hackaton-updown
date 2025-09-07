
-- PostgreSQL

CREATE TABLE IF NOT EXISTS app_user (
                                        id    uuid PRIMARY KEY,
                                        email varchar(255) NOT NULL UNIQUE
);


CREATE TABLE IF NOT EXISTS job (
                                   id             uuid PRIMARY KEY,
                                   user_id        uuid NOT NULL REFERENCES app_user(id),
                                   source_object  varchar(512) NOT NULL,           -- s3://bucket/input/...
                                   result_object  varchar(512),                    -- s3://bucket/output/...
                                   status         varchar(32) NOT NULL,            -- RECEIVED|PROCESSING|COMPLETED|FAILED
                                   error_msg      text,
                                   created_at     timestamptz NOT NULL DEFAULT now(),
                                   updated_at     timestamptz NOT NULL DEFAULT now(),
                                   CONSTRAINT chk_job_status
                                       CHECK (status IN ('RECEIVED','PROCESSING','COMPLETED','FAILED'))
);

-- 3) Índices úteis
CREATE INDEX IF NOT EXISTS idx_job_user_id    ON job (user_id);
CREATE INDEX IF NOT EXISTS idx_job_created_at ON job (created_at);

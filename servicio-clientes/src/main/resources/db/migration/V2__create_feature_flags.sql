CREATE TABLE feature_flags (
nombre VARCHAR(100) PRIMARY KEY,
activo BOOLEAN NOT NULL DEFAULT FALSE

);
INSERT INTO feature_flags (nombre, activo) VALUES ('mask-pii', FALSE) ;

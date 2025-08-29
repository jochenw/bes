DROP TABLE IF EXISTS BesPropertySets;
DROP TABLE IF EXISTS BesProperties;
DROP SEQUENCE IF EXISTS BesPropertySetsSeq;
DROP SEQUENCE IF EXISTS BesPropertiesSq;
CREATE SEQUENCE BesPropertySetsSeq AS BIGINT UNSIGNED INCREMENT BY 1 MINVALUE 1;
CREATE SEQUENCE BesPropertiesSeq AS BIGINT UNSIGNED INCREMENT BY 1 MINVALUE 1;
CREATE TABLE BesPropertySets (
    id BIGINT PRIMARY KEY NOT NULL,
    sha256sum BINARY(32) NOT NULL
);
CREATE TABLE BesProperties (
    id BIGINT NOT NULL,
    setId BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    value VARCHAR(256) NOT NULL,
    FOREIGN KEY F_BesProperties_BesPropertySets (setId) REFERENCES BesPropertySets (id),
    UNIQUE U_BesProperties_SetIdName (setId, name),
    INDEX I_BesProperties_NameValue (name, value)
);

-- Script file for creating the users table.
--
-- DROP SEQUENCE IF EXISTS PropertySetsSeq;
-- DROP SEQUENCE IF EXISTS PropertiesSeq;
-- DROP TABLE IF EXISTS Properties;
-- DROP TABLE IF EXISTS PropertySets;
CREATE Sequence IF NOT EXISTS PropertySetsSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS PropertySets (
  id BIGINT NOT NULL PRIMARY KEY,
  digest BINARY(32) NOT NULL,
  INDEX I_PropertySets_Digest (digest)
);
CREATE Sequence IF NOT EXISTS PropertiesSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS Properties (
  id BIGINT NOT NULL PRIMARY KEY,
  setId BIGINT NOT NULL,
  pKey VARCHAR(128) NOT NULL,
  pValue VARCHAR(256) NOT NULL,
  UNIQUE INDEX U_Properties_SetIdKey(setId, pKey),
  FOREIGN KEY F_PropertiesPropertySets (setId) REFERENCES PropertySets (id)
);

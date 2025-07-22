CREATE SEQUENCE IF NOT EXISTS besParameterSetsSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS besParameterSets (
	id BIGINT NOT NULL PRIMARY KEY,
	creationTime DATETIME NOT NULL
);
CREATE TABLE IF NOT EXISTS besParameters (
	setId BIGINT NOT NULL,
	name VARCHAR(64) NOT NULL,
	value VARCHAR(256) NOT NULL,
	CONSTRAINT FOREIGN KEY fBesParametersToBesParamterSets (setId) REFERENCES besParameterSets (id),
	CONSTRAINT UNIQUE KEY uBesParametersSetIdAndName (setId, name),
	INDEX iBesParametersValue (value)
);

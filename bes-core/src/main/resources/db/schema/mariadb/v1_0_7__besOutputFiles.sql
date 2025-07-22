CREATE SEQUENCE IF NOT EXISTS besOutputFilesSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS besOutputFiles (
	id BIGINT NOT NULL PRIMARY KEY,
	executionId BIGINT NOT NULL,
	contentId BIGINT NOT NULL,
	name VARCHAR(32) NOT NULL,
    CONSTRAINT FOREIGN KEY fBesOutputFilesToBesExecutions (executionId) REFERENCES besExecutions (id),
    CONSTRAINT FOREIGN KEY fBesOutputFilesToBesFilesContent (contentId) REFERENCES besFilesContent(id),
    CONSTRAINT UNIQUE KEY uBesOutputFilesExecutionAndName (executionId, name)
);

CREATE SEQUENCE IF NOT EXISTS besInputFilesSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS besInputFiles (
	id BIGINT NOT NULL PRIMARY KEY,
	jobId BIGINT NOT NULL,
	contentId BIGINT NOT NULL,
	name VARCHAR(64) NOT NULL,
    CONSTRAINT FOREIGN KEY fBesInputFilesToBesJobs (jobId) REFERENCES besJobs (id),
    CONSTRAINT FOREIGN KEY fBesInputFilesToBesFilesContent (contentId) REFERENCES besFilesContent(id),
    CONSTRAINT UNIQUE KEY uBesInputFilesJobAndName (jobId, name)
);

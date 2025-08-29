DROP TABLE IF EXISTS BesJobs;
DROP SEQUENCE IF EXISTS BesJobsSeq;
CREATE SEQUENCE BesJobsSeq AS BIGINT UNSIGNED INCREMENT BY 1 MINVALUE 1;
CREATE TABLE BesJobs (
    id BIGINT PRIMARY KEY NOT NULL,
    pKey VARCHAR(64) NOT NULL,
    ownerId BIGINT NOT NULL,
    propertySetId BIGINT NULL,
    FOREIGN KEY F_BesJobs_BesUsers (ownerId) REFERENCES BesUsers(id),
    FOREIGN KEY F_BesJobs_BesPropertySets (propertySetId) REFERENCES BesPropertySets (id),
    INDEX I_BesJobs_Key (pKey)
);

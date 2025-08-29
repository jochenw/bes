DROP TABLE IF EXISTS BesExecutions;
DROP SEQUENCE IF EXISTS BesExecutionsSeq;
CREATE SEQUENCE BesExecutionsSeq AS BIGINT UNSIGNED INCREMENT BY 1 MINVALUE 1;
CREATE TABLE BesExecutions (
    id BIGINT PRIMARY KEY NOT NULL,
    jobId BIGINT NOT NULL,
    startTime TIMESTAMP NOT NULL,
    endTime TIMESTAMP NULL,
    startingUserId BIGINT NOT NULL,
    stackTrace MEDIUMBLOB NULL,
    propertySetId BIGINT NULL,
    FOREIGN KEY F_BesExecutions_BesJobs (jobId) REFERENCES BesJobs (id),
    FOREIGN KEY F_BesExecutions_BesUsers (startingUserId) REFERENCES BesUsers (id),
    FOREIGN KEY F_BesExecutions_BesPropertySets (propertySetId) REFERENCES BesPropertySets (id),
    INDEX I_BesExecutions_startTimeEndTime (startTime,endTime)
);

CREATE SEQUENCE IF NOT EXISTS besUsersSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS besUsers (
   id BIGINT NOT NULL PRIMARY KEY,
   userId VARCHAR(32) NOT NULL,
   userName VARCHAR(64) NOT NULL,
   userEmail VARCHAR(128) NOT NULL,
   CONSTRAINT UNIQUE KEY uBesUsersUserId (userId),
   CONSTRAINT UNIQUE KEY uBesUsersUserEmail (userEmail),
   INDEX iBesUsersUserName (userName) 
);

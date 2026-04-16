-- Script file for creating the users table.
--
-- DROP SEQUENCE BesUsersSeq;
-- DROP TABLE BesUsers;
CREATE SEQUENCE IF NOT EXISTS BesUsersSeq AS BIGINT;
CREATE TABLE IF NOT EXISTS BesUsers (
  id BIGINT NOT NULL PRIMARY KEY,
  userId VARCHAR(32) NOT NULL,
  email VARCHAR(256) NOT NULL,
  usrName VARCHAR(128) NOT NULL,
  UNIQUE INDEX U_Users_UserId(userId),
  UNIQUE INDEX U_Users_Email(email),
  INDEX I_Users_Name(usrName)
);



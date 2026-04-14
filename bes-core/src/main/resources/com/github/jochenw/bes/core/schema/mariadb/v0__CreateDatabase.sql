-- Script file for creating the bes database
--
-- Note: This script is intentionally *not* using the letter 'V' as th prefix, but a 'v'.
--       The reason is, that this script must be executed against the mysql database
--       (with administrative privileges), and not against the bes database.
--       In other words: We don't want Flyway to execute this script, and that is achieved
--       by the 'wrong' database.
-- DROP DATABASE bes;
CREATE DATABASE IF NOT EXISTS bes  DEFAULT CHARACTER SET = 'UTF8';
GRANT ALL PRIVILEGES ON bes.* TO 'bes'@'localhost' IDENTIFIED BY 'bes';
FLUSH PRIVILEGES;

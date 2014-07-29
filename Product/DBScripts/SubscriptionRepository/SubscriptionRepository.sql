/*
Table creation script for the subscription repository.

This script was intended to be run on MySQL.

Please see the DatabaseInstructions.txt file for database creation and configuration instructions.
*/

DROP TABLE IF EXISTS subscriptionrepository.subscription CASCADE;
CREATE TABLE subscriptionrepository.subscription (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Database generated identifier',
  subscriptionId VARCHAR(128) NOT NULL COMMENT 'Unique identifier UUID for a CONNECT generated subscription',
  subscriptionStatus VARCHAR(45) NULL DEFAULT NULL COMMENT 'Subcription status \'SUBSCRIBED\', \'UNSUBSCRIBED\'',
  subscriptionRole VARCHAR(45) NULL DEFAULT NULL COMMENT 'Subcription role \'CONSUMER\', \'PRODUCER\'',
  topic VARCHAR(255) NULL DEFAULT NULL COMMENT 'Topic of the subscription record',
  dialect VARCHAR(255) NULL DEFAULT NULL,
  consumer VARCHAR(128) NULL DEFAULT NULL COMMENT 'Notification consumer home community id',
  producer VARCHAR(128) NULL DEFAULT NULL COMMENT 'Notification producer home community id',
  patientId VARCHAR(128) NULL DEFAULT NULL COMMENT 'Local system patient identifier',
  patientAssigningAuthority VARCHAR(128) NULL DEFAULT NULL COMMENT 'Assigning authority for the local patient identifier',
  creationTime DATETIME NULL DEFAULT NULL COMMENT 'Format of YYYYMMDDHHMMSS',
  subscribeXML LONGTEXT NULL DEFAULT NULL COMMENT 'Full subscribe message as an XML string',
  subscriptionReferenceXML LONGTEXT NULL DEFAULT NULL COMMENT 'Full subscription reference as an XML string',
  targets LONGTEXT NULL DEFAULT NULL COMMENT 'Full target system as an XML string',
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS subscriptionrepository.notification CASCADE;
CREATE TABLE subscriptionrepository.notification (
  id BIGINT NOT NULL AUTO_INCREMENT,
  subscriptionId BIGINT NOT NULL COMMENT 'Foriegn key to subscription table',
  notificationStatus VARCHAR(45) NULL COMMENT 'Notification status \'RECEIVED\', \'SENT\'',
  topic VARCHAR(255) NULL DEFAULT NULL COMMENT 'Topic name',
  dialect VARCHAR(255) NULL DEFAULT NULL COMMENT 'Topic dialect format',
  fileName VARCHAR(255) NULL DEFAULT NULL COMMENT 'Fully qualified path and file name of notification message location',
  notificationTime DATETIME NULL DEFAULT NULL COMMENT 'Format of YYYYMMDDHHMMSS',
  notificationMessage LONGTEXT NULL DEFAULT NULL COMMENT 'Inbound notification message as an XML string',
  acknowledgementMessage LONGTEXT NULL DEFAULT NULL COMMENT 'Response acknowledgement message as an XML string',
  PRIMARY KEY (id),
  INDEX fk_notification_subscription (subscriptionId ASC),
  CONSTRAINT fk_notification_subscription
    FOREIGN KEY (subscriptionId )
    REFERENCES subscriptionrepository.subscription (id )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


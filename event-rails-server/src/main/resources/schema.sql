CREATE TABLE INT_LOCK  (
						   LOCK_KEY CHAR(36) NOT NULL,
						   REGION VARCHAR(100) NOT NULL,
						   CLIENT_ID CHAR(36),
						   CREATED_DATE DATETIME(6) NOT NULL,
						   constraint INT_LOCK_PK primary key (LOCK_KEY, REGION)
) ENGINE=InnoDB;

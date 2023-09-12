CREATE TABLE IF NOT EXISTS `int_lock`
(
    `lock_key`     char(36)     NOT NULL,
    `region`       varchar(100) NOT NULL,
    `client_id`    char(36) DEFAULT NULL,
    `created_date` datetime(6)  NOT NULL,
    PRIMARY KEY (`lock_key`, `region`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `es__events`
(

    `event_sequence_number` bigint       NOT NULL,
    `context`               varchar(100) DEFAULT 'default',
    `aggregate_id`          varchar(100) DEFAULT NULL,
    `event_name`            varchar(100) NOT NULL,
    `created_at`            bigint       NOT NULL,
    `event_message`         blob         NOT NULL,
    `deleted_at`            bigint       DEFAULT NULL
) ENGINE = InnoDB;

CREATE index `es__events_context_aggregate_id_event_sequence_number_index`
    on `es__events` (`context`, `aggregate_id`, `event_sequence_number`) using BTREE;
CREATE index `es__events_context_event_name_event_sequence_number_index`
    on `es__events` (`context`, `event_name`, `event_sequence_number`) using BTREE;

CREATE TABLE IF NOT EXISTS `es__snapshot`
(
    `snapshot_id`           varchar(205) NOT NULL,
    `context`               varchar(100) NOT NULL,
    `aggregate_id`          varchar(100) NOT NULL,
    `aggregate_state`       blob,
    `event_sequence_number` bigint      DEFAULT NULL,
    `updated_at`            datetime(6) DEFAULT NULL,
    PRIMARY KEY (`snapshot_id`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `performance__handler_service_time`
(
    `id`                varchar(255) NOT NULL,
    `last_service_time` double       NOT NULL,
    `mean_service_time` double       NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `performance__handler_invocation_count`
(
    `id`               varchar(255) NOT NULL,
    `last_count`       int          NOT NULL,
    `mean_probability` double       NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;








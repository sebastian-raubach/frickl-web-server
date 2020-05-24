CREATE TABLE IF NOT EXISTS `access_tokens` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Auto incremented id of this table.',
  `token` varchar(36) NOT NULL COMMENT 'The access token.',
  `expires_on` datetime NULL COMMENT 'When this token expires.',
  `created_on` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'When this record has been created.',
  `updated_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this record has last been updated.',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table contains all tokens that can be used to access folders that aren\'t public.';

CREATE TABLE IF NOT EXISTS `album_tokens` (
  `album_id` int(11) NOT NULL COMMENT 'The album this token belongs to.',
  `access_token_id` int(11) NOT NULL COMMENT 'The access token allowing access to this album.',
  `created_on` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'When this record has been created.',
  `updated_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this record has last been updated.',
  PRIMARY KEY (`album_id`, `access_token_id`) USING BTREE,
  KEY `album_tokens_ibfk_1` (`album_id`) USING BTREE,
  CONSTRAINT `album_tokens_ibfk_1` FOREIGN KEY (`album_id`) REFERENCES `albums` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  KEY `album_tokens_ibfk_2` (`access_token_id`) USING BTREE,
  CONSTRAINT `album_tokens_ibfk_2` FOREIGN KEY (`access_token_id`) REFERENCES `access_tokens` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table contains the mapping between access tokens and albums.';

ALTER TABLE `access_tokens` ADD INDEX `access_tokens_token`(`token`) USING BTREE;
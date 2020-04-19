SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `albums` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Auto incremented id of this table.',
  `name` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'The name of the album. Should ideally be relatively short.',
  `description` text CHARACTER SET latin1 COLLATE latin1_swedish_ci COMMENT 'Optional description of the album.',
  `path` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'The path to the album relative to the base path of the setup.',
  `banner_image_id` int(11) DEFAULT NULL COMMENT 'Optional banner image id. This image will be shown to visually represent this album.',
  `parent_album_id` int(11) DEFAULT NULL COMMENT 'Optional parent album id. If this album is a sub-album of another album, this parent album can be defined here.',
  `created_on` datetime DEFAULT NULL COMMENT 'When this record has been created.',
  `updated_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this record has last been updated.',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `banner_image_id` (`banner_image_id`) USING BTREE,
  KEY `parent_album_id` (`parent_album_id`) USING BTREE,
  CONSTRAINT `albums_ibfk_1` FOREIGN KEY (`banner_image_id`) REFERENCES `images` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `albums_ibfk_2` FOREIGN KEY (`parent_album_id`) REFERENCES `albums` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table contain  s all albums in Frickl. Albums correspond to image folders on the file system.';

CREATE TABLE IF NOT EXISTS `tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Auto incremented id of this table.',
  `name` varchar(255) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'The name of this tag.',
  `created_on` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'When this record has been created.',
  `updated_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this record has last been updated.',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table contains all tags/keywords that have been defined and assigned to images.';

CREATE TABLE IF NOT EXISTS `images` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Auto incremented id of this table.',
  `path` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'The path to the image relative to the base path of the setup.',
  `name` text CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT 'The name of the image. This will be the filename.',
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Boolean deciding if this image is one of the favorites.',
  `exif` json DEFAULT NULL COMMENT 'Optional Exif information in JSON format.',
  `album_id` int(11) NOT NULL COMMENT 'The album this image belongs to. This will be the containing folder.',
  `created_on` datetime DEFAULT NULL COMMENT 'When this record has been created.',
  `updated_on` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'When this record has last been updated.',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `images_ibfk_1` (`album_id`) USING BTREE,
  CONSTRAINT `images_ibfk_1` FOREIGN KEY (`album_id`) REFERENCES `albums` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table contains images from the file system.';

CREATE TABLE IF NOT EXISTS `image_tags` (
  `image_id` int(11) NOT NULL COMMENT 'The foreign key id of the referenced image.',
  `tag_id` int(11) NOT NULL COMMENT 'The foreign key id of the referenced tag.',
  PRIMARY KEY (`image_id`,`tag_id`) USING BTREE,
  KEY `tag_id` (`tag_id`) USING BTREE,
  CONSTRAINT `image_tags_ibfk_1` FOREIGN KEY (`image_id`) REFERENCES `images` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `image_tags_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 ROW_FORMAT=DYNAMIC COMMENT='This table joins `images` and `tags` and therefore defines which tags an image is tagged with.';
    
SET FOREIGN_KEY_CHECKS=1;

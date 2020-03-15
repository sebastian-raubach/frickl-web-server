ALTER TABLE `images` ADD COLUMN `is_public` TINYINT(1) DEFAULT 0 AFTER `album_id`;

ALTER TABLE `images` ADD INDEX `images_is_public` (`is_public`) USING BTREE;
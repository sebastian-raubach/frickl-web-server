ALTER TABLE `images` ADD COLUMN `data_type` enum('image', 'video') DEFAULT 'image' NOT NULL AFTER `is_public`;

ALTER TABLE `images` ADD INDEX `images_data_type` (`data_type`) USING BTREE;
ALTER TABLE `images` ADD COLUMN `view_count` int(11) DEFAULT 0 NOT NULL AFTER `is_public`;
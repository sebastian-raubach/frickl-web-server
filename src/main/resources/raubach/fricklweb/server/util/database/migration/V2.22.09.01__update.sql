UPDATE `tags` SET `name` = LOWER(`name`);

DROP TABLE IF EXISTS `temp_image_tags`;

CREATE TABLE `temp_image_tags` SELECT `image_tags`.`image_id` AS `image_id`, `tags`.`id` AS `tag_id`, `tags`.`name` AS `tag_name` FROM `image_tags` LEFT JOIN `tags` ON `tags`.`id` = `image_tags`.`tag_id`;

UPDATE `image_tags` LEFT JOIN `tags` t ON t.`id` = `image_tags`.`tag_id` SET `tag_id` = (SELECT `tag_id` FROM `temp_image_tags` WHERE `temp_image_tags`.`tag_name` = t.`name` ORDER BY temp_image_tags.`tag_id` ASC LIMIT 1);

DELETE FROM `tags` WHERE NOT EXISTS (SELECT 1 FROM `image_tags` WHERE `image_tags`.`tag_id` = `tags`.`id` LIMIT 1);

DROP TABLE `temp_image_tags`;
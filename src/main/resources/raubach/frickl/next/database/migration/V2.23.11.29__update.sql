CREATE TABLE `users`  (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `username` varchar(255) NOT NULL,
   `password` varchar(255) NOT NULL,
   `permissions` smallint NOT NULL DEFAULT 0,
   `view_type` enum('VIEW_ALL','ALBUM_PERMISSION') NOT NULL DEFAULT 'ALBUM_PERMISSION',
   `last_login` timestamp NULL,
   `created_on` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
   `updated_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`)
);

ALTER TABLE `albums`
    ADD COLUMN `created_by` int(11) NULL COMMENT 'Optional user id. This indicates which user created this album.' AFTER `parent_album_id`,
    ADD FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `images`
    ADD COLUMN `created_by` int(11) NULL COMMENT 'Optional user id. Indicates which user created/uploaded this image.' AFTER `data_type`,
    ADD FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
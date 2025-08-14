ALTER TABLE `album_counts`
ADD COLUMN `newest_image` datetime NULL DEFAULT NULL AFTER `image_view_count`,
ADD COLUMN `oldest_image` datetime NULL DEFAULT NULL AFTER `newest_image`;
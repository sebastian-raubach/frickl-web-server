CREATE TABLE IF NOT EXISTS `album_counts` (
  `album_id` int NOT NULL,
  `image_count` int NOT NULL DEFAULT 0,
  `image_count_public` int NOT NULL DEFAULT 0,
  `album_count` int NOT NULL DEFAULT 0,
  `image_view_count` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`album_id`),
  FOREIGN KEY (`album_id`) REFERENCES `frickl`.`albums` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
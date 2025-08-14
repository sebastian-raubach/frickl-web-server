DROP VIEW IF EXISTS `album_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `album_stats` AS select
    `albums`.`id` AS `id`,
    `albums`.`name` AS `name`,
    `albums`.`description` AS `description`,
    `albums`.`path` AS `path`,
    `albums`.`banner_image_id` AS `banner_image_id`,
    (select `id` from `images` where `images`.`album_id` = `albums`.`id` AND `images`.`is_public` = 1 limit 1) AS `banner_image_public_id`,
    `albums`.`parent_album_id` AS `parent_album_id`,
    `albums`.`created_on` AS `created_on`,
    `albums`.`updated_on` AS `updated_on`,
    `album_counts`.*
from
    `albums` LEFT JOIN `album_counts` ON `albums`.`id` = `album_counts`.`album_id`;

DROP VIEW IF EXISTS `calendar_data`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `calendar_data` AS select
    cast(`images`.`created_on` as date) AS `date`,
    count(1) AS `count`
from
    `images`
where
    (`images`.`created_on` is not null)
group by
    `date`
order by
    `date` desc;

DROP VIEW IF EXISTS `lat_lngs`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `lat_lngs` AS SELECT
     `images`.`id` AS `id`,
     `images`.`path` AS `path`,
     `images`.`name` AS `name`,
     `images`.`is_favorite` AS `is_favorite`,
     `images`.`exif` AS `exif`,
     `images`.`album_id` AS `album_id`,
     `images`.`is_public` AS `is_public`,
     `images`.`view_count` AS `view_count`,
     `images`.`data_type` AS `data_type`,
     `images`.`created_by` AS `created_by`,
     `images`.`created_on` AS `created_on`,
     `images`.`updated_on` AS `updated_on`,
     cast(
             json_unquote(
                     json_extract( `images`.`exif`, '$.gpsLatitude' )) AS DECIMAL ( 64, 10 )) AS `latitude`,
     cast(
             json_unquote(
                     json_extract( `images`.`exif`, '$.gpsLongitude' )) AS DECIMAL ( 64, 10 )) AS `longitude`
 FROM
     `images`
 WHERE
     ((
          json_unquote(
                  json_extract( `images`.`exif`, '$.gpsLatitude' )) IS NOT NULL
          )
         AND ( json_unquote( json_extract( `images`.`exif`, '$.gpsLongitude' )) IS NOT NULL ));

DROP VIEW IF EXISTS `stats_camera`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `stats_camera` AS select
    concat(json_unquote(json_extract(`images`.`exif`, '$.cameraMake')), ' ', json_unquote(json_extract(`images`.`exif`, '$.cameraModel'))) AS `camera`,
    count(1) AS `count`
from
    `images`
where
    (json_unquote(json_extract(`images`.`exif`,
    '$.cameraMake')) is not null)
group by
    `camera`
order by
    `count` desc;

DROP VIEW IF EXISTS `image_timeline`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `image_timeline` AS select
    year(`images`.`created_on`) AS `year`,
    month(`images`.`created_on`) AS `month`,
    json_arrayagg(`images`.`id`) AS `ids`
from
    `images`
where
    ((year(`images`.`created_on`) is not null)
    and (month(`images`.`created_on`) is not null))
group by
    `year`,
    `month`
order by
    `year` desc,
    `month` desc;
DROP VIEW IF EXISTS `album_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `album_stats` AS select
    `albums`.`id` AS `id`,
    `albums`.`name` AS `name`,
    `albums`.`description` AS `description`,
    `albums`.`path` AS `path`,
    `albums`.`banner_image_id` AS `banner_image_id`,
    (select `id` from `images` where `images`.`album_id` = `albums`.`id` AND `images`.`is_public` = 1 limit 1) AS `banner_image_public_id`,
    `albums`.`parent_album_id` AS `parent_album_id`,
    (SELECT MAX(`images`.`created_on`) from `images` where `images`.`album_id` = `albums`.`id` and `images`.`data_type` = 'image') AS `newest_image`,
    (SELECT MIN(`images`.`created_on`) from `images` where `images`.`album_id` = `albums`.`id` and `images`.`data_type` = 'image') AS `oldest_image`,
    `albums`.`created_on` AS `created_on`,
    `albums`.`updated_on` AS `updated_on`,
    (
        select count(1)
    from
        `images`
    where
        (`images`.`album_id` = `albums`.`id`)) AS `count`,
     (
         select count(1)
     from
         `images`
     where
         (`images`.`album_id` = `albums`.`id` AND `images`.`is_public`)) AS `count_public`
from
    `albums`;

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
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `lat_lngs` AS select
    `images`.`id` AS `id`,
    `images`.`path` AS `path`,
    `images`.`album_id` AS `album_id`,
    `images`.`is_public` AS `is_public`,
    cast(json_unquote(json_extract(`images`.`exif`,
    '$.gpsLatitude')) as decimal(64,
    10)) AS `latitude`,
    cast(json_unquote(json_extract(`images`.`exif`,
    '$.gpsLongitude')) as decimal(64,
    10)) AS `longitude`
from
    `images`
where
    ((json_unquote(json_extract(`images`.`exif`,
    '$.gpsLatitude')) is not null)
    and (json_unquote(json_extract(`images`.`exif`,
    '$.gpsLongitude')) is not null));

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

DROP VIEW IF EXISTS `album_access_token`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `album_access_token` AS SELECT
    albums.id AS `album_id`,
    albums.name AS `album_name`,
    albums.description AS `album_description`,
    access_tokens.id AS `token_id`,
    access_tokens.token AS `token_token`,
    access_tokens.expires_on AS `token_expires_on`
 FROM
    access_tokens
 LEFT JOIN album_tokens ON
    album_tokens.access_token_id = access_tokens.id
 LEFT JOIN albums ON
    albums.id = album_tokens.album_id;

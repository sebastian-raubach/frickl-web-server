CREATE OR REPLACE
VIEW `album_stats` AS select
    `albums`.`id` AS `id`,
    `albums`.`name` AS `name`,
    `albums`.`description` AS `description`,
    `albums`.`path` AS `path`,
    `albums`.`banner_image_id` AS `banner_image_id`,
    `albums`.`parent_album_id` AS `parent_album_id`,
    `albums`.`created_on` AS `created_on`,
    `albums`.`updated_on` AS `updated_on`,
    (
        select count(1)
    from
        `images`
    where
        (`images`.`album_id` = `albums`.`id`)) AS `count`
from
    `albums`;

CREATE OR REPLACE
VIEW `calendar_data` AS select
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

CREATE OR REPLACE
VIEW `lat_lngs` AS select
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

CREATE OR REPLACE
VIEW `stats_camera` AS select
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

CREATE OR REPLACE
VIEW `image_timeline` AS select
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
/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;

import raubach.fricklweb.server.database.tables.AccessTokens;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.ImageTags;
import raubach.fricklweb.server.database.tables.Images;
import raubach.fricklweb.server.database.tables.SchemaVersion;


/**
 * A class modelling indexes of tables in frickl.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index ACCESS_TOKENS_ACCESS_TOKENS_TOKEN = Internal.createIndex(DSL.name("access_tokens_token"), AccessTokens.ACCESS_TOKENS, new OrderField[] { AccessTokens.ACCESS_TOKENS.TOKEN }, false);
    public static final Index ALBUMS_BANNER_IMAGE_ID = Internal.createIndex(DSL.name("banner_image_id"), Albums.ALBUMS, new OrderField[] { Albums.ALBUMS.BANNER_IMAGE_ID }, false);
    public static final Index IMAGES_IMAGES_DATA_TYPE = Internal.createIndex(DSL.name("images_data_type"), Images.IMAGES, new OrderField[] { Images.IMAGES.DATA_TYPE }, false);
    public static final Index IMAGES_IMAGES_IS_PUBLIC = Internal.createIndex(DSL.name("images_is_public"), Images.IMAGES, new OrderField[] { Images.IMAGES.IS_PUBLIC }, false);
    public static final Index ALBUMS_PARENT_ALBUM_ID = Internal.createIndex(DSL.name("parent_album_id"), Albums.ALBUMS, new OrderField[] { Albums.ALBUMS.PARENT_ALBUM_ID }, false);
    public static final Index SCHEMA_VERSION_SCHEMA_VERSION_S_IDX = Internal.createIndex(DSL.name("schema_version_s_idx"), SchemaVersion.SCHEMA_VERSION, new OrderField[] { SchemaVersion.SCHEMA_VERSION.SUCCESS }, false);
    public static final Index IMAGE_TAGS_TAG_ID = Internal.createIndex(DSL.name("tag_id"), ImageTags.IMAGE_TAGS, new OrderField[] { ImageTags.IMAGE_TAGS.TAG_ID }, false);
}

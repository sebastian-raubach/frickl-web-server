/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database;


import javax.annotation.Generated;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;

import raubach.fricklweb.server.database.tables.AccessTokens;
import raubach.fricklweb.server.database.tables.AlbumTokens;
import raubach.fricklweb.server.database.tables.Albums;
import raubach.fricklweb.server.database.tables.ImageTags;
import raubach.fricklweb.server.database.tables.Images;
import raubach.fricklweb.server.database.tables.SchemaVersion;
import raubach.fricklweb.server.database.tables.Tags;
import raubach.fricklweb.server.database.tables.records.AccessTokensRecord;
import raubach.fricklweb.server.database.tables.records.AlbumTokensRecord;
import raubach.fricklweb.server.database.tables.records.AlbumsRecord;
import raubach.fricklweb.server.database.tables.records.ImageTagsRecord;
import raubach.fricklweb.server.database.tables.records.ImagesRecord;
import raubach.fricklweb.server.database.tables.records.SchemaVersionRecord;
import raubach.fricklweb.server.database.tables.records.TagsRecord;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>frickl</code> schema.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<AccessTokensRecord, Integer> IDENTITY_ACCESS_TOKENS = Identities0.IDENTITY_ACCESS_TOKENS;
    public static final Identity<AlbumsRecord, Integer> IDENTITY_ALBUMS = Identities0.IDENTITY_ALBUMS;
    public static final Identity<ImagesRecord, Integer> IDENTITY_IMAGES = Identities0.IDENTITY_IMAGES;
    public static final Identity<TagsRecord, Integer> IDENTITY_TAGS = Identities0.IDENTITY_TAGS;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AccessTokensRecord> KEY_ACCESS_TOKENS_PRIMARY = UniqueKeys0.KEY_ACCESS_TOKENS_PRIMARY;
    public static final UniqueKey<AlbumTokensRecord> KEY_ALBUM_TOKENS_PRIMARY = UniqueKeys0.KEY_ALBUM_TOKENS_PRIMARY;
    public static final UniqueKey<AlbumsRecord> KEY_ALBUMS_PRIMARY = UniqueKeys0.KEY_ALBUMS_PRIMARY;
    public static final UniqueKey<ImageTagsRecord> KEY_IMAGE_TAGS_PRIMARY = UniqueKeys0.KEY_IMAGE_TAGS_PRIMARY;
    public static final UniqueKey<ImagesRecord> KEY_IMAGES_PRIMARY = UniqueKeys0.KEY_IMAGES_PRIMARY;
    public static final UniqueKey<SchemaVersionRecord> KEY_SCHEMA_VERSION_PRIMARY = UniqueKeys0.KEY_SCHEMA_VERSION_PRIMARY;
    public static final UniqueKey<TagsRecord> KEY_TAGS_PRIMARY = UniqueKeys0.KEY_TAGS_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<AlbumTokensRecord, AlbumsRecord> ALBUM_TOKENS_IBFK_1 = ForeignKeys0.ALBUM_TOKENS_IBFK_1;
    public static final ForeignKey<AlbumTokensRecord, AccessTokensRecord> ALBUM_TOKENS_IBFK_2 = ForeignKeys0.ALBUM_TOKENS_IBFK_2;
    public static final ForeignKey<AlbumsRecord, ImagesRecord> ALBUMS_IBFK_1 = ForeignKeys0.ALBUMS_IBFK_1;
    public static final ForeignKey<AlbumsRecord, AlbumsRecord> ALBUMS_IBFK_2 = ForeignKeys0.ALBUMS_IBFK_2;
    public static final ForeignKey<ImageTagsRecord, ImagesRecord> IMAGE_TAGS_IBFK_1 = ForeignKeys0.IMAGE_TAGS_IBFK_1;
    public static final ForeignKey<ImageTagsRecord, TagsRecord> IMAGE_TAGS_IBFK_2 = ForeignKeys0.IMAGE_TAGS_IBFK_2;
    public static final ForeignKey<ImagesRecord, AlbumsRecord> IMAGES_IBFK_1 = ForeignKeys0.IMAGES_IBFK_1;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<AccessTokensRecord, Integer> IDENTITY_ACCESS_TOKENS = Internal.createIdentity(AccessTokens.ACCESS_TOKENS, AccessTokens.ACCESS_TOKENS.ID);
        public static Identity<AlbumsRecord, Integer> IDENTITY_ALBUMS = Internal.createIdentity(Albums.ALBUMS, Albums.ALBUMS.ID);
        public static Identity<ImagesRecord, Integer> IDENTITY_IMAGES = Internal.createIdentity(Images.IMAGES, Images.IMAGES.ID);
        public static Identity<TagsRecord, Integer> IDENTITY_TAGS = Internal.createIdentity(Tags.TAGS, Tags.TAGS.ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<AccessTokensRecord> KEY_ACCESS_TOKENS_PRIMARY = Internal.createUniqueKey(AccessTokens.ACCESS_TOKENS, "KEY_access_tokens_PRIMARY", AccessTokens.ACCESS_TOKENS.ID);
        public static final UniqueKey<AlbumTokensRecord> KEY_ALBUM_TOKENS_PRIMARY = Internal.createUniqueKey(AlbumTokens.ALBUM_TOKENS, "KEY_album_tokens_PRIMARY", AlbumTokens.ALBUM_TOKENS.ALBUM_ID, AlbumTokens.ALBUM_TOKENS.ACCESS_TOKEN_ID);
        public static final UniqueKey<AlbumsRecord> KEY_ALBUMS_PRIMARY = Internal.createUniqueKey(Albums.ALBUMS, "KEY_albums_PRIMARY", Albums.ALBUMS.ID);
        public static final UniqueKey<ImageTagsRecord> KEY_IMAGE_TAGS_PRIMARY = Internal.createUniqueKey(ImageTags.IMAGE_TAGS, "KEY_image_tags_PRIMARY", ImageTags.IMAGE_TAGS.IMAGE_ID, ImageTags.IMAGE_TAGS.TAG_ID);
        public static final UniqueKey<ImagesRecord> KEY_IMAGES_PRIMARY = Internal.createUniqueKey(Images.IMAGES, "KEY_images_PRIMARY", Images.IMAGES.ID);
        public static final UniqueKey<SchemaVersionRecord> KEY_SCHEMA_VERSION_PRIMARY = Internal.createUniqueKey(SchemaVersion.SCHEMA_VERSION, "KEY_schema_version_PRIMARY", SchemaVersion.SCHEMA_VERSION.INSTALLED_RANK);
        public static final UniqueKey<TagsRecord> KEY_TAGS_PRIMARY = Internal.createUniqueKey(Tags.TAGS, "KEY_tags_PRIMARY", Tags.TAGS.ID);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<AlbumTokensRecord, AlbumsRecord> ALBUM_TOKENS_IBFK_1 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_ALBUMS_PRIMARY, AlbumTokens.ALBUM_TOKENS, "album_tokens_ibfk_1", AlbumTokens.ALBUM_TOKENS.ALBUM_ID);
        public static final ForeignKey<AlbumTokensRecord, AccessTokensRecord> ALBUM_TOKENS_IBFK_2 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_ACCESS_TOKENS_PRIMARY, AlbumTokens.ALBUM_TOKENS, "album_tokens_ibfk_2", AlbumTokens.ALBUM_TOKENS.ACCESS_TOKEN_ID);
        public static final ForeignKey<AlbumsRecord, ImagesRecord> ALBUMS_IBFK_1 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_IMAGES_PRIMARY, Albums.ALBUMS, "albums_ibfk_1", Albums.ALBUMS.BANNER_IMAGE_ID);
        public static final ForeignKey<AlbumsRecord, AlbumsRecord> ALBUMS_IBFK_2 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_ALBUMS_PRIMARY, Albums.ALBUMS, "albums_ibfk_2", Albums.ALBUMS.PARENT_ALBUM_ID);
        public static final ForeignKey<ImageTagsRecord, ImagesRecord> IMAGE_TAGS_IBFK_1 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_IMAGES_PRIMARY, ImageTags.IMAGE_TAGS, "image_tags_ibfk_1", ImageTags.IMAGE_TAGS.IMAGE_ID);
        public static final ForeignKey<ImageTagsRecord, TagsRecord> IMAGE_TAGS_IBFK_2 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_TAGS_PRIMARY, ImageTags.IMAGE_TAGS, "image_tags_ibfk_2", ImageTags.IMAGE_TAGS.TAG_ID);
        public static final ForeignKey<ImagesRecord, AlbumsRecord> IMAGES_IBFK_1 = Internal.createForeignKey(raubach.fricklweb.server.database.Keys.KEY_ALBUMS_PRIMARY, Images.IMAGES, "images_ibfk_1", Images.IMAGES.ALBUM_ID);
    }
}

/*
 * This file is generated by jOOQ.
 */
package raubach.frickl.next.codegen.tables.records;


import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;

import raubach.frickl.next.codegen.tables.ImageTags;


/**
 * This table joins `images` and `tags` and therefore defines which tags an
 * image is tagged with.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ImageTagsRecord extends UpdatableRecordImpl<ImageTagsRecord> implements Record2<Integer, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>frickl.image_tags.image_id</code>. The foreign key id of
     * the referenced image.
     */
    public void setImageId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>frickl.image_tags.image_id</code>. The foreign key id of
     * the referenced image.
     */
    public Integer getImageId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>frickl.image_tags.tag_id</code>. The foreign key id of
     * the referenced tag.
     */
    public void setTagId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>frickl.image_tags.tag_id</code>. The foreign key id of
     * the referenced tag.
     */
    public Integer getTagId() {
        return (Integer) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Integer, Integer> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Integer> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, Integer> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ImageTags.IMAGE_TAGS.IMAGE_ID;
    }

    @Override
    public Field<Integer> field2() {
        return ImageTags.IMAGE_TAGS.TAG_ID;
    }

    @Override
    public Integer component1() {
        return getImageId();
    }

    @Override
    public Integer component2() {
        return getTagId();
    }

    @Override
    public Integer value1() {
        return getImageId();
    }

    @Override
    public Integer value2() {
        return getTagId();
    }

    @Override
    public ImageTagsRecord value1(Integer value) {
        setImageId(value);
        return this;
    }

    @Override
    public ImageTagsRecord value2(Integer value) {
        setTagId(value);
        return this;
    }

    @Override
    public ImageTagsRecord values(Integer value1, Integer value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ImageTagsRecord
     */
    public ImageTagsRecord() {
        super(ImageTags.IMAGE_TAGS);
    }

    /**
     * Create a detached, initialised ImageTagsRecord
     */
    public ImageTagsRecord(Integer imageId, Integer tagId) {
        super(ImageTags.IMAGE_TAGS);

        setImageId(imageId);
        setTagId(tagId);
    }

    /**
     * Create a detached, initialised ImageTagsRecord
     */
    public ImageTagsRecord(raubach.frickl.next.codegen.tables.pojos.ImageTags value) {
        super(ImageTags.IMAGE_TAGS);

        if (value != null) {
            setImageId(value.getImageId());
            setTagId(value.getTagId());
        }
    }
}
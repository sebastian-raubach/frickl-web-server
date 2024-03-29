/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;


/**
 * This table contains all tags/keywords that have been defined and assigned to
 * images.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tags implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer   id;
    private String    name;
    private Timestamp createdOn;
    private Timestamp updatedOn;

    public Tags() {}

    public Tags(Tags value) {
        this.id = value.id;
        this.name = value.name;
        this.createdOn = value.createdOn;
        this.updatedOn = value.updatedOn;
    }

    public Tags(
        Integer   id,
        String    name,
        Timestamp createdOn,
        Timestamp updatedOn
    ) {
        this.id = id;
        this.name = name;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    /**
     * Getter for <code>frickl.tags.id</code>. Auto incremented id of this
     * table.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>frickl.tags.id</code>. Auto incremented id of this
     * table.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter for <code>frickl.tags.name</code>. The name of this tag.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>frickl.tags.name</code>. The name of this tag.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for <code>frickl.tags.created_on</code>. When this record has been
     * created.
     */
    public Timestamp getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Setter for <code>frickl.tags.created_on</code>. When this record has been
     * created.
     */
    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * Getter for <code>frickl.tags.updated_on</code>. When this record has last
     * been updated.
     */
    public Timestamp getUpdatedOn() {
        return this.updatedOn;
    }

    /**
     * Setter for <code>frickl.tags.updated_on</code>. When this record has last
     * been updated.
     */
    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Tags (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(updatedOn);

        sb.append(")");
        return sb.toString();
    }
}

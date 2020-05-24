/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;


/**
 * This table contains all tags that can be used to access folders that aren't 
 * public.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AccessTokens implements Serializable {

    private static final long serialVersionUID = 1426629822;

    private Integer   id;
    private String    token;
    private Timestamp expiresOn;
    private Timestamp createdOn;
    private Timestamp updatedOn;

    public AccessTokens() {}

    public AccessTokens(AccessTokens value) {
        this.id = value.id;
        this.token = value.token;
        this.expiresOn = value.expiresOn;
        this.createdOn = value.createdOn;
        this.updatedOn = value.updatedOn;
    }

    public AccessTokens(
        Integer   id,
        String    token,
        Timestamp expiresOn,
        Timestamp createdOn,
        Timestamp updatedOn
    ) {
        this.id = id;
        this.token = token;
        this.expiresOn = expiresOn;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpiresOn() {
        return this.expiresOn;
    }

    public void setExpiresOn(Timestamp expiresOn) {
        this.expiresOn = expiresOn;
    }

    public Timestamp getCreatedOn() {
        return this.createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public Timestamp getUpdatedOn() {
        return this.updatedOn;
    }

    public void setUpdatedOn(Timestamp updatedOn) {
        this.updatedOn = updatedOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AccessTokens (");

        sb.append(id);
        sb.append(", ").append(token);
        sb.append(", ").append(expiresOn);
        sb.append(", ").append(createdOn);
        sb.append(", ").append(updatedOn);

        sb.append(")");
        return sb.toString();
    }
}

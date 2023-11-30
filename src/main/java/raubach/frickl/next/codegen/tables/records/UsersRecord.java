/*
 * This file is generated by jOOQ.
 */
package raubach.frickl.next.codegen.tables.records;


import java.sql.Timestamp;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

import raubach.frickl.next.codegen.enums.UsersViewType;
import raubach.frickl.next.codegen.tables.Users;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UsersRecord extends UpdatableRecordImpl<UsersRecord> implements Record8<Integer, String, String, Short, UsersViewType, Timestamp, Timestamp, Timestamp> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>frickl.users.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>frickl.users.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>frickl.users.username</code>.
     */
    public void setUsername(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>frickl.users.username</code>.
     */
    public String getUsername() {
        return (String) get(1);
    }

    /**
     * Setter for <code>frickl.users.password</code>.
     */
    public void setPassword(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>frickl.users.password</code>.
     */
    public String getPassword() {
        return (String) get(2);
    }

    /**
     * Setter for <code>frickl.users.permissions</code>.
     */
    public void setPermissions(Short value) {
        set(3, value);
    }

    /**
     * Getter for <code>frickl.users.permissions</code>.
     */
    public Short getPermissions() {
        return (Short) get(3);
    }

    /**
     * Setter for <code>frickl.users.view_type</code>.
     */
    public void setViewType(UsersViewType value) {
        set(4, value);
    }

    /**
     * Getter for <code>frickl.users.view_type</code>.
     */
    public UsersViewType getViewType() {
        return (UsersViewType) get(4);
    }

    /**
     * Setter for <code>frickl.users.last_login</code>.
     */
    public void setLastLogin(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>frickl.users.last_login</code>.
     */
    public Timestamp getLastLogin() {
        return (Timestamp) get(5);
    }

    /**
     * Setter for <code>frickl.users.created_on</code>.
     */
    public void setCreatedOn(Timestamp value) {
        set(6, value);
    }

    /**
     * Getter for <code>frickl.users.created_on</code>.
     */
    public Timestamp getCreatedOn() {
        return (Timestamp) get(6);
    }

    /**
     * Setter for <code>frickl.users.updated_on</code>.
     */
    public void setUpdatedOn(Timestamp value) {
        set(7, value);
    }

    /**
     * Getter for <code>frickl.users.updated_on</code>.
     */
    public Timestamp getUpdatedOn() {
        return (Timestamp) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<Integer, String, String, Short, UsersViewType, Timestamp, Timestamp, Timestamp> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<Integer, String, String, Short, UsersViewType, Timestamp, Timestamp, Timestamp> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return Users.USERS.ID;
    }

    @Override
    public Field<String> field2() {
        return Users.USERS.USERNAME;
    }

    @Override
    public Field<String> field3() {
        return Users.USERS.PASSWORD;
    }

    @Override
    public Field<Short> field4() {
        return Users.USERS.PERMISSIONS;
    }

    @Override
    public Field<UsersViewType> field5() {
        return Users.USERS.VIEW_TYPE;
    }

    @Override
    public Field<Timestamp> field6() {
        return Users.USERS.LAST_LOGIN;
    }

    @Override
    public Field<Timestamp> field7() {
        return Users.USERS.CREATED_ON;
    }

    @Override
    public Field<Timestamp> field8() {
        return Users.USERS.UPDATED_ON;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getUsername();
    }

    @Override
    public String component3() {
        return getPassword();
    }

    @Override
    public Short component4() {
        return getPermissions();
    }

    @Override
    public UsersViewType component5() {
        return getViewType();
    }

    @Override
    public Timestamp component6() {
        return getLastLogin();
    }

    @Override
    public Timestamp component7() {
        return getCreatedOn();
    }

    @Override
    public Timestamp component8() {
        return getUpdatedOn();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getUsername();
    }

    @Override
    public String value3() {
        return getPassword();
    }

    @Override
    public Short value4() {
        return getPermissions();
    }

    @Override
    public UsersViewType value5() {
        return getViewType();
    }

    @Override
    public Timestamp value6() {
        return getLastLogin();
    }

    @Override
    public Timestamp value7() {
        return getCreatedOn();
    }

    @Override
    public Timestamp value8() {
        return getUpdatedOn();
    }

    @Override
    public UsersRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public UsersRecord value2(String value) {
        setUsername(value);
        return this;
    }

    @Override
    public UsersRecord value3(String value) {
        setPassword(value);
        return this;
    }

    @Override
    public UsersRecord value4(Short value) {
        setPermissions(value);
        return this;
    }

    @Override
    public UsersRecord value5(UsersViewType value) {
        setViewType(value);
        return this;
    }

    @Override
    public UsersRecord value6(Timestamp value) {
        setLastLogin(value);
        return this;
    }

    @Override
    public UsersRecord value7(Timestamp value) {
        setCreatedOn(value);
        return this;
    }

    @Override
    public UsersRecord value8(Timestamp value) {
        setUpdatedOn(value);
        return this;
    }

    @Override
    public UsersRecord values(Integer value1, String value2, String value3, Short value4, UsersViewType value5, Timestamp value6, Timestamp value7, Timestamp value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UsersRecord
     */
    public UsersRecord() {
        super(Users.USERS);
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    public UsersRecord(Integer id, String username, String password, Short permissions, UsersViewType viewType, Timestamp lastLogin, Timestamp createdOn, Timestamp updatedOn) {
        super(Users.USERS);

        setId(id);
        setUsername(username);
        setPassword(password);
        setPermissions(permissions);
        setViewType(viewType);
        setLastLogin(lastLogin);
        setCreatedOn(createdOn);
        setUpdatedOn(updatedOn);
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    public UsersRecord(raubach.frickl.next.codegen.tables.pojos.Users value) {
        super(Users.USERS);

        if (value != null) {
            setId(value.getId());
            setUsername(value.getUsername());
            setPassword(value.getPassword());
            setPermissions(value.getPermissions());
            setViewType(value.getViewType());
            setLastLogin(value.getLastLogin());
            setCreatedOn(value.getCreatedOn());
            setUpdatedOn(value.getUpdatedOn());
        }
    }
}
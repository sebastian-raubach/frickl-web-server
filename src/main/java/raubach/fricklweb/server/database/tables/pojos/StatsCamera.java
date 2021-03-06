/*
 * This file is generated by jOOQ.
 */
package raubach.fricklweb.server.database.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;


/**
 * VIEW
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class StatsCamera implements Serializable {

    private static final long serialVersionUID = -1004326102;

    private String camera;
    private Long   count;

    public StatsCamera() {}

    public StatsCamera(StatsCamera value) {
        this.camera = value.camera;
        this.count = value.count;
    }

    public StatsCamera(
        String camera,
        Long   count
    ) {
        this.camera = camera;
        this.count = count;
    }

    public String getCamera() {
        return this.camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public Long getCount() {
        return this.count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StatsCamera (");

        sb.append(camera);
        sb.append(", ").append(count);

        sb.append(")");
        return sb.toString();
    }
}

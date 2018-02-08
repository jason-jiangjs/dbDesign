package org.vog.base.model.mongo;


import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.JacksonUtil;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * BaseMongoModel
 *
 * @author chuanyu.liang, 12/11/15
 * @version 2.0.0
 * @since 2.0.0
 */
@Document
public class BaseMongoModel implements Serializable {

    @Id
    protected String _id;
    public String get_id() {
        return _id;
    }
    public void set_id(String _id) {
        this._id = _id;
    }

    protected Date created = DateTimeUtil.getDate();
    protected String creator = "0";
    protected Date modified = DateTimeUtil.getDate();
    protected String modifier = "0";
    protected int active = 1;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this._id == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
            return false;
        }
        BaseMongoModel that = (BaseMongoModel) obj;
        return this._id.equals(that.get_id());
    }

    @Override
    public int hashCode() {
        return _id == null ? 0 : _id.hashCode();
    }

    @Override
    public String toString() {
        return JacksonUtil.bean2Json(this);
    }
}

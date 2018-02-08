package org.vog.base.model.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;

/**
 * BaseMongoModel
 *
 * @author chuanyu.liang, 12/11/15
 * @version 2.0.0
 * @since 2.0.0
 */
@Document
public class BaseMongoMapModel extends BaseMongoMap<String, Object> {


    public String get_id() {
        return getStringAttribute("_id");
    }
    public void set_id(String _id) {
        setAttribute("_id", _id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.get_id() == null || obj == null || !(this.getClass().equals(obj.getClass()))) {
            return false;
        }
        BaseMongoMapModel that = (BaseMongoMapModel) obj;
        return this.get_id().equals(that.get_id());
    }

    @Override
    public int hashCode() {
        return this.get_id() == null ? 0 : this.get_id().hashCode();
    }

    @JsonIgnore
    protected void setUpdateDate(Update update, String key, Object value)
    {
        if (value == null) {
            return;
        }
        update.set(key, value);
    }
}

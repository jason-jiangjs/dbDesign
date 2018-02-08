package org.vog.base.model.mongo;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * BaseMongoModel
 *
 * @author chuanyu.liang, 12/11/15
 * @version 2.0.0
 * @since 2.0.0
 */
@Document
public class BaseMongoAuditable extends BaseMongoMapModel {

    public Date getCreated() {
        return getAttribute("created");
    }

    public void setCreated(Date created) {
        if (created == null) {
            return;
        }
        setAttribute("created", created);
    }

    public String getCreator() {
        return getAttribute("creator");
    }

    public void setCreator(String creator) {
        if (creator == null) {
            return;
        }
        setAttribute("creator", creator);
    }

    public Date getModified() {
        return getAttribute("modified");
    }

    public void setModified(Date modified) {
        if (modified == null) {
            return;
        }
        setAttribute("modified", modified);
    }

    public String getModifier() {
        return getAttribute("modifier");
    }

    public void setModifier(String modifier) {
        if (modifier == null) {
            return;
        }
        setAttribute("modifier", modifier);
    }
}

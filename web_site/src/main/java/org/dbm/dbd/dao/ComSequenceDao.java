package org.dbm.dbd.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.stereotype.Repository;

/**
 * 各实体类的序列号
 */
@Repository
public class ComSequenceDao extends BaseMongoDao {

    /**
     * 创建序列号
     */
    public long getNextSequence(String name) {
        DBObject dbObject = getNextSequence_(name);

        if (dbObject == null) {
            createSequence(name);
            dbObject = getNextSequence_(name);
        }

        Object seqObj = dbObject.get("seq");
        if (seqObj instanceof Double) {
            Double seqDouble = (Double)seqObj;
            return seqDouble.longValue();
        }

        return  Long.parseLong(seqObj.toString());
    }

    private DBCollection getDBCollection() {
        return mongoTemplate.getCollection("com_sequence");
    }

    private DBObject getNextSequence_(String name) {
        DBCollection coll = getDBCollection();
        BasicDBObject searchQuery = new BasicDBObject("_id", name);
        BasicDBObject increase = new BasicDBObject("seq", 1);
        BasicDBObject updateQuery = new BasicDBObject("$inc", increase);
        return coll.findAndModify(searchQuery, null, null, false, updateQuery, true, false);
    }

    private void createSequence(String name) {
        DBCollection coll = getDBCollection();
        DBObject insertObject = new BasicDBObject("_id", name);
        insertObject.put("seq", 1);
        coll.save(insertObject);
    }

}

package org.dbm.dbd.dao;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.dbm.common.base.dao.mongo.BaseMongoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;

@Repository
public class GFSFileDao extends BaseMongoDao {

    @Autowired
    private GridFsTemplate gfsTemplate;

    // mongo表名
    private static final String COLL_NAME = "fs.files";

    @Override
    public String getTableName() {
        return COLL_NAME;
    }

    /**
     * 根据指定条件查询文件
     */
    public List<GridFSDBFile> findFile(Query query) {
        return gfsTemplate.find(query);
    }

    /**
     * 保存文件
     */
    public GridFSFile saveFile(InputStream content, String fileName, String contentType, Object metadata) {
        return gfsTemplate.store(content, fileName, contentType, metadata);
    }

}

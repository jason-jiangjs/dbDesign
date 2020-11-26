package org.dbm.dbd.service;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.base.service.BaseService;
import org.dbm.dbd.dao.GFSFileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class GFSFileService extends BaseService {

    @Autowired
    private GFSFileDao fileDao;

    /**
     * 保存文件
     */
    public GridFSFile saveFile(InputStream content, String fileName, String contentType, Object metadata) {
        GridFSFile file = fileDao.saveFile(content, fileName, contentType, metadata);


        return file;
    }


    /**
     * 查询指定数据库
     */
    public List<GridFSDBFile> findFile(String fileId) {
        Query queryObj = new Query(where("id").is(fileId));
        return fileDao.findFile(queryObj);
    }

    /**
     * 查询数据库一览
     */
    public List<BaseMongoMap> findDbList(int page, int limit, boolean checked) {

        return null;
    }

}

package org.dbm.dbd.service;

import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.bson.types.ObjectId;
import org.dbm.common.base.service.BaseService;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.dbd.dao.GFSFileDao;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 删除指定文件(逻辑删除)
     */
    public void deleteFile(Long userId, String fileId) {
        Map<String, Object> infoMap = new HashMap<>();
        long currTime = DateTimeUtil.getDate().getTime();
        infoMap.put("metadata.versionId", currTime);
        infoMap.put("metadata.valid", false);
        infoMap.put("metadata.modifierId", userId);
        infoMap.put("metadata.modifierName", BizCommUtil.getLoginUserName());
        infoMap.put("metadata.modifiedTime", currTime);

        Query query = new Query(where("_id").is(new ObjectId(fileId)));
        WriteResult rst = fileDao.updateObject(query, infoMap, false, false);
        if (rst.isUpdateOfExisting()) {
            logger.warn("deleteFile 不成功");
        }
    }

    /**
     * 查询指定文件
     */
    public List<GridFSDBFile> findFile(String fileId) {
        Query queryObj = new Query(where("_id").is(fileId));
        return fileDao.findFile(queryObj);
    }

    /**
     * 查询文件
     */
    public List<GridFSDBFile> findFileList(long dbId, long tblId) {
        Query queryObj = new Query(where("metadata.dbId").is(dbId));
        queryObj.addCriteria(where("metadata.tableId").is(tblId));
        queryObj.addCriteria(where("metadata.valid").is(true));
        return fileDao.findFile(queryObj);
    }

}

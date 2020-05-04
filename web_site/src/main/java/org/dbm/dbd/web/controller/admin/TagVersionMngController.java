package org.dbm.dbd.web.controller.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.model.AuditDataBean;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.ErChartService;
import org.dbm.dbd.service.TableService;
import org.dbm.dbd.service.TagVersionService;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 版本发布管理
 */
@RestController
public class TagVersionMngController extends BaseController {

    @Autowired
    private DbService dbService;

    @Autowired
    private ErChartService erChartService;

    @Autowired
    private TableService tableService;

    @Autowired
    private TagVersionService tagVersionService;

    /**
     * 发布新版本时，获取默认的版本名称
     */
    @RequestMapping(value = "/ajax/mng/getDefaultTagName", method = RequestMethod.GET)
    public Map<String, Object> getDefaultTagName(@RequestParam Map<String, String> params) {
        Long dbId = BizCommUtil.getSelectedDbId();
        Long userId = getLoginUserId();
        BaseMongoMap dbObj = dbService.findDbById(dbId);
        if (dbObj == null) {
            logger.warn("getDefaultTagName 数据库不存在/已删除 dbId={} userId={}", dbId, userId);
            return ApiResponseUtil.error(ErrorCode.E5001, "该数据库不存在,或者已被删除");
        }

        Map<String, Object> data = new HashMap<>();
        String lastTagName = dbObj.getStringAttribute("lastTagName");
        String nowDate = DateTimeUtil.getNow(DateTimeUtil.COMPRESS_DATE_FORMAT);
        if (lastTagName == null) {
            data.put("lastTagName", "TAG-" + nowDate + "-001");
        } else {
            String[] tagArr = lastTagName.split("-");
            if (tagArr.length != 3) {
                data.put("lastTagName", "TAG-" + nowDate + "-001");
            } else {
                if (nowDate.equals(tagArr[1])) {
                    // 当天
                    int seqNum = NumberUtils.toInt(tagArr[2]) + 1;
                    data.put("lastTagName", "TAG-" + nowDate + "-" + StringUtils.leftPad(Integer.toString(seqNum), 3, "0"));
                } else {
                    data.put("lastTagName", "TAG-" + nowDate + "-001");
                }
            }
        }
        return ApiResponseUtil.success(data);
    }

    /**
     * 创建新版本
     */
    @RequestMapping(value = "/ajax/mng/createNewTag", method = RequestMethod.POST)
    public Map<String, Object> createNewTag(@RequestBody Map<String, Object> params) {
        Long dbId = BizCommUtil.getSelectedDbId();
        Long userId = getLoginUserId();
        String newTagName = StringUtils.trimToNull((String) params.get("lastTagName"));
        if (newTagName == null) {
            logger.warn("createNewTag 必须填写新版本名称 dbId={} userId={}", dbId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数，必须填写新版本名称");
        }
        String tagDesc = StringUtils.trimToNull((String) params.get("tagDesc"));
        if (tagDesc == null) {
            logger.warn("createNewTag 必须填写版本说明 dbId={} userId={}", dbId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少参数，必须填写版本说明");
        }

        String nowDate = DateTimeUtil.getNow(DateTimeUtil.COMPRESS_DATE_FORMAT);
        String[] tagArr = newTagName.split("-");
        if (tagArr.length != 3) {
            logger.warn("createNewTag 新版本名称格式不正确 newTagName={} dbId={} userId={}", newTagName, dbId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "参数错误，新版本名称格式错误，必须是如'TAG-{}-001'的形式", nowDate);
        }
        if (!nowDate.equals(tagArr[1])) {
            // 必须是当天
            logger.warn("createNewTag 新版本名称格式不正确 newTagName={} dbId={} userId={}", newTagName, dbId, userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "参数错误，新版本名称格式错误，中间一项必须是当天日期");
        }

        // 保存项目信息
        long nowTime = DateTimeUtil.getDate().getTime();
        params.put("auditData.modifierId", userId);
        params.put("auditData.modifierName", BizCommUtil.getLoginUserName());
        params.put("auditData.modifiedTime", nowTime);
        dbService.saveDb(dbId, params);

        // 保存版本关联信息
        Map<String, Object> xrefData = new HashMap<>();
        // 先取出当前所有最新表定义(有效的)，先不考虑视图类型
        // TODO--这里先不考虑性能，一次取出所有数据，以后再优化
        List<BaseMongoMap> dataList = tableService.getTableNameList(null, dbId, 0, 0, 0, false);
        List<String> searchKeys = new ArrayList<>(dataList.size());
        dataList.forEach(item -> {
            searchKeys.add(item.getStringAttribute("_id") + "-" + item.getStringAttribute("versionId"));
        });
        xrefData.put("searchKeys", searchKeys);

        // 再取出ER图定义, 没有数据则忽略
        BaseMongoMap erObj = erChartService.findErChartByDbId(dbId);
        if (erObj == null) {
            logger.warn("createNewTag ER图数据不存在,忽略 dbId={} userId={}", dbId, userId);
        } else {
            xrefData.put("erChartSearchKey", erObj.getStringAttribute("_id") + "-" + erObj.getStringAttribute("versionId"));
        }

        xrefData.put("dbId", dbId);
        xrefData.put("tagName", newTagName);
        xrefData.put("tagDesc", tagDesc);

        AuditDataBean auditDataBean = new AuditDataBean();
        auditDataBean.setModifierId(userId);
        auditDataBean.setModifierName(BizCommUtil.getLoginUserName());
        auditDataBean.setModifiedTime(nowTime);
        xrefData.put("auditData", auditDataBean);
        tagVersionService.saveTagDetail(xrefData);

        return ApiResponseUtil.success();
    }

    /**
     * 获取所有发布的版本列表
     */
    @RequestMapping(value = "/ajax/mng/getProjTagList", method = RequestMethod.GET)
    public List<BaseMongoMap> getProjTagList(@RequestParam Map<String, Object> params) {
        Long dbId = BizCommUtil.getSelectedDbId();
        Long userId = getLoginUserId();

        List<BaseMongoMap> dbMapList = tagVersionService.getTagList(dbId, 0, 0);
        if (dbMapList == null || dbMapList.isEmpty()) {
            // 表不存在
            logger.warn("getProjTagList 版本列表不存在 dbId={}, userId={}", dbId, userId);
            return Collections.EMPTY_LIST;
        }
        return dbMapList;
    }

}

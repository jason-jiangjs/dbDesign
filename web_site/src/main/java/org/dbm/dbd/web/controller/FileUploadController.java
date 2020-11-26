package org.dbm.dbd.web.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.GFSFileService;
import org.dbm.dbd.web.util.BizCommUtil;
import org.dbm.dbd.web.util.SystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 共同业务管理
 */
@RestController
public class FileUploadController extends BaseController {

    @Autowired
    private GFSFileService fileService;

    /**
     * 文件上传
     */
    @RequestMapping(value = "/ajax/common/fileUpload", method = RequestMethod.POST)
    public Map<String, Object> fileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                Map<String, Object> metaData = new HashMap<>();
                metaData.put("dbId", StringUtil.convertToLong(request.getParameter("dbId")));
                metaData.put("tableId", StringUtil.convertToLong(request.getParameter("tableId")));
                metaData.put("size", request.getParameter("realSize"));
                metaData.put("lastModifiedDate", request.getParameter("lastModifiedDate"));
                metaData.put("valid", true);
                metaData.put("creatorId", getLoginUserId());
                metaData.put("createdTime", DateTimeUtil.getDate().getTime());

                GridFSFile gfsFile = fileService.saveFile(file.getInputStream(), fileName, file.getContentType(), metaData);
                Map<String, Object> data = new HashMap<>();
                ApplicationContext app = SystemProperty.getApplicationContext();
                data.put("fileUrl", app.getApplicationName() + "/file/attachment?fileId=" + gfsFile.getId().toString());
                data.put("fileName", fileName);
                data.put("fileId", gfsFile.getId().toString());
                data.put("contentType", file.getContentType());
                return data;
            } else {
                logger.warn("DeliveryManagerController.deliveryUpload file is null ");

            }
        } catch (Exception e) {
            logger.error("上传文件时出错", e);

        }
        Map<String, Object> data = new HashMap<>();
        return data;
    }

    /**
     * 下载文件
     */
    @GetMapping(value = "/file/attachment")
    public void downContractDocuments(HttpServletResponse response,
                                      @RequestParam(value = "fileId", required = true) String fileId)
    {
        List<GridFSDBFile> fileList = fileService.findFile(fileId);
        if (fileList != null && fileList.size() > 0)
        {
            InputStream is = null;
            OutputStream os = null;
            try
            {
                GridFSDBFile file = fileList.get(0);
                response.setContentType(file.getContentType());
                response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(file.getFilename(), "UTF-8"));
                response.addHeader("filename", file.getFilename());
                response.addHeader("Access-Control-Expose-Headers", "filename");

                is = file.getInputStream();
                os = response.getOutputStream();
                byte[] temp = new byte[1024];
                int len = 0;
                while ((len = is.read(temp)) != -1)
                {
                    os.write(temp, 0, len);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (os != null)
                    {
                        os.close();
                    }
                    if (is != null)
                    {
                        is.close();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据条件取得指定附件列表
     */
    @RequestMapping(value = "/ajax/common/file/getAttaList", method = RequestMethod.GET)
    public Map<String, Object> getAttaList(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tblId"));
        long dbId = StringUtil.convertToLong(params.get("dbId"));
        if (dbId == 0) {
            logger.warn("getAttaList 缺少dbId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的项目.(缺少参数 dbId)");
        }

        Long selectedDb = BizCommUtil.getSelectedDbId();
        if (selectedDb == null) {
            logger.warn("getAttaList 未选择指定的项目 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.E5001, "错误,未选择指定的项目.");
        }
        if (!selectedDb.equals(Long.valueOf(dbId))) {
            logger.warn("getAttaList 不一致的指定项目 userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.E5001, "错误,不一致的指定项目.");
        }

        ApplicationContext app = SystemProperty.getApplicationContext();

        List<Map<String, String>> fileList = fileService.findFileList(dbId, tblId).stream().map(item -> {
            Map<String, String> mapData = new HashMap<>();
            mapData.put("fileUrl", app.getApplicationName() + "/file/attachment?fileId=" + item.getId().toString());
            mapData.put("fileName", item.getFilename());
            mapData.put("fileId", item.getId().toString());
            mapData.put("contentType", item.getContentType());
            return mapData;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("fileList", fileList);
        return ApiResponseUtil.success(data);
    }

    /**
     * 删除指定附件
     */
    @RequestMapping(value = "/ajax/common/file/deleteAtta", method = RequestMethod.POST)
    public Map<String, Object> deleteAttaFile(@RequestBody Map<String, Object> params) {
        Long userId = getLoginUserId();
        String fileId = (String) params.get("fileId");
        if (fileId == null) {
            logger.warn("deleteAttaFile 缺少fileId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的文件.(缺少参数 fileId)");
        }

        fileService.deleteFile(userId, fileId);
        return ApiResponseUtil.success();
    }

}

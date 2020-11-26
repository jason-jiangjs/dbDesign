package org.dbm.dbd.web.controller;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.dbm.common.ErrorCode;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.ApiResponseUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.GFSFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                metaData.put("dbId", request.getParameter("dbId"));
                metaData.put("tableId", request.getParameter("tableId"));
                metaData.put("size", request.getParameter("realSize"));
                metaData.put("lastModifiedDate", request.getParameter("lastModifiedDate"));

                GridFSFile gfsFile = fileService.saveFile(file.getInputStream(), fileName, file.getContentType(), metaData);
                Map<String, Object> data = new HashMap<>();
                data.put("fileUrl", "/file/attachment?fileId=" + gfsFile.getId().toString());
                data.put("fileName", fileName);
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
                is = fileList.get(0).getInputStream();
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
     * 根据条件取得指定附件
     */
    @RequestMapping(value = "/ajax/common/file/getAttaList", method = RequestMethod.GET)
    public Map<String, Object> getAttaList(@RequestParam Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getTable 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblId)");
        }


        Map<String, Object> data = new HashMap<>();

        return ApiResponseUtil.success(data);
    }

    /**
     * 删除指定附件
     */
    @RequestMapping(value = "/ajax/common/file/deleteAtta", method = RequestMethod.POST)
    public Map<String, Object> getTable(@RequestBody Map<String, String> params) {
        Long userId = getLoginUserId();
        long tblId = StringUtil.convertToLong(params.get("tblId"));
        if (tblId == 0) {
            logger.warn("getTable 缺少tblId userId={}", userId);
            return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 tblId)");
        }


        Map<String, Object> data = new HashMap<>();

        return ApiResponseUtil.success(data);
    }

}

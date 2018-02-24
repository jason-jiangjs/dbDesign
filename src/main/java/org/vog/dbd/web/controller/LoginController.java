package org.vog.dbd.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.vog.base.controller.BaseController;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.Constants;
import org.vog.common.ErrorCode;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.TableService;
import org.vog.dbd.service.UserService;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.util.Map;

/**
 * 用户登录操作
 */
@Controller
public class LoginController extends BaseController {

    @Autowired
    private TableService tableService;

    @Autowired
    private UserService userService;

    /**
     * 登录成功
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        int checkFlg = StringUtil.convertToInt(params.get("type"));

        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        Long dbId = userObj.getFavorite();
        if (dbId != null && dbId != 0 && checkFlg == 0) {
            model.setViewName("table/table_list");

            BaseMongoMap dbMap = tableService.findDbById(dbId);
            if (dbMap == null || dbMap.isEmpty()) {
                // 数据库不存在
                model.addObject("dbId", 0);
            } else {
                model.addObject("dbId", dbId);
                int dbType = dbMap.getIntAttribute("type");
                if (dbType == 0) {
                    logger.warn("getColumnList 未设置数据库类型 id={}", dbId);
                }
                model.addObject("dbType", dbType);
                model.addObject("dbName", dbMap.getStringAttribute("dbName"));
            }
        } else {
            model.setViewName("db_list");
            model.addObject("dbList", tableService.findDbList());
        }
        return model;
    }

    /**
     * 设置/取消缺省工作环境
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/setDefaultDbEnv", method = RequestMethod.POST)
    public Map<String, Object> setDefaultDbEnv(@RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }
        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();

        int checkFlg = StringUtil.convertToInt(params.get("checkFlg"));
        if (checkFlg == 1) {
            // 保存默认工作环境
            long dbId = StringUtil.convertToLong(params.get("dbId"));
            if (dbId == 0) {
                logger.warn("getColumnList 缺少dbId userId={}", userId);
                return ApiResponseUtil.error(ErrorCode.W1001, "错误操作,未选择指定的表.(缺少参数 dbId)");
            }
            userService.setUserFavorite(userObj.getId(), dbId);
        } else {
            // 取消默认工作环境
            userService.setUserFavorite(userObj.getId(), 0);
        }

        return ApiResponseUtil.success();
    }
}

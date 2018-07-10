package org.vog.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.vog.base.controller.BaseController;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.Constants;
import org.vog.common.ErrorCode;
import org.vog.common.util.ApiResponseUtil;
import org.vog.common.util.DateTimeUtil;
import org.vog.common.util.StringUtil;
import org.vog.dbd.service.DbService;
import org.vog.dbd.service.UserService;
import org.vog.dbd.web.login.CustomerUserDetails;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录操作
 */
@Controller
public class LoginController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private DbService dbService;

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
            // 设置了默认工作环境
            model.setViewName("table/table_list");

            BaseMongoMap dbMap = dbService.findDbById(dbId);
            if (dbMap == null || dbMap.isEmpty()) {
                // 数据库不存在
                model.addObject("dbId", 0);
            } else {
                request.getSession().setAttribute("_dbId", dbId);
                model.addObject("dbId", dbId.toString());
                int dbType = dbMap.getIntAttribute("type");
                if (dbType == 0) {
                    logger.warn("getColumnList 未设置数据库类型 id={}", dbId);
                }
                int userReadonly = 1;
                if (userObj.getAuthorities() != null &&
                        (userObj.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_WRITABLE")) ||
                                userObj.getAuthorities().contains(new SimpleGrantedAuthority("PROJ_MNG_USER")) ||
                                userObj.getAuthorities().contains(new SimpleGrantedAuthority("ADMIN_USER")))) {
                    userReadonly = 0;
                }
                model.addObject("userReadonly", userReadonly);
                model.addObject("dbType", dbType);
                model.addObject("dbName", dbMap.getStringAttribute("dbName"));
            }
        } else {
            model.setViewName("db_list");
            model.addObject("dbList", userService.findUserDbList(userObj.getId(), true));
        }

        // 标记当前用户为已登录
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("inLogin", 1);
        infoMap.put("loginTime", DateTimeUtil.getNowTime());
        userService.updateUserInfo(userObj.getId(), infoMap);
        return model;
    }

    /**
     * 去修改密码画面
     */
    @RequestMapping(value = "/changePasswd", method = RequestMethod.GET)
    public ModelAndView changePasswd() {
        ModelAndView model = new ModelAndView();
        model.setViewName("changePassword");
        return model;
    }

    /**
     * 保存新密码(session中必须已有user_id)
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/savePasswd", method = RequestMethod.POST)
    public Map<String, Object> savePasswd(@RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getSession().getAttribute(Constants.KEY_USER_ID);
        if (userId == null || userId == 0) {
            logger.error("用户未登录 sessionid={}", request.getSession().getId());
            return ApiResponseUtil.error(ErrorCode.S9004, "用户未登录");
        }

        String oldPasswd = StringUtils.trimToNull((String) params.get("oldPasswd"));
        String newPasswd = StringUtils.trimToNull((String) params.get("newPasswd"));
        String newPasswdCfm = StringUtils.trimToNull((String) params.get("newPasswdCfm"));
        if (oldPasswd == null || newPasswd == null || newPasswdCfm == null) {
            logger.warn("savePasswd 缺少参数 params={}", params.toString());
            return ApiResponseUtil.error(ErrorCode.W1001, "缺少必须值.");
        }

        BaseMongoMap userObj = userService.getUserById(userId);

        // 先密码匹配验证
        BCryptPasswordEncoder cryptEncoder = new BCryptPasswordEncoder();
        if (!cryptEncoder.matches(oldPasswd, userObj.getStringAttribute("password"))) {
            logger.warn("savePasswd 旧密码错误");
            return ApiResponseUtil.error(ErrorCode.E5010, "旧密码错误.");
        }

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("password", cryptEncoder.encode(newPasswd));
        infoMap.put("status", 1);
        infoMap.put("modifier", userId);
        infoMap.put("modifiedTime", DateTimeUtil.getNowTime());
        userService.updateUserInfo(userId, infoMap);
        return ApiResponseUtil.success();
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

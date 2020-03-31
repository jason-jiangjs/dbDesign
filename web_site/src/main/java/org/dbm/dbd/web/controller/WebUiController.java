package org.dbm.dbd.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.dbm.common.Constants;
import org.dbm.common.base.controller.BaseController;
import org.dbm.common.base.model.mongo.BaseMongoMap;
import org.dbm.common.util.CommUtil;
import org.dbm.common.util.DateTimeUtil;
import org.dbm.common.util.StringUtil;
import org.dbm.dbd.service.DbService;
import org.dbm.dbd.service.UserService;
import org.dbm.dbd.web.login.CustomerUserDetails;
import org.dbm.dbd.web.util.BizCommUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 表管理
 */
@Controller
public class WebUiController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private DbService dbService;

    @Autowired
    private MessageSource messageSource;

    /**
     * 首页/登录页面
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView toIndexPage() {
        String userLanguage = (String) request.getSession().getAttribute("_user_language");
        if (userLanguage == null) {
            request.getSession().setAttribute("_user_language", request.getLocale().toString());
        }
        ModelAndView model = new ModelAndView();
        model.setViewName("index");
        return model;
    }

    /**
     * 登录成功,转到数据库选择画面
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        int checkFlg = StringUtil.convertToInt(params.get("type"));

        CustomerUserDetails userObj = BizCommUtil.getLoginUserDetails();
        Long dbId = userObj.getFavorite();
        if (dbId != null && dbId != 0 && checkFlg == 0) {
            // 设置了默认工作环境, 直接转到表一览页面
            model.setViewName("table/table_list");

            BaseMongoMap dbMap = dbService.findDbById(dbId);
            if (dbMap == null || dbMap.isEmpty()) {
                // 数据库不存在
                model.addObject("dbId", 0);
            } else {
                request.getSession().setAttribute(Constants.KEY_DB_ID, dbId);
                model.addObject("dbId", dbId.toString());
                int dbType = dbMap.getIntAttribute("dataTypeId");
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
            List<Map<String, Object>> dbList = userService.findUserDbList(userObj.getUserId(), true);
            for (Map<String, Object> infoMap : dbList) {
                if (dbId.equals(StringUtil.convertToLong(infoMap.get("id"), null))) {
                    infoMap.put("isDefaultEnv", 1);
                    break;
                }
            }
            model.addObject("dbList", dbList);
        }

        // 标记当前用户为已登录
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("inLogin", 1);
        infoMap.put("loginTime", DateTimeUtil.getNowTime());
        userService.updateUserInfo(userObj.getUserId(), infoMap);
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
     * 转到登录画面
     * TODO-- 期待更好方案，目前是因为AuthenticationFailureHandlerImpl要传参数到页面
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView tologin(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.setViewName("index");
        if (params.get("msg") != null) {
            model.addObject("errMsg", messageSource.getMessage(params.get("msg"), null, Locale.getDefault()));
        }
        return model;
    }

    /**
     * 转到系统异常画面
     */
    @RequestMapping(value = "/sys_error", method = RequestMethod.GET)
    public ModelAndView sysError(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.setViewName("sys_error");
        model.addObject("errMsg", params.get("msg"));
        return model;
    }

    /**
     * 跳转到表一览(主Tab)画面
     */
    @RequestMapping(value = "/table_list", method = RequestMethod.GET)
    public ModelAndView getTableList(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.setViewName("table/table_list");

        Long userId = getLoginUserId();
        Long dbId = BizCommUtil.getSelectedDbId();
        if (dbId == null || dbId == 0) {
            // 数据库不存在
            logger.warn("getTableList 未选择数据库 userId={}", userId);
            model.addObject("dbId", 0);
        }

        BaseMongoMap dbMap = dbService.findDbById(dbId);
        if (dbMap == null || dbMap.isEmpty()) {
            // 数据库不存在
            logger.warn("getTableList 数据库不存在 id={}", dbId);
            model.addObject("dbId", 0);
        } else {
            model.addObject("dbId", dbId);
            int dbType = dbMap.getIntAttribute("dataTypeId");
            if (dbType == 0) {
                logger.warn("getTableList 未设置数据库类型 id={}", dbId);
                model.addObject("dbId", 1);
                return model;
            }

            List<Map<String, Object>> roleList = userService.findUserDbList(userId, false);
            if (roleList == null || roleList.isEmpty()) {
                model.addObject("dbId", 2);
                return model;
            }
            int userReadonly = -1;
            for (Map<String, Object> item : roleList) {
                if (dbId.equals(item.get("dbId"))) {
                    int role = StringUtil.convertToInt(item.get("role"));
                    if (role == 1) {
                        userReadonly = 1;
                    } else if (role == 2 || role == 8 || role == 9) {
                        userReadonly = 0;
                    }
                    break;
                }
            }

            if (userReadonly == -1) {
                model.addObject("dbId", 3);
                return model;
            }

            model.addObject("userReadonly", userReadonly);
            model.addObject("dbType", dbType);
            model.addObject("dbName", dbMap.getStringAttribute("dbName"));
        }
        return model;
    }


    /**
     * 转到用户一览画面
     */
    @RequestMapping(value = "/mng/user_mng", method = RequestMethod.GET)
    public ModelAndView gotoUserMngPage(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.addObject("adminId", getLoginUserId());
        model.addObject("hasAuth", 1);
        model.setViewName("admin/user_mng");
        return model;
    }

    /**
     * 转到数据库一览画面
     */
    @RequestMapping(value = "/mng/db_mng", method = RequestMethod.GET)
    public ModelAndView gotoDbMngPage(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.addObject("adminId", getLoginUserId());
        model.addObject("hasAuth", 1);
        model.setViewName("admin/db_mng");
        return model;
    }

}

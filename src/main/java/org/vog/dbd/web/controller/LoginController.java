package org.vog.dbd.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.vog.base.controller.BaseController;
import org.vog.base.model.mongo.BaseMongoMap;
import org.vog.common.Constants;
import org.vog.dbd.service.TableService;
import org.vog.dbd.web.login.CustomerUserDetails;

/**
 * 用户登录操作
 */
@Controller
public class LoginController extends BaseController {

    @Autowired
    private TableService tableService;

    /**
     * 登录成功
     */
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView model = new ModelAndView();

        CustomerUserDetails userObj = (CustomerUserDetails) ((Authentication) request.getUserPrincipal()).getPrincipal();
        Long dbId = userObj.getFavorite();
        if (dbId != null && dbId != 0) {
            userObj.putContext(Constants.KEY_CURR_DB_ID, dbId);
            model.setViewName("table/table_list");

            BaseMongoMap dbMap = tableService.findDbById(dbId);
            if (dbMap == null || dbMap.isEmpty()) {
                // 数据库不存在
                model.addObject("dbId", 0);
            } else {
                model.addObject("dbId", dbId);
                model.addObject("dbName", dbMap.getStringAttribute("dbName"));
            }
        } else {
            model.setViewName("db_list");
            model.addObject("dbList", tableService.findDbList());
        }
        return model;
    }

}

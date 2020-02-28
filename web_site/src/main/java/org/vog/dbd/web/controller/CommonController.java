package org.vog.dbd.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.vog.common.base.controller.BaseController;
import org.vog.dbd.web.util.SystemProperty;
import org.vog.common.util.StringUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 查询表及列的一览
 */
@Controller
public class CommonController extends BaseController {

    @Autowired
    private MessageSource messageSource;

    /**
     * 获取数据类型的定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getColDataType", method = RequestMethod.GET)
    public List<Map<String, Object>> getColDef(@RequestParam Map<String, String> params) {
        int dbType = StringUtil.convertToInt(params.get("dbType"));
        List<Map<String, Object>> dataList = SystemProperty.resolveProperty("col_datatype_" + dbType, null, List.class);
        if (dataList == null) {
            dataList = Collections.EMPTY_LIST;
        }
        return dataList;
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
}

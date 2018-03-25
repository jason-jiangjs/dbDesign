package org.vog.dbd.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.vog.base.controller.BaseController;
import org.vog.common.util.StringUtil;

import java.util.Map;

/**
 * 查询表及列的一览
 */
@Controller
public class CommonController extends BaseController {

    /**
     * 获取数据类型的定义
     */
    @ResponseBody
    @RequestMapping(value = "/ajax/getColDataType", method = RequestMethod.GET)
    public String getColDef(@RequestParam Map<String, String> params) {
        int dbType = StringUtil.convertToInt(params.get("dbType"));
        if (dbType == 1) {
            return "[ {\"text\":\"char\"}, {\"text\":\"varchar\"}, {\"text\":\"tinytext\"}, {\"text\":\"text\"}, {\"text\":\"blob\"}, {\"text\":\"mediumtext\"}, {\"text\":\"mediumblob\"}, {\"text\":\"longtext\"}, {\"text\":\"longblob\"}, {\"text\":\"tinyint\"}, {\"text\":\"smallint\"}, {\"text\":\"mediumint\"}, {\"text\":\"int\"}, {\"text\":\"bigint\"}, {\"text\":\"float\"}, {\"text\":\"double\"}, {\"text\":\"decimal\"}, {\"text\":\"date\"}, {\"text\":\"datetime\"}, {\"text\":\"timestamp\"} ]";
        } else if (dbType == 2) {
            return "[{\"text\":\"objectId\"},{\"text\":\"bool\"},{\"text\":\"int\"},{\"text\":\"long\"},{\"text\":\"double\"},{\"text\":\"decimal\"},{\"text\":\"string\"},{\"text\":\"object\"},{\"text\":\"array\"},{\"text\":\"binData\"},{\"text\":\"date\"},{\"text\":\"timestamp\"},{\"text\":\"regex\"}]";
        } else if (dbType == 3) {
            return "[{\"text\":\"ancestor_path\"}, {\"text\":\"descendent_path\"}, {\"text\":\"binary\"}, {\"text\":\"boolean\"}, {\"text\":\"booleans\"}, {\"text\":\"currency\"}, {\"text\":\"date\"}, {\"text\":\"dates\"}, {\"text\":\"double\"}, {\"text\":\"doubles\"}, {\"text\":\"float\"}, {\"text\":\"floats\"}, {\"text\":\"ignored\"}, {\"text\":\"int\"}, {\"text\":\"ints\"}, {\"text\":\"location\"}, {\"text\":\"location_rpt\"}, {\"text\":\"long\"}, {\"text\":\"longs\"}, {\"text\":\"lowercase\"}, {\"text\":\"phonetic_en\"}, {\"text\":\"point\"}, {\"text\":\"random\"}, {\"text\":\"string\"}, {\"text\":\"strings\"}, {\"text\":\"tdate\"}, {\"text\":\"tdates\"}, {\"text\":\"tint\"}, {\"text\":\"tints\"}, {\"text\":\"tlong\"}, {\"text\":\"tlongs\"}, {\"text\":\"tfloat\"}, {\"text\":\"tfloats\"}, {\"text\":\"tdouble\"}, {\"text\":\"tdoubles\"}, {\"text\":\"text_cjk\"}, {\"text\":\"text_en\"}, {\"text\":\"text_en_splitting\"}, {\"text\":\"text_en_splitting_tight\"}, {\"text\":\"text_general\"}, {\"text\":\"text_general_rev\"}, {\"text\":\"text_ws\"}, {\"text\":\"textComplex\"}, {\"text\":\"textMaxWord\"}, {\"text\":\"textSimple\"}, {\"text\":\"text_ik\"}]";
        } else {
            return "[]";
        }
    }

    /**
     * 转到登录画面
     * TODO-- 期待更好方案，目前是因为AuthenticationFailureHandlerImpl要传参数到页面
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView tologin(@RequestParam Map<String, String> params) {
        ModelAndView model = new ModelAndView();
        model.setViewName("index");
        model.addObject("errMsg", params.get("msg"));
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

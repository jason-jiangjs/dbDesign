/**
 *
 */

// 默认的web context路径，如果在部署时使用其他路径，必须修改此处代码
Ap_servletContext = "/dbd";

// 加载画面时的一些全局性的初始化设置
$(function () {

    // 全局的ajax访问设置，处理ajax请求时sesion超时
    $.ajaxSetup({
        complete: function(xhr, status) {
            if (xhr.responseText && status == 'error') {
                var errObj = JSON.parse(xhr.responseText);
                if (errObj.status == 408) {
                    window.location = Ap_servletContext + "/index";
                } else if (errObj.status == 404) {
                    window.location = Ap_servletContext + "/error/404.html";
                }
            }
        }
    });

    _loadLanguage();

    // 对Date的扩展，将 Date 转化为指定格式的String
    // 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
    // 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
    Date.prototype.Format = function (fmt) { //author: meizz
        var o = {
            "M+": this.getMonth() + 1, //月份
            "d+": this.getDate(), //日
            "H+": this.getHours(), //小时
            "m+": this.getMinutes(), //分
            "s+": this.getSeconds(), //秒
            "q+": Math.floor((this.getMonth() + 3) / 3), //季度
            "S": this.getMilliseconds() //毫秒
        };
        if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    }
});

// 向string添加扩展（注意这里的参数是个数组）
function _loadLanguage() {
    // 加载资源文件（如果不考虑多语言问题，则不需要这样处理，直接在html中加载该js）
    var lang = navigator.language;
    if (!lang) {
        lang = navigator.browserLanguage;
    }
    if (lang) {
        jQuery.getScript(Ap_servletContext + "/js/messages/msg_" + lang + ".js"); // 这里的context-path固定
    }
}

// 获取资源文件中的属性值(支持参数形式)
function $translate(key) {
    try {
        if (typeof msg == "undefined") { // 这里要换一种方式加载
            _loadLanguage();
        }
    } catch (e) {
        _loadLanguage();
    }
    var args = arguments;
    args[0] = $.trim(msg[key]);
    if (args[0]) {
        return _formatString(args);
    }
    return '';

    // 向string添加扩展（注意这里的参数是个数组）
    function _formatString(argArr) {
        if (argArr.length == 0)
            return null;
        var str = argArr[0];
        for ( var i = 1; i < argArr.length; i++) {
            var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm'); // 根据参数值替换文字中的预留通配符
            str = str.replace(re, argArr[i]);
        }
        return str;
    }
}

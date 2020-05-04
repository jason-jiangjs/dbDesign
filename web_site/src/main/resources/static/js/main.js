/**
 *
 */

// 默认的web context路径，如果在部署时使用其他路径，必须修改此处代码
Ap_servletContext = "/dbd";

// 加载画面时的一些全局性的初始化设置
$(function () {

    // 全局的ajax访问设置，处理ajax请求时sesion超时
    $.ajaxSetup({
        contentType: "application/json; charset=utf-8",
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

// 获取资源文件中的属性值(支持参数形式)
function $translate(key) {
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

function compatibleIETests() {
    var userAgent = navigator.userAgent; //取得浏览器的userAgent字符串
    var isIE = userAgent.indexOf("MSIE") > -1; //判断是否IE<11浏览器
    if(isIE) {
        var reIE = new RegExp("MSIE (\\d+\\.\\d+);");
        reIE.test(userAgent);
        var fIEVersion = parseFloat(RegExp["$1"]);
        if (fIEVersion == 6 || fIEVersion == 7 || fIEVersion == 8 || fIEVersion == 9 || fIEVersion == 10) {
            return false;
        }
    }
    return true;
}

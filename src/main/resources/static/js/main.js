/**
 *
 */
$(function () {

    // 全局的ajax访问设置，处理ajax请求时sesion超时
    $.ajaxSetup({
        complete: function(xhr, status) {
            if (xhr.responseText && status == 'error') {
                var errObj = JSON.parse(xhr.responseText);
                if (errObj.status == 408) {
                    window.location = "/index";
                }
            }
        }
    });

    // 加载资源文件
    var lang = navigator.language;
    if (!lang) {
        lang = navigator.browserLanguage;
    }
    if (lang) {
        jQuery.getScript(Ap_servletContext + "/js/messages/msg_" + lang + ".js"); // 这里的context-path固定
    }

});

Ap_servletContext = "/dbd";

// 获取资源文件中的属性值(支持参数形式)
function $translate(key) {
    if (msg) {
        var args = arguments;
        args[0] = $.trim(msg[key]);
        if (args[0]) {
            return _formatString(args);
        }
    }
    return '';

    // 向string添加扩展（注意这里的参数是个数组）
    function _formatString(argArr) {
        if (argArr.length == 0)
            return null;
        var str = argArr[0];
        for ( var i = 1; i < argArr.length; i++) {
            var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
            str = str.replace(re, argArr[i]);
        }
        return str;
    }
}

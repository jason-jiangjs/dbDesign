/**
 * 文件上传，支持拖拽上传，复制粘贴上传
 * IE11以下浏览器时，使用传统的input file上传
 *
 * 支持图片(压缩成小图片)预览，其他文件显示固定图标
 * 参照资料
 * https://github.com/blueimp
 * https://ruby-china.org/topics/17266
 * https://stackoverflow.com/questions/15253468/get-pasted-image-from-clipboard-firefox
 * http://wizard.ae.krakow.pl/%7Ejb/localio.html
 * http://jishu.admin5.com/hot/140728/2303.html
 */
var gyFileUpload = (function($) {
    
    var redirectUrlDiv = "blc-redirect-url",
        extraDataDiv   = "blc-extra-data",
        internalDataDiv   = "blc-internal-data",
        preAjaxCallbackHandlers = [],
        internalDataHandlers = [],
        servletContext = "/admin", // 目前暂时先固定
        siteBaseUrl = "";//BLC-SITE-BASEURL暂时设为空，因为没有使用

    // 初始化上传控件，默认显示div区域，用于图片拖拽或复制粘贴
    function initFileBox(inputId, divId) {
        // 如果浏览器支持拖拽或复制粘贴，则显示div，否则显示传统的input file



    }

    function addInternalDataHandler(fn) {
        internalDataHandlers.push(fn);
    }
    
    /**
     * Runs all currently registered pre-ajax-callback handlers. If any such handler returns false,
     * we will stop invocation of additional handlers as well as the callback function.
     */
    function runPreAjaxCallbackHandlers($data) {
        return runGenericHandlers($data, preAjaxCallbackHandlers);
    }

    /**
     * Runs all currently registered internal data handlers. If any such handler returns false,
     * we will stop invocation of additional handlers as well as the callback function.
     */
    function runInternalDataHandlers($data) {
        return runGenericHandlers($data, internalDataHandlers);
    }
    

    // 暴露给外部使用的方法
    return {
        initFileBox : initFileBox,
        addInternalDataHandler : addInternalDataHandler,
        redirectIfNecessary : redirectIfNecessary
    }
})($);

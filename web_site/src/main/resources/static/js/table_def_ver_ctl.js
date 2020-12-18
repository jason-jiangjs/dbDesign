/**
 * 主要是表设计时的版本控制，以及sql脚本输出
 */

// 表示当前是否在查看历史版本
var isShowTagHistory = false;

// 打开新窗口，列出所有已发布的版本
function backtoDefaultHome() {
    isShowTagHistory = false;
    window.location.reload();
}

// 打开新窗口，列出所有已发布的版本
function showProjectHistory() {
    if (isShowTagHistory) {
        // 已经在看历史版本
        $('#tagList_grid').datagrid('load', {});
    } else {
        $('#tagList_grid').datagrid({
            singleSelect: true, striped: true, fit: true, rownumbers: true, method: 'get', idField: 'tagName',border:'thin',
            emptyMsg: '没有发布过版本，无数据。',
            url: Ap_servletContext + '/ajax/mng/getProjTagList?_t=' + new Date().getTime(),
            columns: [[
                {field: 'tagName', title: '版本名称', width: 180},
                {
                    field: 'tName', title: '创建者', width: 160,
                    formatter: function contentFormat(value, row, index) {
                        if (row.auditData && row.auditData.modifierName) {
                            return row.auditData.modifierName;
                        } else {
                            return '';
                        }
                    }
                },
                {
                    field: 'tagTime', title: '发布时间', width: 160, fixed: true,
                    formatter: function contentFormat(value, row, index) {
                        if (row.auditData && row.auditData.modifiedTime) {
                            return moment(row.auditData.modifiedTime).format('YYYY-MM-DD HH:mm');
                        } else {
                            return '';
                        }
                    }
                },
                {field: 'tagDesc', title: '发布说明', width: 400}
            ]],
            onClickRow: function (rowIndex, rowData) {
                // 先重新加载表
                $('#tbl-def-grid').datagrid('load', {
                    dbId: $('#dbId').val(),
                    targetType: $('#targetType').val(),
                    tagName: rowData.tagName
                });
                isShowTagHistory = true;

                // 关闭所有编辑相关的按钮/菜单
                $('#nowTagInfo').text('当前查看版本号: ' + rowData.tagName);
                $('#nowTagInfo').show();
                $('#itemOptButton').hide();
                $('#_home_page_btn').css("display", "none");
                $('#_tab_page_btn_editable').css("display", "none");
                $('#_tab_page_btn_editing').css("display", "none");
                $('#tag_home_page_btn').css("display", "inline");
                $('#tag_table_page_btn').css("display", "inline");

                // 关闭所有打开的tab页
                var allTab = $('#tbl-tabs').tabs('tabs');
                var allTabIndex = [];
                allTab.forEach(function (item, index) {
                    var tabIdx = $('#tbl-tabs').tabs('getTabIndex', item);
                    if (tabIdx == 0) {
                        return;
                    }
                    allTabIndex.push(tabIdx);
                    // 注意不能在这个循环里面直接关闭tab页，如果调用'close'方法关闭tab页，变量allTab也会动态变化，导致数据不一致
                });
                if (allTabIndex.length > 0) {
                    allTabIndex.reverse().forEach(function (item, index) {
                        $('#tbl-tabs').tabs('close', item);
                    });
                }
                $('#tagList_dlg').dialog('close');
            }
        });
    }
    $('#tagList_dlg').dialog('open');
}

// 打开新窗口，发布新版本，确定tag名称
function openNewTagDialog() {
    // 设置缺省tag名称, 从后台取回
    var loadLy = layer.load(1);
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/mng/getDefaultTagName',
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                $('#newTagName').textbox('setValue', data.data.lastTagName);
                $('#tblnewtag_dlg').dialog('open');
            } else {
                layer.msg(data.msg + ' (code=' + data.code + ")");
            }
        }
    });
}

// 取消发布新版本
function cancelNewTag() {
    $('#tblnewtag_dlg').dialog('close');
}

// 发布新版本
function submitNewTag() {

    // 先检查是否有人正在编辑表(只能通过后台查询)，若有，则要用户确认是否继续
    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/chkTableInEditing',
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 5102) {
                // 有人正在编辑表，要提示
                var inEditingList = data.data.inEditingList;
                var tipTxt = '目前还有正在编辑的表';
                if (inEditingList.length > 10) {
                    tipTxt += '(仅列举前10条)如下,请确认是否继续操作:<br>';
                } else {
                    tipTxt += '如下,请确认是否继续操作:<br>';
                }
                inEditingList.forEach(function (value, index) {
                    tipTxt += '表 ' + value.tableName + "  编辑者 " + value.currEditorName + '<br>';
                });
                layer.confirm(tipTxt, { icon: 7,
                    btn: ['确定','取消'] //按钮
                }, function(index) {
                    // 提交请求到后台
                    _createNewTag();
                }, function() {
                    // 无操作
                    $('#tblnewtag_dlg').dialog('close');
                });

            } else if (data.code == 0) {
                // 没有人在编辑表，可正常操作
                _createNewTag();
            } else {
                layer.msg(data.msg + ' (code=' + data.code + ")");
            }
        }
    });

    function _createNewTag() {
        var loadLy = layer.load(1);
        var postData = { 'lastTagName': $('#newTagName').textbox('getValue'),
            'tagDesc': $('#tagDesc').textbox('getValue')};
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/mng/createNewTag',
            data: JSON.stringify(postData),
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    // 发布成功
                    layer.alert('发布成功,新版本编号 ' + postData.lastTagName, {icon: 1});
                    $('#tblnewtag_dlg').dialog('close');
                } else {
                    layer.msg(data.msg + ' (code=' + data.code + ")");
                }
            }
        });
    }
}

// 开始修改附件
var isOnUploadAtta = false;
function begintoUploadAtta() {
    // 先检查是否已经有人在编辑，不允许多人同时编辑
    // 要去后台校验
    if (isOnUploadAtta) {
        return false;
    }
    isOnUploadAtta = true;

    if (_uploader == null) {
        if ( !WebUploader.Uploader.support() ) {
            alert( 'Web Uploader 不支持您的浏览器！如果你使用的是IE浏览器，请尝试升级 flash 播放器');
            throw new Error( 'WebUploader does not support the browser you are using.' );
        }
        _uploader = WebUploader.create({
            auto: true,
            server: Ap_servletContext + '/ajax/common/fileUpload',
            pick: '#filePickerWrapper',
            resize: false,
            dnd: '#tblatt_dlg',
            paste: document.body
        });
        // 当有文件添加进来的时候
        _uploader.on( 'beforeFileQueued', function( file ) {
            console.log('fileQueued' + file);
            // 这里有个特殊情况，要先判断超时
            var isTimeout = true;
            $.ajax({
                async: false,
                type: 'get',
                url: Ap_servletContext + '/ajax/common/checkSession?_t=' + new Date().getTime(),
                success: function (data) {
                    isTimeout = false;
                }
            });
            if (isTimeout) {
                return false;
            }

            var fileArr = _uploader.getFiles().filter(item => { return item.name == file.name});
            if (fileArr.length > 0) {
                layer.msg('重复上传文件，"' + file.name + '"已存在.');
                return false;
            }
            file.realSize = file.size;
            file.dbId = _curDbId;
            file.tableId = _curTblId;
            if (file.dbId == undefined || file.dbId == null) {
                layer.msg('未选择指定的项目，请重新选择。');
                return false;
            }
            if (file.tableId == undefined || file.tableId == null) {
                file.tableId = 0;
            }
            return true;
        });
        _uploader.on('uploadBeforeSend', function(block, data, headers) {
            // block为分块数据。
            // file为分块对应的file对象。
            var file = block.file;
            data.realSize= file.realSize;
            data.dbId = file.dbId;
            data.tableId = file.tableId;
        });

        var loadLy = null;
        _uploader.on( 'uploadProgress', function( file, percentage ) {
            // 不使用进度条，就只是简单的使用一个遮盖层
            loadLy = layer.load(1);
        });
        _uploader.on( 'uploadSuccess', function( file, res ) {
            // 取得文件访问URL
            if (res.fileUrl == null || res.fileUrl == undefined) {
                layer.msg('文件' + file.name + '上传时出错，请联系管理员.');
                return false;
            }
            appendFile(res.fileId, file.name, res.fileUrl, res.contentType);
        });

        _uploader.on( 'uploadError', function( file, res ) {
            console.log(file);
            layer.msg('上传文件出错 ' + file.name);
        });

        _uploader.on( 'uploadComplete', function( file, res ) {
            layer.close(loadLy);
        });
    }
    $('#filePickerWrapper').show();
}

// 添加文件
function appendFile(fileId, fileName, fileUrl, contentType) {
    var fileType = getUploadFileType(fileName, contentType);

    var $li = $('<li id="' + fileId + '-li-img" class="file-atta_block"><img style="max-height:64px"></li>'),
        $img = $li.find('img');
    // $list为容器jQuery实例
    var $btns = null;
    if (fileType == "image" || fileType == "pdf") {
        $btns = $('<div class="file-panel"><span class="amplify fa fa-search-plus fa-2x"></span><span id="' + fileId + '" class="cancel fa fa-trash-o fa-2x"></span></div>');
    } else {
        $btns = $('<div class="file-panel"><span class="amplify fa fa-download fa-2x"></span><span id="' + fileId + '" class="cancel fa fa-trash-o fa-2x"></span></div>');
    }
    $li.append( $btns );
    $('#filelist').append($li);

    $li.on( 'mouseenter', function() {
        $btns.stop().animate({height: 32});
    });
    $li.on( 'mouseleave', function() {
        $btns.stop().animate({height: 0});
    });
    $btns.on( 'click', 'span', function() {
        var index = $(this).index();
        switch ( index ) {
            case 0:
                // 查看
                if (fileType == "image" || fileType == "pdf") {
                    // 只是图片和pdf文件有预览
                    $('#file_view_block').empty();
                    var $fileViewDiv = null;
                    if (fileType == "image") {
                        $fileViewDiv = $('<img src="' + fileUrl + '" alt="" width="100%" height="auto">');
                    } else {
                        $fileViewDiv = $('<iframe src="' + fileUrl + '" frameborder="0" width="100%" style="min-height:563px"></iframe>');
                    }
                    $('#file_view_block').append($fileViewDiv);
                    $('#file_view_dlg').dialog('open');
                } else {
                    // 其他文件都是下载后再查看
                    // office文档无法使用view.officeapps.live.com的方案，因为是在内网，附件无法从外网访问
                    window.open(fileUrl);
                }
                return;

            case 1:
                // 删除
                if (!isOnUploadAtta) {
                    layer.msg("必须在编辑模式下才可删除文件，<br/>请先点击对话框右上角编辑按钮");
                    return;
                }
                var loadLy = layer.load(1);
                var postData = {};
                postData.fileId = $.trim($(this).attr("id"));

                $.ajax({
                    type: 'post',
                    url: Ap_servletContext + '/ajax/common/file/deleteAtta',
                    data: JSON.stringify(postData),
                    success: function (data) {
                        layer.close(loadLy);
                        if (data.code == 0) {
                            layer.msg('删除成功');
                            // 刷新附件
                            $("#" + postData.fileId + "-li-img").remove();
                        } else {
                            layer.msg(data.msg + ' (code=' + data.code + ")");
                        }
                    }
                });
                return;
        }
    });

    if (fileType == "image") {
        // 创建缩略图
        $img.attr( 'src', fileUrl );
    } else {
        var dotIdx = fileName.lastIndexOf(".");
        var extName = fileName.substring(dotIdx + 1).toLowerCase();
        if (extName == 'pdf') {
            $img.attr( 'src', 'img/PDF.png' );
        } else if (extName == 'doc' || extName == 'docx') {
            $img.attr( 'src', 'img/WORD.png' );
        } else if (extName == 'xls' || extName == 'xlsx') {
            $img.attr( 'src', 'img/EXCEL.png' );
        } else if (extName == 'ppt' || extName == 'pptx') {
            $img.attr( 'src', 'img/PPT.png' );
        } else {
            $img.attr( 'src', 'img/unknow.png' );
        }
    }
}

// 判断附件的文件类型
function getUploadFileType(fileName, contentType) {
    if (contentType.indexOf("image") == 0) {
        return "image";
    } else {
        var dotIdx = fileName.lastIndexOf(".");
        var extName = fileName.substring(dotIdx + 1).toLowerCase();
        if (extName == 'pdf') {
            return "pdf";
        } else if (extName == 'doc' || extName == 'docx' || extName == 'xls' || extName == 'xlsx' || extName == 'ppt' || extName == 'pptx') {
            return "office";
        } else {
            return "unknow";
        }
    }
}

// 关闭附件对话框
function onCloseUploadAttaDlg() {
    var fileWrapper = $('#filePickerWrapper');
    if (fileWrapper) {
        fileWrapper.hide();
    }
    $('#filelist').empty();
    if (_uploader) {
        _uploader.destroy();
        _uploader = null;
    }
    isOnUploadAtta = false;
}

// 查看附件
var _uploader = null;
// 附件的新增删除不受"开始编辑"按钮控制，独立的权限
function attMng() {
    // 默认不是编辑状态，先从后台取回附件数据
    var curTblId = _curTblId;
    if (curTblId == undefined || curTblId == null) {
        curTblId = 0;
    }
    var param = "?tblId=" + curTblId + "&dbId=" + _curDbId + "&_t=" + new Date().getTime();
    var loadLy = layer.load(1);
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/common/file/getAttaList' + param,
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                var fileList = data.data.fileList;
                if (fileList.length == 0) {
                    return;
                }
                fileList.forEach(function(uploadedFile) {
                    appendFile(uploadedFile.fileId, uploadedFile.fileName, uploadedFile.fileUrl, uploadedFile.contentType);
                });
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });

    // 弹出对话框
    $('#tblatt_dlg').dialog('open');
}

// 查看ER图
function showErDiagram() {
    layer.msg('正在加载 Draw.io', {icon: 16, shade: 0.3, time:2});
    $('#topFrame').css('width', '100%');
    $('#topFrame').css('height', '100%');
    $('#topFrame').attr('src', 'drawio?dev=1&gapi=0&db=0&od=0&tr=0&gh=0&gl=0&_time=' + new Date().getTime());
}
// 关闭ER图
function closeErDiagram() {
    $('#topFrame').css('width', '0px');
    $('#topFrame').css('height', '0px');
    $('#topFrame').attr('src', '');
}

function openHelpPage() {
    window.open(Ap_servletContext + '/docs/help.html');
}

// 查看表定义的修改历史
// 列出每一次的修改点
function showEditHistory() {
    // 从后台获取表定义的修改历史
    if (_curTblId == undefined || _curTblId == null) {
        layer.alert('出错了，当前未选择任何表！');
        return false;
    }
    var param = "?tblId=" + _curTblId + "&dbId=" + _curDbId + "&_t=" + new Date().getTime();
    var loadLy = layer.load(1);
    $.ajax({
        type: 'get',
        url: Ap_servletContext + '/ajax/table/getSQLUpdateList' + param,
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                var updatedList = data.data.updatedList;
                if (updatedList.length == 0) {
                    layer.msg('当前查看的表没有修改历史');
                    return;
                }
                $('#tblEditHisDlg').dialog('open');
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

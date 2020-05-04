/**
 * 主要是设计稿的版本控制，以及sql脚本输出
 */



// 导出SQL文
// 主页：导出所选择的表
function exportSql() {
    var tblIdList = [];
    if (opType == 1) {
        if (_curTblId == 0 || _curTblId == undefined || _curTblId == '') {
            return;
        }
        if (_curTblId < 100) {
            layer.msg("创建表时不可直接导出表定义，必须先保存．");
            return;
        }
        tblIdList.push(_curTblId);
    } else {
        // 获取所选中的表
        var tblList = $('#tbl-def-grid').datagrid('getChecked');
        if (tblList.length == 0) {
            layer.msg('未选择要删除的表');
            return false;
        }
        tblList.forEach(function(obj, index) {
            tblIdList.push(obj._id);
        });
    }

    var url = Ap_servletContext + '/ajax/exportSql';

    var tmpInput = $("<input type='text' name='tblIdList'/>");
    tmpInput.attr("value", JSON.stringify(tblIdList));
    var myform = $("<form></form>").attr("action", url).attr("method", "post");
    myform.append(tmpInput);
    myform.appendTo('body').submit().remove();
}

// 导出SQL文
// 主页：导出所选择的表的变更SQL脚本(相对于以前发布的版本)
function exportUpdateSql() {

}

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
            singleSelect: true, striped: true, fit: true, rownumbers: true, method: 'get', idField: 'tagName',
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

// 查看附件
function attMng() {
    if (_curTblId == 0 || _curTblId == undefined || _curTblId == null) {
        return false;
    }
    var gridObj = $('#tblidx_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        // 没有选择时，表示是整个表的附件


    } else {
        // 表示是当前行的附件


    }

    // 弹出对话框
    // $('#tblatt_dlg').dialog('open');

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
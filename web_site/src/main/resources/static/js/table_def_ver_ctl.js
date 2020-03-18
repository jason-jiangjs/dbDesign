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

// 发布新版本
function releaseNewTag() {
    // 先检查是否有人正在编辑表(只能通过后台查询)，若有，则要用户确认是否继续
    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/chkTableInEditing',
        contentType: "application/json; charset=utf-8",
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
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/releaseNewTag',
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    // 发布成功
                    layer.alert('发布成功,新版本编号 ' + data.tagName, {icon: 1});
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
function lookDiagram() {

}

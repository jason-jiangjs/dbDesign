/**
 * 数据库设计管理
 */

// 画面项目初始化，加载数据库一览
$(function () {

    // 加载列定义
    var options = {
        idField: "_id",
        fit: true,
        fitColumns: true,
        nowrap: false,
        striped: true,
        singleSelect: true,
        method: 'get',
        pagination: true,
        pageSize: 20,
        pageList: [20,50,100],
        toolbar: '#db_grid_toolbar'
    };
    options.url = Ap_servletContext + '/ajax/mng/getDbList?_t=' + new Date().getTime();
    options.columns = [[
        {field:'_id',title:'ID',width:80},
        {field:'dbName',title:'名称',width:100},
        {field:'dbNameCN',title:'说明',width:100},
        {field:'typeStr',title:'版本号',width:100},
        {field:'desc',title:'备注',width:100},
        {field:'type',title:'类型',width:100,
            formatter: function(value, row, index) {
                if (value) {
                    return $translate('db_type_val_' + value);
                }
                return '';
            }
        },
        {field:'deleteFlg',title:'状态',width:80,
            formatter: function(value, row, index) {
                if (value == 'true') {
                    return $translate('user_status_val_1');
                } else if (value == 'false') {
                    return $translate('user_status_val_4');
                }
                return '';
            }
        }
    ]];

    options.onDblClickRow = function(index, row) {
        // 弹出对话框，显示数据库详细信息
        $('#userId').val(row._id);
        $('#optType').val(0);
        $('#accNo').textbox('setValue', row.userId);
        $('#userName').textbox('setValue', row.userName);
        $('#role').combobox('select', row.role);
        $('#status').combobox('select', row.status);
        $('#db_dlg').dialog('open');
    };

    $('#db_grid').datagrid(options);
});

// 提交修改(保存)
function submitForm() {
    // 不判断是否已修改，全部提交至后台
    var postData = {};
    postData.tiid = $.trim($('#userId').val());
    postData.optType = $.trim($('#optType').val());
    postData.accNo = $.trim($('#accNo').textbox('getValue'));
    postData.accName = $.trim($('#userName').textbox('getValue'));
    postData.role = $.trim($('#role').combobox('getValue'));
    postData.status = $.trim($('#status').combobox('getValue'));

    var loadLy = layer.load(1);
    $.ajax({
        type: 'post',
        url: Ap_servletContext + '/ajax/mng/saveUserInfo',
        data: JSON.stringify(postData),
        contentType: "application/json; charset=utf-8",
        success: function (data) {
            layer.close(loadLy);
            if (data.code == 0) {
                layer.msg('保存成功，将关闭对话框．');
                setTimeout("_endSave()", 1000)
            } else {
                layer.msg(data.msg + ' code=' + data.code);
            }
        }
    });
}

// 关闭对话框,刷新用户一览(当前分页)
function _endSave() {
    $('#db_grid').datagrid('reload', {});
    $('#db_dlg').dialog('close');
}

// 取消修改，并关闭对话框
function cancelForm() {
    $('#db_dlg').dialog('close');
}

// 添加数据库
function addDb() {
    // 弹出对话框
    $('#userId').val(null);
    $('#optType').val(1);
    $('#accNo').textbox('setValue', null);
    $('#userName').textbox('setValue', null);
    $('#role').combobox('select', '0');
    $('#status').combobox('select', '0');
    $('#db_dlg').dialog('open');
}

// 删除数据库
function deleteDb() {
    var gridObj = $('#db_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        layer.msg('请选择一个数据库后再操作．')
        return;
    }
    var s2 = gridObj.datagrid('getRowIndex', s1._id);
    if (s2 < 0) {
        layer.msg('数据错误，请刷新画面后再操作．')
        return;
    }

    layer.confirm('确定要删除选定的数据库［' + s1.dbName + '］?<br>该操作不可恢复，是否确认删除?', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        // 提交请求到后台
        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/mng/delDb?dbId=' + s1._id,
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    layer.close(index);
                    // 刷新用户一览
                    $('#db_grid').datagrid('reload', {});
                } else {
                    layer.msg(data.msg + ' code=' + data.code);
                }
            }
        });
    }, function() {
        // 无操作
    });
}

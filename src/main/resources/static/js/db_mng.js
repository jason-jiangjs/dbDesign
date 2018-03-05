/**
 * 数据库设计管理
 */

// 画面项目初始化，加载用户一览
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
        pageSize: 10,
        pageList: [10,20,50,100],
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

    $('#db_grid').datagrid(options);

    $.extend($.fn.datagrid.methods, {
        editCell: function(jq,param){
            return jq.each(function(){
                var opts = $(this).datagrid('options');
                var fields = $(this).datagrid('getColumnFields',true).concat($(this).datagrid('getColumnFields'));
                for(var i=0; i<fields.length; i++){
                    var col = $(this).datagrid('getColumnOption', fields[i]);
                    col.editor1 = col.editor;
                    if (fields[i] != param.field){
                        col.editor = null;
                    }
                }
                $(this).datagrid('beginEdit', param.index);
                for(var i=0; i<fields.length; i++){
                    var col = $(this).datagrid('getColumnOption', fields[i]);
                    col.editor = col.editor1;
                }
            });
        }
    });
});

var editIndex = null;
// 双击表格行，开始编辑，只允许一行一行编辑
function onClickRowBegEdit(index,　field,　value) {
    if (endEditing()) {
        $('#role_grid').datagrid('selectRow', index).datagrid('editCell', { index: index, field: field });
        var ed = $('#role_grid').datagrid('getEditor', { index: index, field: field });
        if (ed && ed.target) {
            if (field == 'desc') {
                $(ed.target).focus();
            } else {
                if ($(ed.target).closest('td').find("input.textbox-text")[0]) {
                    $(ed.target).closest('td').find("input.textbox-text")[0].focus();
                }
            }
        }
        //$(ed.target).focus();
        editIndex = index;
    }
}

// 结束编辑状态
function endEditing() {
    if (editIndex == null) {
        return true
    }
    if ($('#role_grid').datagrid('validateRow', editIndex)) {
        $('#role_grid').datagrid('endEdit', editIndex);
        editIndex = null;
        return true;
    } else {
        return false;
    }
}

// 提交修改(保存)
function submitForm() {
    $('#role_grid').datagrid('acceptChanges');
    var roleList = $('#role_grid').datagrid('getRows');
    if (roleList.length > 0) {
        for (idx in roleList) {
            if (roleList[idx].dbName == '' || roleList[idx].role == '') {
                layer.alert("没有正确设置访问权限,不允许设置空的值.");
                return;
            }
        }
    }

    // 不判断是否已修改，全部提交至后台
    var postData = {};
    postData.tiid = $.trim($('#userId').val());
    postData.optType = $.trim($('#optType').val());
    postData.accNo = $.trim($('#accNo').textbox('getValue'));
    postData.accName = $.trim($('#userName').textbox('getValue'));
    postData.role = $.trim($('#role').combobox('getValue'));
    postData.status = $.trim($('#status').combobox('getValue'));
    postData.roleList = roleList;

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


// 添加用户
function addUser() {
    //　弹出对话框
    $('#userId').val(null);
    $('#optType').val(1);
    $('#accNo').textbox('setValue', null);
    $('#userName').textbox('setValue', null);
    $('#role').combobox('select', '0');
    $('#status').combobox('select', '0');

    // 加载权限信息
    $('#role_grid').datagrid({
        url: Ap_servletContext + '/ajax/mng/getUserRoleList?iid=0&_t=' + new Date().getTime(),
        columns: role_grid_cols,
        onDblClickCell: onClickRowBegEdit
    });
    $('#user_dlg').dialog('open');
}

// 删除用户
function deleteUser() {
    var gridObj = $('#user_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        layer.msg('请选择一个用户后再操作．')
        return;
    }
    var s2 = gridObj.datagrid('getRowIndex', s1._id);
    if (s2 < 0) {
        layer.msg('数据错误，请刷新画面后再操作．')
        return;
    }

    layer.confirm('确定要删除选定的用户［' + s1.userId + '］?<br>该操作不可恢复，是否确认删除?', { icon: 7,
        btn: ['确定','取消'] //按钮
    }, function(index) {
        // 提交请求到后台
        var loadLy = layer.load(1);
        $.ajax({
            type: 'post',
            url: Ap_servletContext + '/ajax/mng/delUser?userId=' + s1.userId,
            success: function (data) {
                layer.close(loadLy);
                if (data.code == 0) {
                    layer.close(index);
                    // 刷新用户一览
                    $('#user_grid').datagrid('reload', {});
                } else {
                    layer.msg(data.msg + ' code=' + data.code);
                }
            }
        });
    }, function() {
        // 无操作
    });
}

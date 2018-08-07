/**
 * 用户管理
 */

var role_grid_cols = [[
    { field:'dbId', title:'数据库名称', width:473,
        formatter: function(value, row, index) {
            // 因为从combobox中选中值获得的是该valueField，要再转换一次，用于画面显示
            if (value) {
                var nowTxt = null;
                for (idx in this.editor.options.data) {
                    if (value == this.editor.options.data[idx].dbId) {
                        nowTxt = this.editor.options.data[idx].dbNameTxt;
                    }
                }
                if (nowTxt) {
                    return nowTxt;
                }
            }
            return '';
        },
        editor : {
            "type" : "combobox",
            "options" : {
                "valueField" : "dbId",
                "textField" : "dbNameTxt",
                "method" : "get",
                "required" : true,
                limitToList: true,
                panelHeight: 100,
                editable: false
            }
        }
    },
    { field:'role', title:'访问权限', width:95,
        formatter: function(value, row, index) {
            if (value) {
                return $translate('role_val_' + value);
            }
            return '';
        },
        editor : {
            "type" : "combobox",
            "options" : {
                "valueField" : "text",
                "textField" : "name",
                "data" : [
                    {text:'1',name:'只读权限'},
                    {text:'2',name:'读写权限'},
                    {text:'8',name:'项目管理员'},
                    {text:'9',name:'系统管理员'}
                ],
                "required" : true,
                limitToList: true,
                panelHeight: 100,
                editable: false
            }
        }
    }
]];

// 画面项目初始化，加载用户一览
$(function () {

    // 必须这样动态定义
    var loadLy = layer.load(1);
    $.ajax({
        type: 'get',
        async: false,
        url: Ap_servletContext + "/ajax/getDbListByUser?iid=" + $.trim($('#adminId').val()) + '&_t=' + new Date().getTime(),
        success: function (data) {
            layer.close(loadLy);
            role_grid_cols[0][0].editor.options.data = data;
        }
    });
    //role_grid_cols[0][0].editor.options.url = Ap_servletContext + "/ajax/getDbListByUser?iid=" + $.trim($('#adminId').val()) + '&_t=' + new Date().getTime();

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
        toolbar: '#user_grid_toolbar'
    };
    options.onDblClickRow = function(index, row) {
        // 弹出对话框，显示用户详细信息（包括数据库访问权限）
        // 加载用户信息
        $('#userId').val(row._id);
        $('#optType').val(0);
        $('#accNo').textbox('setValue', row.userId);
        $('#userName').textbox('setValue', row.userName);
        $('#role').combobox('select', row.role);
        $('#status').combobox('select', row.status);

        // 加载权限信息
        $('#role_grid').datagrid({
            url: Ap_servletContext + '/ajax/mng/getUserRoleList?iid=' + row._id + '&_t=' + new Date().getTime(),
            columns: role_grid_cols,
            onDblClickCell: onClickRowBegEdit
        });
        $('#user_dlg').dialog('open');
    };
    options.url = Ap_servletContext + '/ajax/mng/getUserList?_t=' + new Date().getTime();
    options.columns = [[
        {field:'_id',title:'ID',width:80},
        {field:'userId',title:'登录帐号',width:100},
        {field:'userName',title:'姓名',width:100},
        {field:'from',title:'来源',width:100},
        {field:'role',title:'角色',width:80,
            formatter: function(value, row, index) {
                if (value) {
                    return $translate('role_val_' + value);
                }
                return '';
            }
        },
        {field:'status',title:'状态',width:100,
            formatter: function(value, row, index) {
                if (value >= 0) {
                    return $translate('user_status_val_' + value);
                }
                return '';
            }
        }
    ]];

    $('#user_grid').datagrid(options);

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
function onClickRowBegEdit(index, field, value) {
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

// 关闭对话框,刷新用户一览(当前分页)
function _endSave() {
    $('#user_grid').datagrid('reload', {});
    $('#user_dlg').dialog('close');
}

// 取消修改，并关闭对话框
function cancelForm() {
    $('#user_dlg').dialog('close');
}

// 添加用户
function addUser() {
    // 弹出对话框
    $('#userId').val(null);
    $('#optType').val(1);
    $('#accNo').textbox('setValue', null);
    $('#userName').textbox('setValue', null);
    $('#role').combobox('select', '0');
    $('#status').combobox('select', '0');

    // 加载权限信息
    $('#role_grid').datagrid({
        //data: [{}],
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
            url: Ap_servletContext + '/ajax/mng/delUser?userId=' + s1._id,
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
var defaultVal = 0;
// 添加用户权限
function addUserRole() {
    defaultVal ++;
    $('#role_grid').datagrid('appendRow', { default: defaultVal });
}

// 删除用户权限
function deleteUserRole() {
    var gridObj = $('#role_grid');
    var s1 = gridObj.datagrid('getSelected');
    if (s1 == null || s1 == undefined) {
        layer.msg('请选择一个项目后再操作．')
        return;
    }
    if (s1.default >= 1) {
        // 新增的项目，直接删除
        var s2 = gridObj.datagrid('getRowIndex', s1);
        if (s2 < 0) {
            layer.msg('数据错误，请刷新画面后再操作．')
            return;
        }
        gridObj.datagrid('deleteRow', s2);
        return;
    }

    var s2 = gridObj.datagrid('getRowIndex', s1.dbId);
    if (s2 < 0) {
        layer.msg('数据错误，请刷新画面后再操作．')
        return;
    }
    // 再对比一下数据是否正确
    var roleList = $('#role_grid').datagrid('getRows');
    if (roleList.length == 0) {
        layer.msg("数据错误，没有设置访问权限");
        return;
    }
    if (roleList[s2].dbId == undefined || roleList[s2].dbId != s1.dbId) {
        layer.msg("数据错误，id不一致");
        return;
    }
    gridObj.datagrid('deleteRow', s2);
}
